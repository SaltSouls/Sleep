package com.tainted.common.network.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SleepTimeUpdatePacket {
    private final String sleepTime;

    public SleepTimeUpdatePacket(String sleepTime) { this.sleepTime = sleepTime; }

    public static SleepTimeUpdatePacket decode(FriendlyByteBuf buf) {
        String sleepTime = buf.readUtf();
        return new SleepTimeUpdatePacket(sleepTime);
    }

    public void encode(FriendlyByteBuf buf) { buf.writeUtf(this.sleepTime); }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.updateSleepTime(this.sleepTime));
    }

}
