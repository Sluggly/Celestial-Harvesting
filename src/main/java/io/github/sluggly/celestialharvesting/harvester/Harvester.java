package io.github.sluggly.celestialharvesting.harvester;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.client.screen.HarvesterMenu;
import io.github.sluggly.celestialharvesting.init.BlockEntityInit;
import io.github.sluggly.celestialharvesting.mission.Mission;
import io.github.sluggly.celestialharvesting.mission.MissionItem;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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

public class Harvester extends BlockEntity implements MenuProvider {
    private HarvesterData harvesterData = new HarvesterData(null);
    private static final int INVENTORY_SIZE = 54;
    private boolean isInternalModification = false;
    private final ItemStackHandler itemHandler = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) { return isInternalModification; }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    public ItemStackHandler getItemHandler() { return this.itemHandler; }

    private static final ResourceLocation SOLAR_PANEL_UPGRADE = new ResourceLocation(CelestialHarvesting.MOD_ID, "integrated_solar_panel");
    private static final int SOLAR_GENERATION_RATE = 10;

    private final EnergyStorage energyStorage = new EnergyStorage(20000, 512, 0) {
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
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    public Harvester(BlockPos pPos, BlockState pBlockState) { super(BlockEntityInit.HARVESTER.get(), pPos, pBlockState); }

    @Override
    public @NotNull Component getDisplayName() { return Component.translatable("block.celestialharvesting.harvester"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new HarvesterMenu(pContainerId, pPlayerInventory, this);
    }

    public HarvesterData getHarvesterData() { return harvesterData; }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, Harvester pBlockEntity) {
        if (pLevel.isClientSide()) { return; }

        if (pBlockEntity.getHarvesterData().getStatus().equals(NBTKeys.HARVESTER_ONGOING)) {
            int timeLeft = pBlockEntity.getHarvesterData().getMissionTimeLeft();
            timeLeft--;
            pBlockEntity.getHarvesterData().setMissionTimeLeft(timeLeft);

            if (timeLeft <= 0) { pBlockEntity.completeMission(); }
            else {
                if (timeLeft % 20 == 0) { pBlockEntity.setChanged(); }
            }
        }
        else {
            if (pBlockEntity.getHarvesterData().hasUpgrade(SOLAR_PANEL_UPGRADE)) {
                // Only generate power if idle (don't generate during missions) and not full
                if (pBlockEntity.getHarvesterData().getStatus().equals(NBTKeys.HARVESTER_STATUS_IDLE) &&
                        pBlockEntity.energyStorage.getEnergyStored() < pBlockEntity.energyStorage.getMaxEnergyStored()) {

                    // Check for daytime and sky access
                    if (pLevel.isDay() && pLevel.canSeeSky(pPos.above())) {
                        pBlockEntity.energyStorage.receiveEnergy(SOLAR_GENERATION_RATE, false);
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
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("HarvesterData")) { harvesterData = new HarvesterData(pTag.getCompound("HarvesterData")); }
        if (pTag.contains("inventory")) { itemHandler.deserializeNBT(pTag.getCompound("inventory")); }
        if (pTag.contains("energy", Tag.TAG_INT)) { energyStorage.deserializeNBT(pTag.get("energy")); }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public @NotNull CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    public int getEnergyStored() { return this.energyStorage.getEnergyStored(); }
    public int getMaxEnergyStored() { return this.energyStorage.getMaxEnergyStored(); }
    public void consumeEnergy(int amount) {
        this.energyStorage.extractEnergy(amount, false);
        setChanged();
        if(level != null) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
    }

    public void completeMission() {
        String missionIdStr = this.harvesterData.getActiveMissionID();
        if (missionIdStr.isEmpty()) return;

        Mission mission = Mission.getMissionFromId(new ResourceLocation(missionIdStr));
        if (mission == null) return;

        int currentHealth = this.harvesterData.getCurrentHealth();
        this.harvesterData.setCurrentHealth(currentHealth - mission.getDamage());

        if (this.harvesterData.getCurrentHealth() <= 0) {
            for (int i = 0; i < this.itemHandler.getSlots(); i++) { this.itemHandler.setStackInSlot(i, ItemStack.EMPTY); }
        }
        else {
            try {
                this.isInternalModification = true;
                for (MissionItem reward : mission.getRewards()) {
                    ItemHandlerHelper.insertItemStacked(this.itemHandler, reward.toItemStack(), false);
                }
            }
            finally {
                this.isInternalModification = false;
            }
        }

        this.harvesterData.setStatus(NBTKeys.HARVESTER_IDLE);
        this.harvesterData.setActiveMissionID("");
        this.harvesterData.setMissionTimeLeft(0);

        setChanged();
        if (level != null) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
    }


}
