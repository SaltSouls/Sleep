package com.tainted.common.events;

import com.tainted.Sleep;
import com.tainted.common.capability.Status;
import com.tainted.common.capability.StatusCapProvider;
import com.tainted.common.network.client.SleepTimeUpdatePacket;
import com.tainted.common.utils.DespawnUtils;
import com.tainted.common.utils.TimeUtils;
import com.tainted.common.utils.TimeUtils.Time;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import static com.tainted.common.network.NetworkHandler.sendToClient;
import static com.tainted.common.utils.LevelUtils.tickBlocks;
import static com.tainted.common.utils.SleepUtils.getTimeAddition;

@Mod.EventBusSubscriber(modid = com.tainted.Sleep.MODID)
public class SleepEvents {

    // test to see if this works
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSleepTimeCheck(@NotNull SleepingTimeCheckEvent event) {
        event.setResult(Event.Result.ALLOW);
    }

    private static void updateSleepTime(String time, @NotNull Player player) {
        @NotNull LazyOptional<Status> cap = player.getCapability(StatusCapProvider.INSTANCE);
        cap.ifPresent(playerCap -> {
            if(playerCap.getSleepTime().equals(time)) return;
            playerCap.setSleepTime(time);
            sendToClient(new SleepTimeUpdatePacket(playerCap.getSleepTime()), (ServerPlayer) player);
            Sleep.LOGGER.info(player.getName() + " went to bed during " + playerCap.getSleepTime());
        });
    }

    @SubscribeEvent
    public static void onPlayerSleep(@NotNull PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        Level level = player.getLevel();
        if (!level.isClientSide) {
            Sleep.LOGGER.info("Current time slice: " + TimeUtils.getTimeSlice(level));
            TimeUtils.Time dayTime = TimeUtils.getTimeSlice(level);
            if (dayTime == null) return; // this should never happen
            switch (dayTime) {
                case MORNING_E -> updateSleepTime("early_morning", player);
                case MORNING -> updateSleepTime("morning", player);
                case MORNING_L -> updateSleepTime("late_morning", player);
                case NOON_E -> updateSleepTime("early_afternoon", player);
                case NOON -> updateSleepTime("afternoon", player);
                case NOON_L -> updateSleepTime("late_afternoon", player);
                case EVENING_E -> updateSleepTime("early_evening", player);
                case EVENING -> updateSleepTime("evening", player);
                case EVENING_L -> updateSleepTime("late_evening", player);
                case NIGHT_E -> updateSleepTime("early_night", player);
                case NIGHT -> updateSleepTime("night", player);
                case NIGHT_L -> updateSleepTime("late_night", player);
            }
        }
    }

    @SubscribeEvent
    public static void onSleepComplete(@NotNull SleepFinishedTimeEvent event) {
        Level level = (Level) event.getLevel();
        MinecraftServer server = level.getServer();
        if (server == null) return; // this should never happen
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player == null) return; // this also should never happen
            Time playerTime = TimeUtils.getPlayerTimeSlice(player);
            if (playerTime == null) return; // another thing that should never happen
            long timeAddition = getTimeAddition(level, player);
            int dayLength = Level.TICKS_PER_DAY;
            switch (playerTime) {
                case MORNING_E, MORNING, MORNING_L, NOON_E, NOON, NOON_L, NIGHT_L -> {
                    tickBlocks(player, timeAddition, dayLength);
                    event.setTimeAddition(timeAddition);
                }
                case EVENING_E, EVENING, EVENING_L, NIGHT_E, NIGHT -> {
                    DespawnUtils.despawnEntities(level, player);
                    tickBlocks(player, timeAddition, dayLength);
                    event.setTimeAddition(timeAddition);
                }
            }
        }
    }

}
