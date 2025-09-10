package io.github.sluggly.celestialharvesting.utils;

import io.github.sluggly.celestialharvesting.admin.Admin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class Utils {

    // Client and Server
    public static boolean hasEnoughItems(Item item, Player player, int requiredAmount) {
        if (player.level().isClientSide) {
            if (Admin.CLIENT_ADMIN_NO_ITEM_REQUIRED) { return true; }
        }
        else {
            if (Admin.hasPlayerNoRequiredItem((ServerPlayer) player)) { return true; }
        }
        return countItemInInventory(item, player) >= requiredAmount;
    }

    // Client and Server
    public static int countItemInInventory(Item item, Player player) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack it = player.getInventory().getItem(i);
            if (it.getItem().equals(item)) {
                if (!it.isDamaged() && !it.isEnchanted()) {
                    total += it.getCount();
                }
            }
        }
        return total;
    }

    // Server only /!\ Does not check if enough in inventory /!\
    public static void removeItemInInventory(Item item, int number, Player player) {
        if (Admin.hasPlayerNoRequiredItem((ServerPlayer) player)) { return; }
        int toRemove = number;
        for (int i=0;i<player.getInventory().getContainerSize();i++) {
            ItemStack it = player.getInventory().getItem(i);
            if (it.getItem().equals(item)) {
                if ((!it.isDamaged())&&(!it.isEnchanted())) {
                    int currentCount = it.getCount();
                    if (currentCount > toRemove) {
                        it.setCount(currentCount-toRemove);
                        player.getInventory().setItem(i,it);
                        break;
                    }
                    else {
                        toRemove -= it.getCount();
                        player.getInventory().setItem(i,ItemStack.EMPTY);
                        if (toRemove == 0) { break; }
                    }
                }
            }
        }
        player.getInventory().setChanged();
    }

    public static int generateSeed() {
        Random random = new Random();
        return random.nextInt(100000)+1000;
    }
}
