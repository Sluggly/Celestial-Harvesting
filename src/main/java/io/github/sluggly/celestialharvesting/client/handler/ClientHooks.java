package io.github.sluggly.celestialharvesting.client.handler;

import io.github.sluggly.celestialharvesting.admin.Admin;
import io.github.sluggly.celestialharvesting.client.screen.MainScreen;
import io.github.sluggly.celestialharvesting.client.screen.UpgradeScreen;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.mission.MissionManager;
import io.github.sluggly.celestialharvesting.network.StoCPacket;
import io.github.sluggly.celestialharvesting.upgrade.UpgradeManager;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ClientHooks {
    public static void handleActions(StoCPacket msg) {
        String action = msg.action;
        if (Admin.ADMIN_SERVER_CONSOLE_LOG) { System.out.println("Action received : " + action); }
        switch (action) {
            case NBTKeys.ACTION_REFRESH_DATA -> updateNBTData(msg);
            case NBTKeys.ACTION_OPEN_HARVESTER_SCREEN -> openHarvesterScreen(msg);
            case NBTKeys.ACTION_SYNC_MISSIONS -> MissionManager.handleMissionSync(msg);
            case NBTKeys.ACTION_SYNC_UPGRADES -> UpgradeManager.handleUpgradeSync(msg);
            // ADMIN CASES
            //case "LogData" -> ArenaData.logCustomDataClient();
            case "Admin" -> Admin.CLIENT_IS_ADMIN_MODE_ACTIVATED = true;
            case "AdminNo" -> Admin.CLIENT_IS_ADMIN_MODE_ACTIVATED = false;
            case "AdminLog" -> Admin.ADMIN_SERVER_CONSOLE_LOG = true;
            case "AdminLogNo" -> Admin.ADMIN_SERVER_CONSOLE_LOG = false;
            case "AdminInstant" -> Admin.CLIENT_ADMIN_MISSION_INSTANT = true;
            case "AdminInstantNo" -> Admin.CLIENT_ADMIN_MISSION_INSTANT = false;
            case "AdminItems" -> Admin.CLIENT_ADMIN_NO_ITEM_REQUIRED = true;
            case "AdminItemsNo" -> Admin.CLIENT_ADMIN_NO_ITEM_REQUIRED = false;
        }
    }

    private static void openHarvesterScreen(StoCPacket msg) {
        CompoundTag data = msg.data;
        if (data == null || !data.contains(NBTKeys.BLOCK_POS)) return;

        BlockPos pos = BlockPos.of(data.getLong(NBTKeys.BLOCK_POS));
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            BlockEntity be = mc.level.getBlockEntity(pos);
            if (be instanceof Harvester harvester) {
                mc.setScreen(new MainScreen(harvester));
            }
        }
    }

    private static void updateNBTData(StoCPacket msg) {
        CompoundTag data = msg.data;
        if (data == null || !data.contains(NBTKeys.BLOCK_POS)) return;

        BlockPos pos = BlockPos.of(data.getLong(NBTKeys.BLOCK_POS));
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            BlockEntity be = mc.level.getBlockEntity(pos);
            if (be instanceof Harvester harvester) {
                harvester.load(data);
                if (mc.screen instanceof UpgradeScreen upgradeScreen) {
                    if (upgradeScreen.isForHarvester(harvester)) { upgradeScreen.refreshData(); }
                }
            }
        }
    }
}
