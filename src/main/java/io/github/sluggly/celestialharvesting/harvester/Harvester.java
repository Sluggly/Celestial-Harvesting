package io.github.sluggly.celestialharvesting.harvester;

import io.github.sluggly.celestialharvesting.admin.Admin;
import io.github.sluggly.celestialharvesting.client.screen.HarvesterMenu;
import io.github.sluggly.celestialharvesting.init.BlockEntityInit;
import io.github.sluggly.celestialharvesting.mission.Mission;
import io.github.sluggly.celestialharvesting.mission.MissionItem;
import io.github.sluggly.celestialharvesting.upgrade.UpgradeDefinition;
import io.github.sluggly.celestialharvesting.upgrade.UpgradeManager;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Harvester extends BlockEntity implements MenuProvider {
    private HarvesterData harvesterData = new HarvesterData(null);
    private boolean isInternalModification = false;
    private ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    public ItemStackHandler getItemHandler() { return this.itemHandler; }

    private final Random random = new Random();

    public static final int BASE_ENERGY_CAPACITY = 20000;
    private EnergyStorage energyStorage; // No longer final
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    public Harvester(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityInit.HARVESTER.get(), pPos, pBlockState);
        this.itemHandler = createItemHandler(1);
        this.recalculateEnergyStorage(0);
    }

    private ItemStackHandler createItemHandler(int rows) {
        return new ItemStackHandler(rows * 9) {
            @Override
            protected void onContentsChanged(int slot) { setChanged(); }
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) { return isInternalModification; }
        };
    }

    public void recalculateEnergyStorage(int currentEnergy) {
        int newCapacity = BASE_ENERGY_CAPACITY;

        // Loop through all unlocked upgrades and SUM the bonuses
        Set<ResourceLocation> unlockedUpgrades = this.harvesterData.getUnlockedUpgrades();
        Map<ResourceLocation, UpgradeDefinition> allUpgrades = UpgradeManager.getInstance().getAllUpgrades();
        for (ResourceLocation upgradeId : unlockedUpgrades) {
            UpgradeDefinition def = allUpgrades.get(upgradeId);
            if (def != null && def.energy_capacity_bonus().isPresent()) {
                newCapacity += def.energy_capacity_bonus().get();
            }
        }

        // Create the new storage object
        this.energyStorage = new EnergyStorage(newCapacity, 512, 0) {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int received = super.receiveEnergy(maxReceive, simulate);
                if (received > 0 && !simulate) {
                    setChanged();
                    if(level != null) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
                }
                return received;
            }
        };

        // Set the energy to the previous amount, capped by the new capacity
        this.energyStorage.receiveEnergy(currentEnergy, false);

        // Invalidate the capability so other mods see the change
        this.lazyEnergyHandler.invalidate();
        this.lazyEnergyHandler = LazyOptional.of(() -> this.energyStorage);
    }

    @Override
    public @NotNull Component getDisplayName() { return Component.translatable("block.celestialharvesting.harvester"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new HarvesterMenu(pContainerId, pPlayerInventory, this);
    }

    public HarvesterData getHarvesterData() { return harvesterData; }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, Harvester pBlockEntity) {
        if (pLevel.isClientSide()) {
            if (pBlockEntity.animationState != AnimationState.NONE) {
                pBlockEntity.animationTick++;
                float yOffset = pBlockEntity.getAnimationYOffset(0);
                pBlockEntity.spawnAnimationParticles(yOffset);
            }
            return;
        }

        // Taking off animation
        if (pBlockEntity.animationState == AnimationState.TAKING_OFF) {
            pBlockEntity.animationTick++;
            if (pBlockEntity.animationTick >= ANIMATION_DURATION) {
                pBlockEntity.animationState = AnimationState.NONE;
                pBlockEntity.getHarvesterData().setStatus(NBTKeys.HARVESTER_ONGOING);
            }
            pBlockEntity.setChanged();
            return;
        }

        // Landing animation
        if (pBlockEntity.animationState == AnimationState.LANDING) {
            pBlockEntity.animationTick++;
            if (pBlockEntity.animationTick >= ANIMATION_DURATION) {
                pBlockEntity.animationState = AnimationState.NONE;
                pBlockEntity.finishMissionCompletion();
            }
            pBlockEntity.setChanged();
            return;
        }

        if (pBlockEntity.getHarvesterData().getStatus().equals(NBTKeys.HARVESTER_ONGOING)) {
            int timeLeft = pBlockEntity.getHarvesterData().getMissionTimeLeft();
            timeLeft--;
            pBlockEntity.getHarvesterData().setMissionTimeLeft(timeLeft);

            if (timeLeft <= 0) { pBlockEntity.startLandingSequence(); }
            else if (timeLeft % 20 == 0) { pBlockEntity.setChanged(); }
        }
        else {
            int solarRate = pBlockEntity.harvesterData.getSolarGenerationRate();
            if (solarRate > 0) {
                if (pBlockEntity.energyStorage.getEnergyStored() < pBlockEntity.energyStorage.getMaxEnergyStored()) {
                    if (pLevel.isDay() && pLevel.canSeeSky(pPos.above())) {
                        pBlockEntity.energyStorage.receiveEnergy(solarRate, false);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) { return lazyEnergyHandler.cast(); }
        if (cap == ForgeCapabilities.ITEM_HANDLER) { return lazyItemHandler.cast(); }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergyHandler = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        pTag.put("HarvesterData", harvesterData.dataTag);
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.put("energy", energyStorage.serializeNBT());
        pTag.putInt("animationState", animationState.ordinal());
        pTag.putInt("animationTick", animationTick);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("HarvesterData")) { this.harvesterData = new HarvesterData(pTag.getCompound("HarvesterData")); }
        this.itemHandler = createItemHandler(this.harvesterData.getInventoryRows());
        if (pTag.contains("inventory")) { itemHandler.deserializeNBT(pTag.getCompound("inventory")); }
        recalculateEnergyStorage(0);
        if (pTag.contains("energy", Tag.TAG_INT)) { energyStorage.deserializeNBT(pTag.get("energy")); }
        if (pTag.contains("animationState")) { animationState = AnimationState.values()[pTag.getInt("animationState")]; }
        if (pTag.contains("animationTick")) { animationTick = pTag.getInt("animationTick"); }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        tag.putInt("animationState", this.animationState.ordinal());
        tag.putInt("animationTick", this.animationTick);
        return tag;
    }
    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) { handleUpdateTag(tag); }
    }
    public int getEnergyStored() { return this.energyStorage.getEnergyStored(); }
    public int getMaxEnergyStored() { return this.energyStorage.getMaxEnergyStored(); }
    public void consumeEnergy(int amount) {
        this.energyStorage.extractEnergy(amount, false);
        setChanged();
        if(level != null) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
    }

    public enum AnimationState { NONE, TAKING_OFF, LANDING }
    private AnimationState animationState = AnimationState.NONE;
    private int animationTick = 0;
    public static final int ANIMATION_DURATION = 100;
    public static final float ANIMATION_HEIGHT = 100.0f;
    public AnimationState getAnimationState() { return this.animationState; }
    public int getAnimationTick() { return this.animationTick; }

    public void startMissionSequence(Mission mission, ServerPlayer player) {
        if (this.level == null || this.level.isClientSide()) return;

        int modifiedFuelCost = this.harvesterData.getModifiedFuelCost(mission.getFuelCost());
        if (getHarvesterData().getStatus().equals(NBTKeys.HARVESTER_IDLE) && getEnergyStored() >= modifiedFuelCost) {
            consumeEnergy(modifiedFuelCost);
            getHarvesterData().setActiveMissionID(mission.getId().toString());

            int missionTime = this.harvesterData.getModifiedMissionTime(mission.getTravelTime());
            if (Admin.arePlayerMissionsInstant(player)) { missionTime = 3; }

            getHarvesterData().setMissionTimeLeft(missionTime);

            this.animationState = AnimationState.TAKING_OFF;
            this.animationTick = 0;

            BlockState currentState = this.level.getBlockState(this.worldPosition);
            if (currentState.is(this.getBlockState().getBlock())) {
                this.level.setBlock(this.worldPosition, currentState.setValue(HarvesterBlock.STATE, HarvesterBlock.State.IN_MISSION), 3);
            }

            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void startLandingSequence() {
        if (level == null || level.isClientSide()) return;

        level.setBlock(worldPosition, getBlockState().setValue(HarvesterBlock.STATE, HarvesterBlock.State.IDLE), 3);

        this.animationState = AnimationState.LANDING;
        this.animationTick = 0;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void finishMissionCompletion() {
        String missionIdStr = this.harvesterData.getActiveMissionID();
        if (missionIdStr.isEmpty()) return;

        Mission mission = Mission.getMissionFromId(new ResourceLocation(missionIdStr));
        if (mission == null) return;

        float negationChance = this.harvesterData.getDamageNegationChance();
        int damageToApply = mission.getDamage();

        if (this.random.nextFloat() < negationChance) { damageToApply = 0; }

        int currentHealth = this.harvesterData.getCurrentHealth();
        this.harvesterData.setCurrentHealth(currentHealth - damageToApply);

        if (this.harvesterData.getCurrentHealth() <= 0) {
            for (int i = 0; i < this.itemHandler.getSlots(); i++) { this.itemHandler.setStackInSlot(i, ItemStack.EMPTY); }
        }
        else {
            int totalRolls = 1 + this.harvesterData.getLootRerollCount();
            for (int i = 0; i < totalRolls; i++) {
                try {
                    this.isInternalModification = true;
                    for (MissionItem reward : mission.getRewards()) {
                        if (reward.chance().isEmpty() || this.random.nextDouble() < reward.chance().get()) {
                            ItemHandlerHelper.insertItemStacked(this.itemHandler, reward.getRandomizedStack(this.random), false);
                        }
                    }
                }
                finally { this.isInternalModification = false; }
            }
        }

        this.harvesterData.setStatus(NBTKeys.HARVESTER_IDLE);
        this.harvesterData.setActiveMissionID("");
        this.harvesterData.setMissionTimeLeft(0);

        this.harvesterData.generateSeed();
        this.harvesterData.rerollMissions();

        setChanged();
        if (level != null) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
    }

    public float getAnimationYOffset(float partialTick) {
        if (this.animationState == AnimationState.NONE) { return 0.0f; }

        float animTicks = this.animationTick + partialTick;
        float progress = Math.min(animTicks / (float) ANIMATION_DURATION, 1.0f);

        float easedProgress = progress * progress;

        float yOffset = 0;
        if (this.animationState == AnimationState.TAKING_OFF) { yOffset = easedProgress * ANIMATION_HEIGHT; }
        else if (this.animationState == AnimationState.LANDING) {
            float landingProgress = 1.0f - progress;
            easedProgress = 1.0f - (landingProgress * landingProgress);
            yOffset = (1.0f - easedProgress) * ANIMATION_HEIGHT;
        }
        return yOffset;
    }

    private void spawnAnimationParticles(float yOffset) {
        if (this.level == null || !this.level.isClientSide() || this.animationState == AnimationState.NONE) { return; }

        double centerX = this.worldPosition.getX() + 0.5;
        double groundY = this.worldPosition.getY() + yOffset + 0.1;
        double centerZ = this.worldPosition.getZ() + 0.5;

        for (int i = 0; i < 4; i++) {
            double px = centerX + (this.random.nextDouble() - 0.5) * 0.9;
            double pz = centerZ + (this.random.nextDouble() - 0.5) * 0.9;

            double vx = (this.random.nextDouble() - 0.5) * 0.05;
            double vy = -0.1;
            double vz = (this.random.nextDouble() - 0.5) * 0.05;

            this.level.addParticle(ParticleTypes.LARGE_SMOKE, px, groundY, pz, vx, vy, vz);
            this.level.addParticle(ParticleTypes.FLAME, px, groundY, pz, 0, vy, 0);
        }
    }

    public void applyInventoryUpgrade(int bonusRows) {
        if (this.level == null || this.level.isClientSide()) return;

        int currentRows = this.harvesterData.getInventoryRows();
        int newRows = Math.min(6, currentRows + bonusRows);
        if (newRows <= currentRows) return;

        ItemStackHandler newHandler = createItemHandler(newRows);

        for (int i = 0; i < this.itemHandler.getSlots(); i++) { newHandler.setStackInSlot(i, this.itemHandler.getStackInSlot(i)); }

        this.itemHandler = newHandler;
        this.harvesterData.setInventoryRows(newRows);

        this.lazyItemHandler.invalidate();
        this.lazyItemHandler = LazyOptional.of(() -> this.itemHandler);

        setChanged();
    }


}
