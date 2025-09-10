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
}
