package me.bramar.extracommands.commands;

import de.myzelyam.api.vanish.VanishAPI;
import me.bramar.extracommands.Main;
import me.bramar.extracommands.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ListCommand implements CommandExecutor {
    private final Main main;

    public ListCommand(Main main) {
        this.main = main;
    }
    // "list" "who"

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Player p = sender instanceof Player ? (Player) sender : null;
        final HashMap<Player, PlayerInfo> info = new HashMap<>();
        for(Player player : Bukkit.getOnlinePlayers()) {
            info.put(player, new PlayerInfo(main, player));
        }
        int i = 0;
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(p == null || VanishAPI.canSee(p, player)) i++;
        }
        StringBuilder msg = new StringBuilder(ChatColor.translateAlternateColorCodes('&', "&6There are " + i + " players in this server:\n&r"));
        for(PlayerInfo.Group group : PlayerInfo.Group.values()) {
            boolean added = false;
            for(Map.Entry<Player, PlayerInfo> entry : info.entrySet()) {
                if(p != null && !VanishAPI.canSee(p, entry.getKey())) continue;
                if(group != entry.getValue().getGroup()) continue;
                if(!added) msg.append(ChatColor.translateAlternateColorCodes('&', PlayerInfo.Group.getDisplayName(group, main) + "&6: "));
                if(!added) added = true;
                msg.append(ChatColor.translateAlternateColorCodes('&', (VanishAPI.isInvisible(entry.getKey()) ? ChatColor.GRAY + "[HIDDEN] " + ChatColor.GOLD + entry.getValue().getDisplayName() : entry.getValue().getDisplayName()) + "&6, "));
            }
            if(added) msg = new StringBuilder(msg.substring(0, msg.length() - 2));
            if(added) msg.append("\n");
        }
        sender.sendMessage(msg.toString());
        return true;
    }
}
