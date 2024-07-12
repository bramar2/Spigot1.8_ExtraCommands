package me.bramar.extracommands.commands;

import me.bramar.extracommands.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.bramar.extracommands.Main.PREFIX;

public class ECReloadCommand implements CommandExecutor {
    private final Main main;

    public ECReloadCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            if(!sender.hasPermission("e.reload")) {
                sender.sendMessage(ChatColor.RED + "No permission! (e.reload)");
                return true;
            }
        }
        try {
            main.loadConfiguration();
            sender.sendMessage(PREFIX + "Configuration reloaded.");
        }catch(Exception e1) {
            sender.sendMessage(ChatColor.RED + "An error occured while reloading configuration: " + e1.getMessage() + "\n" + ChatColor.RED + "See the console for the full stack trace.");
            e1.printStackTrace();
        }
        return true;
    }
}
