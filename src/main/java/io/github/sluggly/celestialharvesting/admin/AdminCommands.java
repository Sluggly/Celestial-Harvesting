package io.github.sluggly.celestialharvesting.admin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class AdminCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("TM_adminMode").requires(source -> source.hasPermission(2)).executes(AdminCommands::toggleAdminModeCommand));
        dispatcher.register(Commands.literal("TM_serverConsoleLog").requires(source -> source.hasPermission(2)).executes(AdminCommands::toggleAdminServerConsoleLogCommand));
        dispatcher.register(Commands.literal("TM_adminInstant").requires(source -> source.hasPermission(2)).executes(AdminCommands::toggleAdminInstantCommand));
        dispatcher.register(Commands.literal("TM_adminItems").requires(source -> source.hasPermission(2)).executes(AdminCommands::toggleAdminItemsCommand));
    }

    private static int toggleAdminModeCommand(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        System.out.println("Toggling player administrator: " + command.getSource().getPlayer());
        Admin.togglePlayerAdministrator(command.getSource().getPlayerOrException());
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleAdminServerConsoleLogCommand(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        System.out.println("Toggling Server Console Log");
        Admin.toggleServerConsoleLog(command.getSource().getPlayerOrException());
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleAdminInstantCommand(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        System.out.println("Toggling Admin Instant Missions for player: " + command.getSource().getPlayer());
        Admin.adminInstant(command.getSource().getPlayerOrException());
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleAdminItemsCommand(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        System.out.println("Toggling Admin No Item Requirements for player: " + command.getSource().getPlayer());
        Admin.adminItems(command.getSource().getPlayerOrException());
        return Command.SINGLE_SUCCESS;
    }
}
