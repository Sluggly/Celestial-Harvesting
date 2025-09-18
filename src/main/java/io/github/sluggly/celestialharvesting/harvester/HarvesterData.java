package io.github.sluggly.celestialharvesting.harvester;

import io.github.sluggly.celestialharvesting.mission.Mission;
import io.github.sluggly.celestialharvesting.mission.MissionManager;
import io.github.sluggly.celestialharvesting.upgrade.UpgradeDefinition;
import io.github.sluggly.celestialharvesting.upgrade.UpgradeManager;
import io.github.sluggly.celestialharvesting.utils.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

import static io.github.sluggly.celestialharvesting.utils.NBTKeys.*;

public class HarvesterData {
    public CompoundTag dataTag;

    public HarvesterData(CompoundTag tag) {
        if (tag == null) {
            this.dataTag = new CompoundTag();
            this.dataTag.putInt(HARVESTER_CURRENT_HEALTH, 100);
            this.dataTag.putInt(HARVESTER_MAX_HEALTH, 100);
            this.dataTag.putInt(HARVESTER_SEED, Utils.generateSeed());
            this.dataTag.putInt(HARVESTER_TIER,1);
            this.dataTag.putString(HARVESTER_STATUS,HARVESTER_IDLE);
            this.dataTag.putInt(HARVESTER_MISSION_TIME_LEFT, 0);
            this.dataTag.putString(HARVESTER_ACTIVE_MISSION_ID, "");
            this.dataTag.putInt(HARVESTER_INVENTORY_ROWS, 1);
            this.dataTag.put(HARVESTER_UPGRADES, new ListTag());
            rerollMissions();
        }
        else { this.dataTag = tag; }
    }

    public int getCurrentHealth() { return this.dataTag.getInt(HARVESTER_CURRENT_HEALTH); }
    public int getMaxHealth() { return this.dataTag.getInt(HARVESTER_MAX_HEALTH); }
    public void setCurrentHealth(int health) { this.dataTag.putInt(HARVESTER_CURRENT_HEALTH, health); }
    public void setMaxHealth(int health) { this.dataTag.putInt(HARVESTER_MAX_HEALTH, health); }
    public int getSeed() { return this.dataTag.getInt(HARVESTER_SEED); }
    public void generateSeed() { this.dataTag.putInt(HARVESTER_SEED, Utils.generateSeed()); }
    public String getState() { return this.dataTag.getString(HARVESTER_STATUS); }
    public void setState(String state) { this.dataTag.putString(HARVESTER_STATUS,state); }
    public int getInventoryRows() { return this.dataTag.contains(HARVESTER_INVENTORY_ROWS) ? this.dataTag.getInt(HARVESTER_INVENTORY_ROWS) : 1; }
    public void setInventoryRows(int rows) { this.dataTag.putInt(HARVESTER_INVENTORY_ROWS, rows); }

    public int getNumberOfMissions(int seed) {
        int bonus = this.getMissionCountBonus();
        int minimum = 1 + bonus;
        int maximum = 3 + bonus;
        Random random = new Random(seed);
        return random.nextInt(maximum - minimum + 1) + minimum;
    }

    public int getTier() { return this.dataTag.getInt(HARVESTER_TIER); }
    public void setTier(int tier) { this.dataTag.putInt(HARVESTER_TIER, tier); }

    public String getStatus() { return this.dataTag.getString(HARVESTER_STATUS); }
    public void setStatus(String status) { this.dataTag.putString(HARVESTER_STATUS, status); }

    public int getMissionTimeLeft() { return this.dataTag.getInt(HARVESTER_MISSION_TIME_LEFT); }
    public void setMissionTimeLeft(int ticks) { this.dataTag.putInt(HARVESTER_MISSION_TIME_LEFT, ticks); }

    public String getActiveMissionID() { return this.dataTag.getString(HARVESTER_ACTIVE_MISSION_ID); }
    public void setActiveMissionID(String id) { this.dataTag.putString(HARVESTER_ACTIVE_MISSION_ID, id); }

    public boolean hasUpgrade(ResourceLocation id) {
        ListTag upgrades = this.dataTag.getList(HARVESTER_UPGRADES, Tag.TAG_STRING);
        return upgrades.contains(StringTag.valueOf(id.toString()));
    }

    public void addUpgrade(ResourceLocation id) {
        if (!hasUpgrade(id)) {
            this.dataTag.getList(HARVESTER_UPGRADES, Tag.TAG_STRING).add(StringTag.valueOf(id.toString()));
        }
    }

    public Set<ResourceLocation> getUnlockedUpgrades() {
        Set<ResourceLocation> unlocked = new HashSet<>();
        ListTag upgrades = this.dataTag.getList(HARVESTER_UPGRADES, Tag.TAG_STRING);
        for (Tag tag : upgrades) { unlocked.add(new ResourceLocation(tag.getAsString())); }
        return unlocked;
    }

