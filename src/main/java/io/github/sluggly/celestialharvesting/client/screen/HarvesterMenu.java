package io.github.sluggly.celestialharvesting.client.screen;

import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.init.BlockInit;
import io.github.sluggly.celestialharvesting.init.MenuInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class HarvesterMenu extends AbstractContainerMenu {
    public final Harvester blockEntity;
    private final Level level;
    private final ContainerData data;

    // Server-side constructor
    public HarvesterMenu(int pContainerId, Inventory pPlayerInventory, Harvester pBlockEntity) {
        super(MenuInit.HARVESTER_MENU.get(), pContainerId);
        this.blockEntity = pBlockEntity;
        this.level = pPlayerInventory.player.level();

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> HarvesterMenu.this.blockEntity.getHarvesterData().getCurrentHealth();
                    case 1 -> HarvesterMenu.this.blockEntity.getHarvesterData().getMaxHealth();
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> HarvesterMenu.this.blockEntity.getHarvesterData().setCurrentHealth(pValue);
                    case 1 -> HarvesterMenu.this.blockEntity.getHarvesterData().setMaxHealth(pValue);
                }
            }

            @Override
            public int getCount() { return 2; }
        };

        addPlayerInventory(pPlayerInventory);
        addPlayerHotbar(pPlayerInventory);

        addDataSlots(data);
    }

    // Client-side constructor
    public HarvesterMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf pExtraData) {
        this(pContainerId, pPlayerInventory, getBlockEntity(pPlayerInventory, pExtraData));
    }

    private static Harvester getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
        final BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (blockEntity instanceof Harvester) {
            return (Harvester) blockEntity;
        }
        throw new IllegalStateException("Illegal BlockEntity class at position!" + data.readBlockPos());
    }

    public int getHealth() { return this.data.get(0); }
    public int getMaxHealth() { return this.data.get(1); }


    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockInit.HARVESTER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pButtonId) {
        switch (pButtonId) {
            case 1:
                System.out.println("Server: Upgrades button clicked!");
                return true;
            case 2:
                System.out.println("Server: Repair button clicked!");
                this.blockEntity.getHarvesterData().setCurrentHealth(this.blockEntity.getHarvesterData().getMaxHealth());
                return true;
        }
        return super.clickMenuButton(pPlayer, pButtonId);
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        Slot sourceSlot = this.slots.get(pIndex);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyStack = sourceStack.copy();

        if (pIndex < 36) {
            if (!this.moveItemStackTo(sourceStack, 36, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(sourceStack, 0, 36, false)) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(pPlayer, sourceStack);
        return copyStack;
    }
}