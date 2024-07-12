package me.bramar.extracommands.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmeticCommand implements CommandExecutor {
    // "cosmetic" "cosmetics"
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            ((Player) commandSender).performCommand("uc menu main");
        }else System.out.println("This command only works for players!");
        return true;
    }
}
