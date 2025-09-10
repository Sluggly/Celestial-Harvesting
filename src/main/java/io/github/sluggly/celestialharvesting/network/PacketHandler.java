package io.github.sluggly.celestialharvesting.network;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CelestialHarvesting.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        INSTANCE.messageBuilder(StoCPacket.class, 1)
                .encoder(StoCPacket::encode)
                .decoder(StoCPacket::new)
                .consumerMainThread(StoCPacket::handle)
                .add();
        INSTANCE.messageBuilder(CtoSPacket.class, 2)
                .encoder(CtoSPacket::encode)
                .decoder(CtoSPacket::new)
                .consumerMainThread(CtoSPacket::handle)
                .add();
    }

    public static void sendToServer(CtoSPacket msg) {
        INSTANCE.sendToServer(msg);
    }

    public static void sendToServer(String string) {
        CtoSPacket msg = new CtoSPacket(string);
        sendToServer(msg);
    }

    public static void sendToServer(String string, CompoundTag data) {
        CtoSPacket msg = new CtoSPacket(string,data);
        sendToServer(msg);
    }
    public static void sendToPlayer(StoCPacket msg, ServerPlayer player) {
        INSTANCE.sendTo(msg,player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToPlayer(String action, CompoundTag customData, ServerPlayer player) {
        StoCPacket msg = new StoCPacket(action, customData);
        sendToPlayer(msg,player);
    }

    public static void sendToPlayer(String action, ServerPlayer player) {
        StoCPacket msg = new StoCPacket(action);
        sendToPlayer(msg,player);
    }

    public static <MSG> void sendToPlayer(MSG msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToDimension(String action, net.minecraft.nbt.CompoundTag customData, ResourceKey<Level> dimension) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> dimension), new StoCPacket(action, customData));
    }

}
