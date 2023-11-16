package com.tainted.common.capability;

import com.tainted.common.network.NetworkHandler;
import com.tainted.common.network.client.ExhaustionUpdatePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = com.tainted.Sleep.MODID)
public class StatusSync {

    private StatusSync() {
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(@NotNull AttachCapabilitiesEvent<Entity> event) {
        event.addCapability(StatusCapProvider.NAME, new StatusCapProvider());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sync((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        sync((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.@NotNull Clone event) {
        if (!event.getEntity().level.isClientSide && event.isWasDeath()) {
            event.getEntity().getCapability(StatusCapProvider.INSTANCE)
                    .ifPresent(cap -> {
                        CompoundTag old = cap.serializeNBT();
                        event.getEntity().getCapability(StatusCapProvider.INSTANCE)
                                .ifPresent(exhaustionState -> cap.deserializeNBT(old));
                    });
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        event.getEntity().getCapability(StatusCapProvider.INSTANCE)
                .ifPresent(cap -> cap.setExhaustionState("rested"));
    }

    private static void sync(ServerPlayer player) {
        player.getCapability(StatusCapProvider.INSTANCE)
                .ifPresent(cap -> {
                    NetworkHandler.sendToClient(new ExhaustionUpdatePacket(cap.getExhaustionState()), player);
                });
    }

}
