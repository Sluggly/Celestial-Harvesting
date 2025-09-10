package io.github.sluggly.celestialharvesting.admin;

import io.github.sluggly.celestialharvesting.network.PacketHandler;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;

public class Admin {

    public static boolean ADMIN_SERVER_CONSOLE_LOG = false;
    public static boolean ADMIN_RESET_ON_LOGIN = false;

    // CLIENT SIDE
    public static boolean CLIENT_IS_ADMIN_MODE_ACTIVATED = false;
    public static boolean CLIENT_ADMIN_NO_ITEM_REQUIRED = false;
    public static boolean CLIENT_ADMIN_MISSION_INSTANT = false;

    public static class Admin_Config {
        public boolean ADMIN_MISSION_INSTANT = false;
        public boolean ADMIN_NO_ITEM_REQUIRED = false;
        public Admin_Config() {}
    }

    public static HashMap<ServerPlayer,Admin_Config> administratorsMap = new HashMap<>();

    public static boolean isPlayerAdministrator(ServerPlayer player) {
        return administratorsMap.containsKey(player); }


    public static boolean hasPlayerNoRequiredItem(ServerPlayer player) {
        if (!isPlayerAdministrator(player)) { return false; }
        return administratorsMap.get(player).ADMIN_NO_ITEM_REQUIRED;
    }

    public static boolean arePlayerMissionsInstant(ServerPlayer player) {
        if (!isPlayerAdministrator(player)) { return false; }
        return administratorsMap.get(player).ADMIN_MISSION_INSTANT;
    }

    public static void toggleServerConsoleLog(ServerPlayer player) {
        ADMIN_SERVER_CONSOLE_LOG = !ADMIN_SERVER_CONSOLE_LOG;
        if (ADMIN_SERVER_CONSOLE_LOG) { PacketHandler.sendToPlayer("AdminLog",player); }
        else { PacketHandler.sendToPlayer("AdminLogNo",player); }
        PacketHandler.sendToPlayer(NBTKeys.ACTION_REFRESH_DATA,player);
    }

    // Server
    public static void playerRequestError(Player player, String errorMessage) {
        if (Admin.ADMIN_SERVER_CONSOLE_LOG) {
            String callingMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            System.out.println("Error from the player " + player.getScoreboardName() + " : " + errorMessage + " (called from " + callingMethodName + ")");
        }
    }

    // Server Admin only
    public static void adminItems(ServerPlayer player) {
        if (Admin.isPlayerAdministrator(player)) {
            administratorsMap.get(player).ADMIN_NO_ITEM_REQUIRED = !administratorsMap.get(player).ADMIN_NO_ITEM_REQUIRED;
            if (administratorsMap.get(player).ADMIN_NO_ITEM_REQUIRED) { PacketHandler.sendToPlayer("AdminItems",player); }
            else { PacketHandler.sendToPlayer("AdminItemsNo",player); }
            PacketHandler.sendToPlayer(NBTKeys.ACTION_REFRESH_DATA,player);
        }
    }

    // Server Admin only
    public static void adminInstant(ServerPlayer player) {
        if (Admin.isPlayerAdministrator(player)) {
            administratorsMap.get(player).ADMIN_MISSION_INSTANT = !administratorsMap.get(player).ADMIN_MISSION_INSTANT;
            if (administratorsMap.get(player).ADMIN_MISSION_INSTANT) {
                PacketHandler.sendToPlayer("AdminInstant",player);
            }
            else { PacketHandler.sendToPlayer("AdminInstantNo",player); }
            PacketHandler.sendToPlayer(NBTKeys.ACTION_REFRESH_DATA,player);
        }
    }

    // Server Admin only
    public static void togglePlayerAdministrator(ServerPlayer player) {
        if (administratorsMap.containsKey(player)) { administratorsMap.remove(player); }
        else { administratorsMap.put(player, new Admin_Config()); }
        if (administratorsMap.containsKey(player)) { PacketHandler.sendToPlayer("Admin",player); }
        else { PacketHandler.sendToPlayer("AdminNo",player); }
        if (Admin.ADMIN_SERVER_CONSOLE_LOG) { System.out.println("Toggled admin for player: " + player.getScoreboardName()); }
        PacketHandler.sendToPlayer(NBTKeys.ACTION_REFRESH_DATA,player);
    }
}
