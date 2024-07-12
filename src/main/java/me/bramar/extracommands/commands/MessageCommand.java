package me.bramar.extracommands.commands;

import de.myzelyam.api.vanish.VanishAPI;
import me.bramar.extracommands.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.bramar.extracommands.Main.PREFIX;

public class MessageCommand implements CommandExecutor {
    // "pm" "msg" "message" "emsg" "w" "whisper" "ewhisper"
    private final Main main;
    public MessageCommand(Main plugin) {
        this.main = plugin;
    }
    // "er" "reply" "r" "ereply"
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if(args.length < 2) {
                p.sendMessage(PREFIX + "Usage: /msg <entity> <message>");
                return true;
            }
            Player player;
            try {
                player = Bukkit.getPlayer(args[0]);
            }catch(Exception e1) {
                p.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            if(player == null) {
                p.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            if(VanishAPI.isInvisible(player) && !VanishAPI.canSee(p, player)) {
                p.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            if(main.getMessageMute().contains(p.getUniqueId())) {
                p.sendMessage(ChatColor.RED + "You have been blocked from doing /msg!");
                return true;
            }
            if(main.getIgnorelist().containsKey(p.getUniqueId())) {
                if(main.getIgnorelist().get(p.getUniqueId()).contains(player.getUniqueId())) {
                    p.sendMessage(ChatColor.RED + "You can't message someone you ignore!");
                    return true;
                }
            }
            if(main.getIgnorelist().containsKey(player.getUniqueId())) {
                if(main.getIgnorelist().get(player.getUniqueId()).contains(p.getUniqueId())) {
                    p.sendMessage(ChatColor.RED + "That entity is ignoring you!");
                    return true;
                }
            }
            if(main.getMessageToggle().contains(player.getUniqueId())) {
                p.sendMessage(ChatColor.RED + "That entity has their msg toggled off.");
                return true;
            }
            StringBuilder msg = new StringBuilder();
            for(int i = 1; i < args.length; i++) {
                msg.append(args[i]);
                if(i != args.length - 1) msg.append(" ");
            }

            msg = new StringBuilder(main.fixEssentialsChat(p, msg.toString()));
            p.sendMessage(main.getSend(main.getDisplayName(player), msg.toString()));
            player.sendMessage(main.getReceive(main.getDisplayName(p), msg.toString()));
            main.sendSocialSpy(main.getThirdPerson(main.getDisplayName(p), main.getDisplayName(player), msg.toString()));
            main.getLastMessage().remove(player.getUniqueId());
            main.getLastMessage().put(player.getUniqueId(), p.getUniqueId());
        }else {
            String cmd = "essentials:msg ";
            for(String arg : args) {
                cmd += arg + " ";
            }
            Bukkit.dispatchCommand(commandSender, cmd);
        }
        return true;
    }

}
