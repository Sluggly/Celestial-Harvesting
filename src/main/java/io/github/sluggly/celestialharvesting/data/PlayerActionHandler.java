package io.github.sluggly.celestialharvesting.data;

import io.github.sluggly.celestialharvesting.admin.Admin;
import io.github.sluggly.celestialharvesting.network.CtoSPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerActionHandler {
    // Server only
    public static void handlePlayerAction(@NotNull CtoSPacket message, ServerPlayer player) {
        String action = message.action;
        if (Admin.ADMIN_SERVER_CONSOLE_LOG) { System.out.println("Received action: " + action + " from player: " + player.getScoreboardName()); }
        CompoundTag data = message.data;
        switch (action) {

        }
    }
}
