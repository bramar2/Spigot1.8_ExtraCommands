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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static me.bramar.extracommands.Main.PREFIX;

public class IgnoreCommand implements CommandExecutor {
    private final Main main;

    public IgnoreCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length == 0) {
                StringBuilder msg = new StringBuilder(PREFIX + "Ignored players: ");
                if(!main.getIgnorelist().containsKey(p.getUniqueId()) || main.getIgnorelist().get(p.getUniqueId()).isEmpty()) {
                    p.sendMessage(msg.append("None!").toString());
                    return true;
                }
                main.getIgnorelist().get(p.getUniqueId()).forEach(ignoredUUID -> {
                    OfflinePlayer ignoredOff = Bukkit.getOfflinePlayer(ignoredUUID);
                    if(ignoredOff.hasPlayedBefore()) msg.append(ignoredOff.getName());
                    else msg.append(ignoredUUID);
                    msg.append(", ");
                });
                p.sendMessage(msg.substring(0, msg.length() - 2));
            }else {
                OfflinePlayer ignoredPlayer;
                if(args[0].contains("-")) {
                    p.sendMessage(PREFIX + "Checking from UUID...");
                    ignoredPlayer = Bukkit.getOfflinePlayer(UUID.fromString(args[0]));
                }else
                    ignoredPlayer = Bukkit.getOfflinePlayer(args[0]);

                if(!ignoredPlayer.hasPlayedBefore()) {
                    p.sendMessage(ChatColor.RED + "ERROR: Player not found.");
                    return true;
                }
                if(ignoredPlayer.getUniqueId().equals(p.getUniqueId())) {
                    p.sendMessage(ChatColor.RED + "You can't ignore yourself, that would be silly!");
                    return true;
                }
                if(main.getIgnoreexempt().contains(ignoredPlayer.getName())) {
                    p.sendMessage(ChatColor.RED + "You can't ignore that player!");
                    return true;
                }
                List<UUID> ignoredList = main.getIgnorelist().getOrDefault(p.getUniqueId(), new ArrayList<>());
                if(ignoredList.contains(ignoredPlayer.getUniqueId())) {
                    ignoredList.remove(ignoredPlayer.getUniqueId());
                    p.sendMessage(PREFIX + "You are no longer ignoring " + ignoredPlayer.getName());
                }else {
                    ignoredList.add(ignoredPlayer.getUniqueId());
                    p.sendMessage(PREFIX + "You are now ignoring " + ignoredPlayer.getName());
                }
                main.getIgnorelist().put(p.getUniqueId(), ignoredList);
                main.getDatabaseConfig().getConfigurationSection("ignore-list").set(p.getUniqueId().toString(),
                        ignoredList.stream().map(UUID::toString).collect(Collectors.toList()));
                try {
                    main.getDatabaseConfig().save(main.getDatabaseFile());
                }catch(IOException e) {
                    e.printStackTrace();
                    System.out.println("Failed to save database! (Ignore)");
                }
            }
        }else sender.sendMessage("This command only works for players!");
        return true;
    }
}
