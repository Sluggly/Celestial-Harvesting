package io.github.sluggly.celestialharvesting.client.screen;

import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.init.BlockInit;
import io.github.sluggly.celestialharvesting.init.MenuInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class HarvesterInventoryMenu extends AbstractContainerMenu {
    public final Harvester blockEntity;

    public HarvesterInventoryMenu(int pContainerId, Inventory pPlayerInventory, Harvester pBlockEntity) {
        super(MenuInit.HARVESTER_INVENTORY_MENU.get(), pContainerId);
        this.blockEntity = pBlockEntity;

        // Harvester Inventory Slots (6 rows of 9)
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }

        addPlayerInventory(pPlayerInventory);
        addPlayerHotbar(pPlayerInventory);
    }

    public HarvesterInventoryMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf pExtraData) {
        this(pContainerId, pPlayerInventory, getBlockEntity(pPlayerInventory, pExtraData));
    }

    private static Harvester getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
        final BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (blockEntity instanceof Harvester) {
            return (Harvester) blockEntity;
        }
        throw new IllegalStateException("Illegal BlockEntity class at position!" + data.readBlockPos());
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(pPlayer.level(), blockEntity.getBlockPos()), pPlayer, BlockInit.HARVESTER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 140 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 198));
        }
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        Slot sourceSlot = this.slots.get(pIndex);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyStack = sourceStack.copy();

        int harvesterInvSize = 54;
        int playerInvStart = harvesterInvSize;
        int playerInvEnd = this.slots.size();

        if (pIndex < harvesterInvSize) {
            if (!this.moveItemStackTo(sourceStack, playerInvStart, playerInvEnd, true)) {
                return ItemStack.EMPTY;
            }
        }
        else { return ItemStack.EMPTY; }

        if (sourceStack.getCount() == 0) { sourceSlot.set(ItemStack.EMPTY); }
        else { sourceSlot.setChanged(); }
        sourceSlot.onTake(pPlayer, sourceStack);
        return copyStack;
    }
}