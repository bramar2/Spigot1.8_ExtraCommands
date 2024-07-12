package me.bramar.extracommands.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.bramar.extracommands.Main.PREFIX;

public class ConsoleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(!p.hasPermission("e.useconsole")) {
                p.sendMessage(ChatColor.RED + "No permission! (e.useconsole)");
                return true;
            }
            if(args.length < 1) {
                p.sendMessage(PREFIX + "Usage: /console <cmd>");
                return true;
            }
            StringBuilder msg = new StringBuilder();
            for(int i = 0; i < args.length; i++) {
                msg.append(args[i]);
                if(i != args.length - 1) msg.append(" ");
            }
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), msg.toString());
            p.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "[Console: /" + msg + "]");
            p.sendMessage(ChatColor.GREEN + "Command runned. Check console for more info");
        }else {
            sender.sendMessage("This command only works for players!");
        }
        return true;
    }
}
