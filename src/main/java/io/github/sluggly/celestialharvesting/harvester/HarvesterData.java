package io.github.sluggly.celestialharvesting.harvester;

import io.github.sluggly.celestialharvesting.utils.Utils;
import net.minecraft.nbt.CompoundTag;

import static io.github.sluggly.celestialharvesting.utils.NBTKeys.*;

public class HarvesterData {
    public CompoundTag dataTag;

    public HarvesterData(CompoundTag tag) {
        if (tag == null) {
            this.dataTag = new CompoundTag();
            this.dataTag.putInt(HARVESTER_CURRENT_HEALTH, 100);
            this.dataTag.putInt(HARVESTER_MAX_HEALTH, 100);
            this.dataTag.putInt(HARVESTER_SEED, Utils.generateSeed());
        }
        else { this.dataTag = tag; }
    }

    public int getCurrentHealth() { return this.dataTag.getInt(HARVESTER_CURRENT_HEALTH); }
    public int getMaxHealth() { return this.dataTag.getInt(HARVESTER_MAX_HEALTH); }
    public void setCurrentHealth(int health) { this.dataTag.putInt(HARVESTER_CURRENT_HEALTH, health); }
    public void setMaxHealth(int health) { this.dataTag.putInt(HARVESTER_MAX_HEALTH, health); }
    public int getSeed() { return this.dataTag.getInt(HARVESTER_SEED); }
    public void generateSeed() { this.dataTag.putInt(HARVESTER_SEED, Utils.generateSeed()); }

}
