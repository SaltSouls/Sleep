package com.tainted.common.events;

import com.tainted.Sleep;
import com.tainted.common.capability.Status;
import com.tainted.common.capability.StatusCapProvider;
import com.tainted.common.network.client.ExhaustionUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import static com.tainted.common.network.NetworkHandler.sendToClient;
import static com.tainted.common.utils.ExhaustionUtils.*;

@Mod.EventBusSubscriber(modid = com.tainted.Sleep.MODID)
public class TickEvents {

    private static void updateExhaustion(String exhaustion, @NotNull ServerPlayer player) {
        LazyOptional<Status> cap = player.getCapability(StatusCapProvider.INSTANCE);
        cap.ifPresent(playerCap -> {
            if(playerCap.getExhaustionState().equals(exhaustion)) return;
            playerCap.setExhaustionState(exhaustion);
            sendToClient(new ExhaustionUpdatePacket(playerCap.getExhaustionState()), player);
        });
    }

    private static int seconds(int seconds) {
        return seconds * 20;
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.@NotNull PlayerTickEvent event) {
        // only run event during the END phase
        TickEvent.Phase phase = event.phase;
        if (!phase.equals(TickEvent.Phase.END)) return;
        // only run event if not client-side
        Level level = event.player.getLevel();
        if (level.isClientSide() && !(event.player instanceof ServerPlayer)) return;
        // only run checks every 30 seconds
        long time = level.getGameTime();
        if (time % seconds(30) != 0) return;
        // reset exhaustion and return if on peaceful
        ServerPlayer player = (ServerPlayer) event.player;
        Difficulty difficulty = level.getDifficulty();
        if (difficulty.equals(Difficulty.PEACEFUL)) {
            updateExhaustion("rested", player);
            return;
        }
        ServerStatsCounter stats = player.getStats();
        int dayLength = Level.TICKS_PER_DAY;
        int lastRested = stats.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        int days = Mth.clamp(lastRested, 1, Integer.MAX_VALUE) / dayLength;
        Exhaustion exhaustion = getExhaustionState(level, days);
        if (exhaustion == null) return; // this should never happen
        // log info during the start of a new day
        if (level.getDayTime() % dayLength <= seconds(30)) {
            Sleep.LOGGER.info("Current exhaustion state is: " + exhaustion);
            Sleep.LOGGER.info(days + " day(s) have passed without sleeping.");
        }
        switch (exhaustion) {
            case RESTED -> updateExhaustion("rested", player);
            case TIRED -> updateExhaustion("tired", player);
            case VERY_TIRED -> updateExhaustion("very_tired", player);
            case EXHAUSTED -> updateExhaustion("exhausted", player);
            case PASSED_OUT -> {
                updateExhaustion("passed_out", player);
                // sets the players sleep timer 3 days back if they pass out
                stats.setValue(player, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), lastRested - (dayLength * getMultiplier(level)));
            }
        }
    }
}
