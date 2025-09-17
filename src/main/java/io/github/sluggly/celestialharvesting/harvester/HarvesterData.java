package io.github.sluggly.celestialharvesting.harvester;

import io.github.sluggly.celestialharvesting.mission.Mission;
import io.github.sluggly.celestialharvesting.mission.MissionManager;
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

    public int getNumberOfMissions(int seed) {
        int minimum = 1;
        int maximum = 3;
        Random random = new Random(seed);
        int value = random.nextInt(maximum - minimum) + minimum;
        return value;
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

}
