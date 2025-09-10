package io.github.sluggly.celestialharvesting.network;

import io.github.sluggly.celestialharvesting.client.handler.ClientHooks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StoCPacket {
    public final String action;
    public final CompoundTag data;

    public StoCPacket(FriendlyByteBuf buffer) {
        this.action = buffer.readUtf();
        this.data = buffer.readAnySizeNbt();
    }

    public StoCPacket(String action, CompoundTag customData) {
        this.action = action;
        this.data = customData.copy();
    }

    public StoCPacket(String action) {
        this.action = action;
        this.data = null;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.action);
        buffer.writeNbt(data);
    }

    public static void handle(StoCPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.handleActions(message)));
        ctx.get().setPacketHandled(true);
    }
}
