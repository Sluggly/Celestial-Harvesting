package io.github.sluggly.celestialharvesting.client.handler;

import io.github.sluggly.celestialharvesting.admin.Admin;
import io.github.sluggly.celestialharvesting.network.StoCPacket;

public class ClientHooks {
    public static void handleActions(StoCPacket msg) {
        String action = msg.action;
        if (Admin.ADMIN_SERVER_CONSOLE_LOG) { System.out.println("Action received : " + action); }
        switch (action) {
            //case NBTKeys.ACTION_REFRESH_DATA -> updateNBTData(msg);
            //case NBTKeys.ACTION_OPEN_MAIN_DIALOG_SCREEN -> {
            //    updateNBTData(msg);
            //    openMainDialogScreen();
            //}
            //case NBTKeys.ACTION_OPEN_TEAM_SELECTION_SCREEN -> {
            //    updateNBTData(msg);
            //    openTeamSelectionScreen();
            //}
            //case NBTKeys.ACTION_OPEN_BET_SCREEN -> {
            //    updateNBTData(msg);
            //    openBetScreen();
            //}
            //case NBTKeys.ACTION_OPEN_LEADERBOARD_SCREEN -> {
            //    updateNBTData(msg);
            //    openLeaderboardScreen();
            //}
            //case NBTKeys.ACTION_OPEN_HISTORY_SCREEN -> {
            //    updateNBTData(msg);
            //    openHistoryScreen();
            //}
            //// ADMIN CASES
            //case "LogData" -> ArenaData.logCustomDataClient();
            case "Admin" -> Admin.CLIENT_IS_ADMIN_MODE_ACTIVATED = true;
            case "AdminNo" -> Admin.CLIENT_IS_ADMIN_MODE_ACTIVATED = false;
            case "AdminLog" -> Admin.ADMIN_SERVER_CONSOLE_LOG = true;
            case "AdminLogNo" -> Admin.ADMIN_SERVER_CONSOLE_LOG = false;
        }
    }
}
