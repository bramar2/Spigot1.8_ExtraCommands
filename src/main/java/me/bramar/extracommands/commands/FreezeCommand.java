package me.bramar.extracommands.commands;

import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.bramar.extracommands.Main.PREFIX;

public class FreezeCommand extends BukkitRunnable implements CommandExecutor, Listener {
    private final List<UUID> frozen = new ArrayList<>();
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if(frozen.contains(e.getPlayer().getUniqueId())) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + e.getPlayer().getName() + " Leaving the game while frozen");
            frozen.remove(e.getPlayer().getUniqueId());
        }
    }
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            if(frozen.contains(e.getPlayer().getUniqueId()))
                e.setTo(new Location(e.getTo().getWorld(), e.getFrom().getX(), e.getTo().getY(), e.getFrom().getZ()));
        }
    }

    @Override
    public void run() {
        for(UUID uuid : frozen) {
            Player p = Bukkit.getPlayer(uuid);
            if(p != null) {
                p.sendMessage(ChatColor.GRAY + "\nYou are currently frozen by staff! Do not leave or you will be banned.\n");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(!p.hasPermission("e.freeze")) {
                p.sendMessage(ChatColor.RED + "No permission! (e.freeze)");
                return true;
            }
            if(args.length < 1) {
                p.sendMessage(PREFIX + "Usage: /freeze <player>");
                return true;
            }
            Player player;
            if(args[0].contains("-")) {
                player = Bukkit.getPlayer(UUID.fromString(args[0]));
            }else player = Bukkit.getPlayer(args[0]);
            if(player == null || !VanishAPI.canSee(p, player)) {
                p.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            if(frozen.contains(player.getUniqueId())) {
                frozen.remove(player.getUniqueId());
                String msg = ChatColor.translateAlternateColorCodes('&', String.format("&7[Silent] &a%s&7 has unfreezed &a%s", p.getName(), player.getName()));
                Bukkit.getOnlinePlayers().stream().filter(p1 -> p1.hasPermission("e.freeze")).forEach(p1 -> p1.sendMessage(msg));
                Bukkit.getConsoleSender().sendMessage(msg);
                player.sendMessage(ChatColor.GREEN + "You have been unfrozen by Staff!");
            }else {
                frozen.add(player.getUniqueId());
                String msg = ChatColor.translateAlternateColorCodes('&', String.format("&7[Silent] &a%s&7 has freezed &a%s", p.getName(), player.getName()));
                Bukkit.getOnlinePlayers().stream().filter(p1 -> p1.hasPermission("e.freeze")).forEach(p1 -> p1.sendMessage(msg));
                Bukkit.getConsoleSender().sendMessage(msg);
                player.sendMessage(ChatColor.RED + "You have been frozen by Staff! Do not leave or you will be banned.");
            }
        }else {
            if(args.length < 1) {
                sender.sendMessage("Usage: /freeze <player>");
                return true;
            }
            Player player;
            if(args[0].contains("-")) {
                player = Bukkit.getPlayer(UUID.fromString(args[0]));
            }else player = Bukkit.getPlayer(args[0]);
            if(player == null) {
                sender.sendMessage("Player not found.");
                return true;
            }
            if(frozen.contains(player.getUniqueId())) {
                frozen.remove(player.getUniqueId());
                String msg = ChatColor.translateAlternateColorCodes('&', String.format("&7[Silent] &a%s&7 has unfreezed &a%s", "CONSOLE", player.getName()));
                Bukkit.getOnlinePlayers().stream().filter(p1 -> p1.hasPermission("e.freeze")).forEach(p1 -> p1.sendMessage(msg));
                Bukkit.getConsoleSender().sendMessage(msg);
                player.sendMessage(ChatColor.GREEN + "You have been unfrozen by Staff!");
            }else {
                frozen.add(player.getUniqueId());
                String msg = ChatColor.translateAlternateColorCodes('&', String.format("&7[Silent] &a%s&7 has freezed &a%s", "CONSOLE", player.getName()));
                Bukkit.getOnlinePlayers().stream().filter(p1 -> p1.hasPermission("e.freeze")).forEach(p1 -> p1.sendMessage(msg));
                Bukkit.getConsoleSender().sendMessage(msg);
                player.sendMessage(ChatColor.RED + "You have been frozen by Staff! Do not leave or you will be banned.");
            }
        }
        return true;
    }
}
