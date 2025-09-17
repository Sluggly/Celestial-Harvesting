package io.github.sluggly.celestialharvesting.mission;

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

public class MissionManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final String DIRECTORY = "missions";
    private static final MissionManager INSTANCE = new MissionManager();

    private Map<ResourceLocation, MissionDefinition> loadedMissions = new HashMap<>();

    private MissionManager() {
        super(GSON, DIRECTORY);
    }

    public static MissionManager getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonObjects, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, MissionDefinition> newMissions = new HashMap<>();

        jsonObjects.forEach((location, jsonElement) -> {
            MissionDefinition.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                    .resultOrPartial(error -> System.err.println("Failed to parse mission " + location + ": " + error))
                    .ifPresent(mission -> newMissions.put(location, mission));
        });

        this.loadedMissions = newMissions;
        System.out.println("Loaded " + this.loadedMissions.size() + " mission definitions.");
    }

    public MissionDefinition getMission(ResourceLocation id) { return this.loadedMissions.get(id); }

    public Map<ResourceLocation, MissionDefinition> getAllMissions() { return this.loadedMissions; }

    public void setMissions(Map<ResourceLocation, MissionDefinition> missions) {
        this.loadedMissions = missions;
        System.out.println("Client received and loaded " + this.loadedMissions.size() + " missions.");
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

    public static void handleMissionSync(StoCPacket msg) {
        CompoundTag data = msg.data;
        if (data == null || !data.contains(NBTKeys.MISSIONS_DATA)) {
            System.err.println("Received mission sync packet with no data!");
            return;
        }

        CompoundTag missionsTag = data.getCompound(NBTKeys.MISSIONS_DATA);
        Map<ResourceLocation, MissionDefinition> syncedMissions = new HashMap<>();

        for (String key : missionsTag.getAllKeys()) {
            ResourceLocation location = new ResourceLocation(key);
            Tag missionTag = missionsTag.get(key);

            MissionDefinition.CODEC.parse(NbtOps.INSTANCE, missionTag)
                    .resultOrPartial(error -> System.err.println("Failed to decode mission " + location + ": " + error))
                    .ifPresent(definition -> syncedMissions.put(location, definition));
        }
        MissionManager.getInstance().setMissions(syncedMissions);
    }
}