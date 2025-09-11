package io.github.sluggly.celestialharvesting.data;

import io.github.sluggly.celestialharvesting.admin.Admin;
import io.github.sluggly.celestialharvesting.client.screen.HarvesterInventoryMenu;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.network.CtoSPacket;
import io.github.sluggly.celestialharvesting.network.PacketHandler;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class PlayerActionHandler {
    // Server only
    public static void handlePlayerAction(@NotNull CtoSPacket message, ServerPlayer player) {
        String action = message.action;
        if (Admin.ADMIN_SERVER_CONSOLE_LOG) { System.out.println("Received action: " + action + " from player: " + player.getScoreboardName()); }
        CompoundTag data = message.data;
        switch (action) {
            case NBTKeys.ACTION_REPAIR_HARVESTER -> {
                if (data.contains(NBTKeys.BLOCK_POS)) {
                    BlockPos pos = BlockPos.of(data.getLong(NBTKeys.BLOCK_POS));
                    BlockEntity be = player.level().getBlockEntity(pos);
                    if (be instanceof Harvester harvester) {
                        harvester.getHarvesterData().setCurrentHealth(harvester.getHarvesterData().getMaxHealth());
                        harvester.setChanged();
                        CompoundTag payload = new CompoundTag();
                        payload.put("HarvesterData", harvester.getHarvesterData().dataTag);
                        payload.putLong(NBTKeys.BLOCK_POS, pos.asLong());
                        PacketHandler.sendToPlayer(NBTKeys.ACTION_REFRESH_DATA, payload, player);
                    }
                }
            }
            case NBTKeys.ACTION_OPEN_HARVESTER_INVENTORY -> {
                if (data.contains(NBTKeys.BLOCK_POS)) {
                    BlockPos pos = BlockPos.of(data.getLong(NBTKeys.BLOCK_POS));
                    BlockEntity be = player.level().getBlockEntity(pos);
                    if (be instanceof Harvester harvester) {
                        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                                (id, inv, p) -> new HarvesterInventoryMenu(id, inv, harvester),
                                harvester.getDisplayName()
                        ), pos);
                    }
                }
            }
        }
    }
}
