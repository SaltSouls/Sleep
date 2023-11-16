package com.tainted.common.network;

import com.tainted.Sleep;
import com.tainted.common.network.client.ExhaustionUpdatePacket;
import com.tainted.common.network.client.SleepTimeUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "S1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Sleep.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void registerMessages() {
        int id = 0;
        INSTANCE.messageBuilder(ExhaustionUpdatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ExhaustionUpdatePacket::encode)
                .decoder(ExhaustionUpdatePacket::decode)
                .consumerMainThread(ExhaustionUpdatePacket::handle)
                .add();
        INSTANCE.messageBuilder(SleepTimeUpdatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SleepTimeUpdatePacket::encode)
                .decoder(SleepTimeUpdatePacket::decode)
                .consumerMainThread(SleepTimeUpdatePacket::handle)
                .add();

    }

    public static void sendToClient(Object message, ServerPlayer player) {
        INSTANCE.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object message, ServerPlayer player) {
        INSTANCE.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_SERVER);
    }

}
