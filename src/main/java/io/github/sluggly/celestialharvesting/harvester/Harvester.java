package io.github.sluggly.celestialharvesting.harvester;

import io.github.sluggly.celestialharvesting.client.screen.HarvesterMenu;
import io.github.sluggly.celestialharvesting.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Harvester extends BlockEntity implements MenuProvider {
    private HarvesterData harvesterData = new HarvesterData(null);

    public Harvester(BlockPos pPos, BlockState pBlockState) { super(BlockEntityInit.HARVESTER.get(), pPos, pBlockState); }

    @Override
    public @NotNull Component getDisplayName() { return Component.translatable("block.celestialharvesting.harvester"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new HarvesterMenu(pContainerId, pPlayerInventory, this);
    }

    public HarvesterData getHarvesterData() { return harvesterData; }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        pTag.put("HarvesterData", harvesterData.dataTag);
        super.saveAdditional(pTag);
    }


    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("HarvesterData")) {
            harvesterData = new HarvesterData(pTag.getCompound("HarvesterData"));
        }
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, Harvester pBlockEntity) {
        // All core logic (like counting down a timer) should ONLY run on the server side.
        if (pLevel.isClientSide()) {
            // Client-side logic here (e.g., handling animations, spawning particles)
        } else {
            // Server-side logic here (e.g., checking if a mission is active, counting down the timer,
            // completing the mission and adding resources, etc.)

            // Example:
            // if (pBlockEntity.harvesterData.isMissionActive()) {
            //     pBlockEntity.harvesterData.tickDownTimer();
            //     if (pBlockEntity.harvesterData.isMissionComplete()) {
            //         pBlockEntity.completeMission();
            //         // Mark the block entity as dirty to ensure its new state is saved.
            //         setChanged(pLevel, pPos, pState);
            //     }
            // }
        }
    }

}
