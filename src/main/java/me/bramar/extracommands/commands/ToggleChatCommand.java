package me.bramar.extracommands.commands;

import me.bramar.extracommands.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ToggleChatCommand implements CommandExecutor, Listener {
    private final Main main;
    private boolean chatDisabled = false;

    public ToggleChatCommand(Main main) {
        this.main = main;
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if(e.getPlayer().hasPermission("e.bypasschat"))
            return;
        if(chatDisabled) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.GRAY + "The chat is currently muted!");
            System.out.println("[MUTED-CHAT-LOG] " + e.getPlayer().getName() + ": " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        chatDisabled = !chatDisabled;
        if(sender instanceof Player) {
            boolean silent = args.length != 0 && args[0].toLowerCase().contains("-s");
            Bukkit.broadcastMessage(ChatColor.GREEN + "[ToggleChat] Chat has been " +
                    (chatDisabled ? "disabled" : "enabled") + " by " + (silent ? ChatColor.GRAY + "CONSOLE" : main.getDisplayName((Player) sender)));
        }else {
            Bukkit.broadcastMessage(ChatColor.GREEN + "[ToggleChat] Chat has been " +
                    (chatDisabled ? "disabled" : "enabled") + " by " + ChatColor.GRAY + "CONSOLE");
        }
        return true;
    }
}
