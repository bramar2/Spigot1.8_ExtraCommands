package me.bramar.extracommands.commands;

import me.bramar.extracommands.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.bramar.extracommands.Main.PREFIX;

public class BroadcastCommand implements CommandExecutor {
    // "broadcast" "bc"
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if(!p.hasPermission("e.broadcast")) {
                p.sendMessage(ChatColor.RED + "No permission! (e.broadcast)");
                return true;
            }
            if(args.length == 0) {
                p.sendMessage(PREFIX + "Usage: /broadcast <message>");
                return true;
            }
            StringBuilder msg = new StringBuilder();
            for(int i = 0; i < args.length; i++) {
                msg.append(args[i]);
                if(i != args.length - 1) msg.append(" ");
            }
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg.toString()));
        }else {
            if(args.length == 0) {
                Bukkit.getServer().getConsoleSender().sendMessage(PREFIX + "Usage: /broadcast <message>");
                return true;
            }
            StringBuilder msg = new StringBuilder();
            for(int i = 1; i < args.length; i++) {
                msg.append(args[i]);
                if(i != args.length - 1) msg.append(" ");
            }
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg.toString()));
        }
        return true;
    }
}
