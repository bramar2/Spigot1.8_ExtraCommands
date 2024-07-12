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

public class ReplyCommand implements CommandExecutor {
    private final Main main;

    public ReplyCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length < 1) {
                p.sendMessage(PREFIX + "Usage: /r <message>");
                return true;
            }
            if(!main.getLastMessage().containsKey(p.getUniqueId())) {
                p.sendMessage(ChatColor.GRAY + "There is no-one to reply to.");
                return true;
            }
            StringBuilder msg = new StringBuilder();
            for(int i = 0; i < args.length; i++) {
                msg.append(args[i]);
                if(i != args.length - 1) msg.append(" ");
            }
            msg = new StringBuilder(main.fixEssentialsChat(p, msg.toString()));
            Player player = Bukkit.getPlayer(main.getLastMessage().get(p.getUniqueId()));
            if(player == null || !player.isOnline()) {
                p.sendMessage(ChatColor.RED + "ERROR: Player is not online.");
                return true;
            }
            if(VanishAPI.isInvisible(player) && !VanishAPI.canSee(p, player)) {
                p.sendMessage(ChatColor.GRAY + "There is no-one to reply to.");
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
            p.sendMessage(main.getSend(main.getDisplayName(player), msg.toString()));
            player.sendMessage(main.getReceive(main.getDisplayName(p), msg.toString()));
            main.sendSocialSpy(main.getThirdPerson(main.getDisplayName(p), main.getDisplayName(player), msg.toString()));
            main.getLastMessage().remove(player.getUniqueId());
            main.getLastMessage().put(player.getUniqueId(), p.getUniqueId());
        }else sender.sendMessage("This command only works for players! Use /msg instead");
        return true;
    }
}
