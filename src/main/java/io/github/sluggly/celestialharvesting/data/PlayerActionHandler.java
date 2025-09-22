package io.github.sluggly.celestialharvesting.data;

import io.github.sluggly.celestialharvesting.admin.Admin;
import io.github.sluggly.celestialharvesting.client.screen.HarvesterInventoryMenu;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.harvester.HarvesterBlock;
import io.github.sluggly.celestialharvesting.mission.Mission;
import io.github.sluggly.celestialharvesting.mission.MissionItem;
import io.github.sluggly.celestialharvesting.network.CtoSPacket;
import io.github.sluggly.celestialharvesting.network.PacketHandler;
import io.github.sluggly.celestialharvesting.upgrade.UpgradeDefinition;
import io.github.sluggly.celestialharvesting.upgrade.UpgradeManager;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import io.github.sluggly.celestialharvesting.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
            case NBTKeys.ACTION_START_MISSION -> {
                if (data.contains(NBTKeys.BLOCK_POS) && data.contains(NBTKeys.HARVESTER_ACTIVE_MISSION_ID)) {
                    BlockPos pos = BlockPos.of(data.getLong(NBTKeys.BLOCK_POS));
                    ResourceLocation missionId = new ResourceLocation(data.getString(NBTKeys.HARVESTER_ACTIVE_MISSION_ID));

                    BlockEntity be = player.level().getBlockEntity(pos);
                    if (be instanceof Harvester harvester) {
                        Mission mission = Mission.getMissionFromId(missionId);
                        if (mission == null) return;
                        harvester.startMissionSequence(mission, player);
                    }
                }
            }
            case NBTKeys.ACTION_UNLOCK_UPGRADE -> {
                if (data.contains(NBTKeys.BLOCK_POS) && data.contains(NBTKeys.UPGRADE_ID)) {
                    BlockPos pos = BlockPos.of(data.getLong(NBTKeys.BLOCK_POS));
                    ResourceLocation upgradeId = new ResourceLocation(data.getString(NBTKeys.UPGRADE_ID));

                    BlockEntity be = player.level().getBlockEntity(pos);
                    UpgradeDefinition def = UpgradeManager.getInstance().getAllUpgrades().get(upgradeId);

                    if (be instanceof Harvester harvester && def != null) {
                        if (harvester.getHarvesterData().hasUpgrade(upgradeId)) return;

                        for (String reqIdStr : def.requirements()) {
                            if (!harvester.getHarvesterData().hasUpgrade(new ResourceLocation(reqIdStr))) return;
                        }

                        for (MissionItem item : def.cost()) {
                            if (!Utils.hasEnoughItems(item.item().asItem(), player, item.count())) return;
                        }

                        for (MissionItem item : def.cost()) {
                            Utils.removeItemInInventory(item.item().asItem(), item.count(), player);
                        }

                        def.grantsTier().ifPresent(newTier -> {
                            if (newTier > harvester.getHarvesterData().getTier()) {
                                harvester.getHarvesterData().setTier(newTier);
                            }
                        });

                        def.inventory_rows().ifPresent(harvester::applyInventoryUpgrade);

                        if (def.energy_capacity_bonus().isPresent()) {
                            harvester.recalculateEnergyStorage(harvester.getEnergyStored());
                        }

                        harvester.getHarvesterData().addUpgrade(upgradeId);
                        harvester.setChanged();

                        BlockState currentState = player.level().getBlockState(pos);
                        if (!currentState.getValue(HarvesterBlock.UPGRADED)) {
                            player.level().setBlock(pos, currentState.setValue(HarvesterBlock.UPGRADED, true), 3);
                        }

                        CompoundTag payload = new CompoundTag();
                        payload.put("HarvesterData", harvester.getHarvesterData().dataTag);
                        payload.putLong(NBTKeys.BLOCK_POS, pos.asLong());
                        PacketHandler.sendToPlayer(NBTKeys.ACTION_REFRESH_DATA, payload, player);
                    }
                }
            }
        }
    }
}
