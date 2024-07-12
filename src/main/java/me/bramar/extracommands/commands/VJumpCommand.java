package me.bramar.extracommands.commands;

import me.bramar.extracommands.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VJumpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            Main.getInstance().jump((Player) sender);
        }else sender.sendMessage("This command only works for players!");
        return true;
    }
}
