package me.bramar.extracommands.commands;

import de.myzelyam.api.vanish.VanishAPI;
import me.bramar.extracommands.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

import static me.bramar.extracommands.Main.PREFIX;

public class PingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length == 0) {
                int ping = ((CraftPlayer)p).getHandle().ping;
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format("%sYour ping is &l%s&r&6ms", PREFIX, ping)));
            }else {
                Player player;
                if(args[0].contains("-")) {
                    p.sendMessage(PREFIX + "Checking from UUID...");
                    player = Bukkit.getPlayer(UUID.fromString(args[0]));
                }else player = Bukkit.getPlayer(args[0]);
                if(player == null || !VanishAPI.canSee(p, player)) {
                    p.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
                int ping = ((CraftPlayer)player).getHandle().ping;
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format("%s%s's ping is &l%s&r&6ms", PREFIX, player.getName(), ping)));
            }
        }else {
            if(args.length == 0) {
                sender.sendMessage("Usage: /ping <player>");
            }else {
                Player player;
                if(args[0].contains("-")) {
                    sender.sendMessage(PREFIX + "Checking from UUID...");
                    player = Bukkit.getPlayer(UUID.fromString(args[0]));
                }else player = Bukkit.getPlayer(args[0]);
                if(player == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
                int ping = ((CraftPlayer)player).getHandle().ping;
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format("%s%s's ping is &l%s&r&6ms", PREFIX, player.getName(), ping)));
            }
        }
        return true;
    }
}