    public void rerollMissions() {
        int currentTier = this.getTier();
        int missionCountToSelect = this.getNumberOfMissions(this.getSeed());
        List<ResourceLocation> finalMissionList = new ArrayList<>();
        Random random = new Random(this.getSeed());

        List<ResourceLocation> eligibleMissions = new ArrayList<>();
        List<ResourceLocation> ineligibleMissions = new ArrayList<>();

        for (ResourceLocation id : MissionManager.getInstance().getAllMissions().keySet()) {
            Mission mission = Mission.getMissionFromId(id);
            if (mission != null) {
                if (mission.getRequiredTier() <= currentTier) { eligibleMissions.add(id); }
                else { ineligibleMissions.add(id); }
            }
        }

        if (eligibleMissions.isEmpty()) {
            System.err.println("CELESTIAL HARVESTING: No missions available for Tier " + currentTier + "! Harvester will have no missions. Please check your mission JSON files.");
            this.dataTag.put(HARVESTER_AVAILABLE_MISSIONS, new ListTag());
            return;
        }

        Collections.shuffle(eligibleMissions, random);
        ResourceLocation guaranteedMission = eligibleMissions.remove(0);
        finalMissionList.add(guaranteedMission);

        List<ResourceLocation> remainingCandidates = new ArrayList<>();
        remainingCandidates.addAll(eligibleMissions);
        remainingCandidates.addAll(ineligibleMissions);
        Collections.shuffle(remainingCandidates, random);

        int remainingToSelect = Math.min(missionCountToSelect - 1, remainingCandidates.size());
        for (int i = 0; i < remainingToSelect; i++) {
            finalMissionList.add(remainingCandidates.get(i));
        }

        ListTag missionListTag = new ListTag();
        for (ResourceLocation missionId : finalMissionList) {
            missionListTag.add(StringTag.valueOf(missionId.toString()));
        }
        this.dataTag.put(HARVESTER_AVAILABLE_MISSIONS, missionListTag);
    }

    public List<ResourceLocation> getAvailableMissions() {
        List<ResourceLocation> missions = new ArrayList<>();
        if (this.dataTag.contains(HARVESTER_AVAILABLE_MISSIONS, Tag.TAG_LIST)) {
            ListTag missionListTag = this.dataTag.getList(HARVESTER_AVAILABLE_MISSIONS, Tag.TAG_STRING);
            for (Tag tag : missionListTag) {
                missions.add(new ResourceLocation(tag.getAsString()));
            }
        }
        return missions;
    }

    public int getModifiedMissionTime(int baseTravelTimeInSeconds) {
        float bestModifier = 1.0f;

        Set<ResourceLocation> unlockedUpgrades = this.getUnlockedUpgrades();

        for (ResourceLocation upgradeId : unlockedUpgrades) {
            UpgradeDefinition def = UpgradeManager.getInstance().getAllUpgrades().get(upgradeId);
            if (def != null && def.speed_modifier().isPresent()) {
                if (def.speed_modifier().get() < bestModifier) { bestModifier = def.speed_modifier().get(); }
            }
        }

        return (int) (baseTravelTimeInSeconds * bestModifier * 20);
    }

    public int getModifiedFuelCost(int baseFuelCost) {
        float bestModifier = 1.0f;

        Set<ResourceLocation> unlockedUpgrades = this.getUnlockedUpgrades();
        Map<ResourceLocation, UpgradeDefinition> allUpgrades = UpgradeManager.getInstance().getAllUpgrades();

        for (ResourceLocation upgradeId : unlockedUpgrades) {
            UpgradeDefinition def = allUpgrades.get(upgradeId);
            if (def != null && def.fuel_modifier().isPresent()) {
                if (def.fuel_modifier().get() < bestModifier) { bestModifier = def.fuel_modifier().get(); }
            }
        }

        return (int) (baseFuelCost * bestModifier);
    }

    public float getDamageNegationChance() {
        float bestChance = 0.0f;

        Set<ResourceLocation> unlockedUpgrades = this.getUnlockedUpgrades();
        Map<ResourceLocation, UpgradeDefinition> allUpgrades = UpgradeManager.getInstance().getAllUpgrades();

        for (ResourceLocation upgradeId : unlockedUpgrades) {
            UpgradeDefinition def = allUpgrades.get(upgradeId);
            if (def != null && def.damage_negation_chance().isPresent()) {
                if (def.damage_negation_chance().get() > bestChance) {
                    bestChance = def.damage_negation_chance().get();
                }
            }
        }
        return bestChance;
    }

    public int getLootRerollCount() {
        int bestRerolls = 0;

        Set<ResourceLocation> unlockedUpgrades = this.getUnlockedUpgrades();
        Map<ResourceLocation, UpgradeDefinition> allUpgrades = UpgradeManager.getInstance().getAllUpgrades();

        for (ResourceLocation upgradeId : unlockedUpgrades) {
            UpgradeDefinition def = allUpgrades.get(upgradeId);
            if (def != null && def.loot_rerolls().isPresent()) {
                if (def.loot_rerolls().get() > bestRerolls) { bestRerolls = def.loot_rerolls().get(); }
            }
        }
        return bestRerolls;
    }

    public int getSolarGenerationRate() {
        int bestRate = 0;

        Set<ResourceLocation> unlockedUpgrades = this.getUnlockedUpgrades();
        Map<ResourceLocation, UpgradeDefinition> allUpgrades = UpgradeManager.getInstance().getAllUpgrades();

        for (ResourceLocation upgradeId : unlockedUpgrades) {
            UpgradeDefinition def = allUpgrades.get(upgradeId);
            if (def != null && def.solar_generation().isPresent()) {
                if (def.solar_generation().get() > bestRate) { bestRate = def.solar_generation().get(); }
            }
        }
        return bestRate;
    }

    private int getMissionCountBonus() {
        int bestBonus = 0;

        Set<ResourceLocation> unlockedUpgrades = this.getUnlockedUpgrades();
        Map<ResourceLocation, UpgradeDefinition> allUpgrades = UpgradeManager.getInstance().getAllUpgrades();

        for (ResourceLocation upgradeId : unlockedUpgrades) {
            UpgradeDefinition def = allUpgrades.get(upgradeId);
            if (def != null && def.mission_bonus().isPresent()) {
                if (def.mission_bonus().get() > bestBonus) { bestBonus = def.mission_bonus().get(); }
            }
        }
        return bestBonus;
    }

}
