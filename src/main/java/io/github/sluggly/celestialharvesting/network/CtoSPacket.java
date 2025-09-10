package io.github.sluggly.celestialharvesting.network;

import io.github.sluggly.celestialharvesting.data.PlayerActionHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CtoSPacket {
    public final String action;
    public final CompoundTag data;

    public CtoSPacket(String action) {
        this.action = action;
        this.data = new CompoundTag();
    }

    public CtoSPacket(String action, CompoundTag customData) {
        this.action = action;
        this.data = customData.copy();
    }

    public CtoSPacket(FriendlyByteBuf buffer) {
        this.action = buffer.readUtf();
        this.data = buffer.readAnySizeNbt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(action);
        buffer.writeNbt(data);
    }

    public static void handle(CtoSPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> PlayerActionHandler.handlePlayerAction(message, ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }
}
