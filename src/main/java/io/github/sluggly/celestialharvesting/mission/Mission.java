package io.github.sluggly.celestialharvesting.mission;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class Mission {
    private final ResourceLocation id;
    private final MissionDefinition definition;

    public Mission(ResourceLocation id, MissionDefinition definition) {
        this.id = id;
        this.definition = definition;
    }

    public ResourceLocation getId() { return id; }
    public String getName() { return definition.name(); }
    public ResourceLocation getIcon() { return new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/gui/" + definition.icon() + ".png"); }
    public int getTravelTime() { return definition.travel(); }
    public int getFuelCost() { return definition.fuel(); }
    public List<String> getRequiredModules() { return definition.module().orElse(List.of()); }
    public int getDamage() { return definition.damage(); }
    public List<MissionItem> getRewards() { return definition.rewards(); }
    public int getRequiredTier() { return this.definition.tier().orElse(1); }

    public static Mission getMissionFromId(ResourceLocation missionId) {
        MissionDefinition definition = MissionManager.getInstance().getMission(missionId);
        if (definition != null) { return new Mission(missionId, definition); }
        return null;
    }
}