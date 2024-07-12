package me.bramar.extracommands.commands;

import de.myzelyam.api.vanish.VanishAPI;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

import static me.bramar.extracommands.Main.PREFIX;

public class DemoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!sender.hasPermission("e.demo")) {
            sender.sendMessage(ChatColor.RED + "No permission! (e.demo)");
            return true;
        }
        if(args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /demo <player> [-c/0/101/102/103]\n" +
                    "-c: Credits, 0: Demo screen\n" +
                    "101: Demo movement controls\n" +
                    "102: Demo jump controls\n" +
                    "103: Demo inventory controls\n" +
                    "Default: Demo screen");
        }else {
            String input = args.length >= 2 ? args[1] : "0";
            Player player;
            if(args[0].contains("-"))
                player = Bukkit.getPlayer(UUID.fromString(args[0]));
            else
                player = Bukkit.getPlayer(args[0]);

            if(player == null || (sender instanceof Player && !VanishAPI.canSee((Player) sender, player))) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }

            float value;
            if(input.equalsIgnoreCase("-c"))
                value = 0f;
            else try {
                value = Float.parseFloat(input);
            }catch(NumberFormatException e1) {
                value = 0f;
            }
            PacketPlayOutGameStateChange packet = new PacketPlayOutGameStateChange(input.equalsIgnoreCase("-c") ? 4 : 5,
                    value);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

            sender.sendMessage(PREFIX + "Packet sent!");
        }
        return true;
    }
}
