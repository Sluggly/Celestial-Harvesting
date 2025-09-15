package io.github.sluggly.celestialharvesting.upgrade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.sluggly.celestialharvesting.network.PacketHandler;
import io.github.sluggly.celestialharvesting.network.StoCPacket;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UpgradeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final String DIRECTORY = "upgrades";
    private static final UpgradeManager INSTANCE = new UpgradeManager();

    private Map<ResourceLocation, UpgradeDefinition> upgrades = new HashMap<>();

    private UpgradeManager() { super(GSON, DIRECTORY); }
    public static UpgradeManager getInstance() { return INSTANCE; }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonObjects, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, UpgradeDefinition> loadedUpgrades = new HashMap<>();
        jsonObjects.forEach((location, jsonElement) -> {
            UpgradeDefinition.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                    .resultOrPartial(error -> System.err.println("Failed to parse upgrade " + location + ": " + error))
                    .ifPresent(upgrade -> loadedUpgrades.put(location, upgrade));
        });
        this.upgrades = loadedUpgrades;
        System.out.println("Loaded " + this.upgrades.size() + " harvester upgrades.");
    }

    public Map<ResourceLocation, UpgradeDefinition> getAllUpgrades() { return this.upgrades; }

    public void setClientUpgrades(Map<ResourceLocation, UpgradeDefinition> upgrades) {
        this.upgrades = upgrades;
        System.out.println("Client received and loaded " + this.upgrades.size() + " harvester upgrades.");
    }

    public static void syncUpgradesToClient(ServerPlayer player) {
        Map<ResourceLocation, UpgradeDefinition> upgrades = UpgradeManager.getInstance().getAllUpgrades();
        CompoundTag upgradeData = new CompoundTag();

        upgrades.forEach((location, definition) -> {
            UpgradeDefinition.CODEC.encodeStart(NbtOps.INSTANCE, definition)
                    .resultOrPartial(error -> System.err.println("Failed to encode upgrade " + location + ": " + error))
                    .ifPresent(tag -> upgradeData.put(location.toString(), tag));
        });

        CompoundTag payload = new CompoundTag();
        payload.put(NBTKeys.UPGRADES_DATA, upgradeData);
        PacketHandler.sendToPlayer(NBTKeys.ACTION_SYNC_UPGRADES, payload, player);
        System.out.println("Sent " + upgrades.size() + " upgrades to player " + player.getGameProfile().getName());
    }

    public static void handleUpgradeSync(StoCPacket msg) {
        CompoundTag data = msg.data;
        if (data == null || !data.contains(NBTKeys.UPGRADES_DATA)) return;

        CompoundTag upgradesTag = data.getCompound(NBTKeys.UPGRADES_DATA);
        Map<ResourceLocation, UpgradeDefinition> syncedUpgrades = new HashMap<>();

        for (String key : upgradesTag.getAllKeys()) {
            ResourceLocation location = new ResourceLocation(key);
            Tag upgradeTag = upgradesTag.get(key);
            UpgradeDefinition.CODEC.parse(NbtOps.INSTANCE, upgradeTag)
                    .resultOrPartial(error -> System.err.println("Failed to decode upgrade " + location + ": " + error))
                    .ifPresent(definition -> syncedUpgrades.put(location, definition));
        }
        UpgradeManager.getInstance().setClientUpgrades(syncedUpgrades);
    }
}