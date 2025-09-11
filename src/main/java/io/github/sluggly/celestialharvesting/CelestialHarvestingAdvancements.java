package io.github.sluggly.celestialharvesting;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.function.Consumer;

public class CelestialHarvestingAdvancements {
    public static final HashMap<String,HashMap<String, Consumer<ServerPlayer>>> mapAllAdvancement;
    static {
        mapAllAdvancement = new HashMap<>();
        mapAllAdvancement.put("Bet",new HashMap<>());
    }

    // MISC ADVANCEMENTS
    public static void emptyFunction(ServerPlayer player) {}

    public static void grantCustomAdvancement(ServerPlayer player, String advancementId) {
        MinecraftServer server = player.getServer();
        if (server == null) { return; }
        ResourceLocation id = new ResourceLocation(CelestialHarvesting.MOD_ID, advancementId);
        Advancement advancement = server.getAdvancements().getAdvancement(id);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
            }
        }
    }

    public static void checkAndTestGrantAdvancement(ServerPlayer player, String action) {
        MinecraftServer server = player.getServer();
        if (server == null) { return; }
        for (String advancementId : mapAllAdvancement.get(action).keySet()) {
            ResourceLocation id = new ResourceLocation(CelestialHarvesting.MOD_ID, advancementId);
            Advancement advancement = server.getAdvancements().getAdvancement(id);
            if (advancement != null) {
                if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                    mapAllAdvancement.get(action).get(advancementId).accept(player);
                }
            }
        }
    }

    public static void resetAllAdvancements(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) { return; }
        for (HashMap<String,Consumer<ServerPlayer>> map : mapAllAdvancement.values()) {
            for (String stringId : map.keySet()) {
                ResourceLocation id = new ResourceLocation(CelestialHarvesting.MOD_ID, stringId);
                Advancement advancement = server.getAdvancements().getAdvancement(id);
                if (advancement != null) {
                    AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                    if (progress.hasProgress()) {
                        for (String criterion : progress.getCompletedCriteria()) {
                            player.getAdvancements().revoke(advancement,criterion);
                        }
                    }
                }
            }
        }
        player.getAdvancements().flushDirty(player);
    }

}
