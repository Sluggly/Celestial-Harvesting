package io.github.sluggly.celestialharvesting.mission;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
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

    public MissionDefinition getMission(ResourceLocation id) {
        return this.loadedMissions.get(id);
    }

    public Map<ResourceLocation, MissionDefinition> getAllMissions() {
        return this.loadedMissions;
    }

    // This method will be called on the client side when receiving the sync packet
    public void setMissions(Map<ResourceLocation, MissionDefinition> missions) {
        this.loadedMissions = missions;
        System.out.println("Client received and loaded " + this.loadedMissions.size() + " missions.");
    }
}