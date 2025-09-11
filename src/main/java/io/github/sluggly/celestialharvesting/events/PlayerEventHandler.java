package io.github.sluggly.celestialharvesting.events;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.mission.MissionDefinition;
import io.github.sluggly.celestialharvesting.mission.MissionManager;
import io.github.sluggly.celestialharvesting.network.PacketHandler;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = CelestialHarvesting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(MissionManager.getInstance());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncMissionsToClient(player);
        }
    }

    public static void syncMissionsToClient(ServerPlayer player) {
        Map<ResourceLocation, MissionDefinition> missions = MissionManager.getInstance().getAllMissions();
        CompoundTag missionData = new CompoundTag();

        missions.forEach((location, definition) -> {
            MissionDefinition.CODEC.encodeStart(NbtOps.INSTANCE, definition)
                    .resultOrPartial(error -> System.err.println("Failed to encode mission " + location + ": " + error))
                    .ifPresent(tag -> missionData.put(location.toString(), tag));
        });

        CompoundTag payload = new CompoundTag();
        payload.put(NBTKeys.MISSIONS_DATA, missionData);
        PacketHandler.sendToPlayer(NBTKeys.ACTION_SYNC_MISSIONS, payload, player);
        System.out.println("Sent " + missions.size() + " missions to player " + player.getGameProfile().getName());
    }
}
