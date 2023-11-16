package com.tainted.common.network.client;

import com.tainted.common.capability.StatusCapProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientPacketHandler {

    public static void updateSleepTime(String sleepTime) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return; // this should never happen
        player.getCapability(StatusCapProvider.INSTANCE)
                .ifPresent(cap -> cap.setSleepTime(sleepTime));
    }

    public static void updateExhaustionState(String exhaustionState) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return; // this should never happen
        player.getCapability(StatusCapProvider.INSTANCE)
                .ifPresent(cap -> cap.setExhaustionState(exhaustionState));
    }

}
