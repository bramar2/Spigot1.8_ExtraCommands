package me.bramar.extracommands.commands;

import me.bramar.extracommands.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.bramar.extracommands.Main.PREFIX;

public class SocialspyCommand implements CommandExecutor {

    private final Main main;

    public SocialspyCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(!p.hasPermission("e.socialspy")) {
                p.sendMessage(ChatColor.RED + "No permission! (e.socialspy)");
                return true;
            }
            if(main.getSocialSpy().contains(p.getUniqueId())) {
                main.getSocialSpy().remove(p.getUniqueId());
                p.sendMessage(PREFIX + "You have disabled socialspy!");
            }else {
                main.getSocialSpy().add(p.getUniqueId());
                p.sendMessage(PREFIX + "You have enabled socialspy!");
            }
        }else {
            main.consoleSocialSpy = !main.consoleSocialSpy;
            sender.sendMessage(PREFIX + "You have " + (main.consoleSocialSpy ? "enabled" : "disabled") + " socialspy!");
        }
        return true;
    }
}
