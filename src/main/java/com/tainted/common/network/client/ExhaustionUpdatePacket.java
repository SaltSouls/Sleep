package com.tainted.common.network.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ExhaustionUpdatePacket {
    private final String exhaustionState;

    public ExhaustionUpdatePacket(String exhaustionState) { this.exhaustionState = exhaustionState; }

    public static ExhaustionUpdatePacket decode(FriendlyByteBuf buf) {
        String exhaustionState = buf.readUtf();
        return new ExhaustionUpdatePacket(exhaustionState);
    }

    public void encode(FriendlyByteBuf buf) { buf.writeUtf(this.exhaustionState); }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.updateExhaustionState(this.exhaustionState));
    }

}
