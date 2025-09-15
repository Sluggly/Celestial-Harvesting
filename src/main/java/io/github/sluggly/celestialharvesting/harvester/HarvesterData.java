package io.github.sluggly.celestialharvesting.harvester;

import io.github.sluggly.celestialharvesting.utils.Utils;
import net.minecraft.nbt.CompoundTag;

import java.util.Random;

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

}
