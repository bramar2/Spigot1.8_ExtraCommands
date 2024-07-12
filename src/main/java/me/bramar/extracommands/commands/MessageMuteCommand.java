package me.bramar.extracommands.commands;

import me.bramar.extracommands.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import static me.bramar.extracommands.Main.PREFIX;

public class MessageMuteCommand implements CommandExecutor {
    // "msgmute"
    private final Main main;

    public MessageMuteCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!sender.hasPermission("e.msgmute")) {
            sender.sendMessage(ChatColor.RED + "No permission. (e.msgmute)");
            return true;
        }
        if(args.length < 1) {
            sender.sendMessage(PREFIX + "Usage: /msgmute <player>");
            return true;
        }
        UUID uuid;
        String name = args[0];
        Player player;
        if(args[0].contains("-")) {
            uuid = UUID.fromString(args[0]);
            player = Bukkit.getPlayer(uuid);
            OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
            if(off.hasPlayedBefore())
                name = Bukkit.getOfflinePlayer(uuid).getName();
            sender.sendMessage(PREFIX + "Your input has a dash in it. Checking from uuid...");
        }else {
            player = Bukkit.getPlayer(args[0]);
            OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
            if(!off.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.RED + "ERROR: Player not found");
                return true;
            }
            uuid = off.getUniqueId();
        }
        if(main.getMessageMute().contains(uuid)) {
            main.getMessageMute().remove(uuid);
            sender.sendMessage(PREFIX + "Successfully un message-muted " + name + " [" + uuid + "]");
            if(player != null) player.sendMessage(ChatColor.RED + "You have been unblocked from doing /msg by " + ChatColor.GREEN + "Staff" + ChatColor.RED + "!");
        }else {
            main.getMessageMute().add(uuid);
            sender.sendMessage(PREFIX + "Successfully message-muted " + name + " [" + uuid + "]");
            if(player != null) player.sendMessage(ChatColor.RED + "You have been blocked from doing /msg by " + ChatColor.GREEN + "Staff" + ChatColor.RED + "!");
        }
        main.getDatabaseConfig().set("msgmute", main.getMessageMute().stream().map(UUID::toString).collect(Collectors.toList()));
        try {
            main.getDatabaseConfig().save(main.getDatabaseFile());
        }catch(IOException e) {
            e.printStackTrace();
            System.out.println("Failed to save database (MessageMute)");
        }
        return true;
    }
}
