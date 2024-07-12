package me.bramar.extracommands.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static me.bramar.extracommands.Main.PREFIX;

public class NightvisionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("This command only works for players!");
            return true;
        }
        Player p = (Player) commandSender;
        if(p.hasPermission("e.nightvision.exempt")) {
            p.sendMessage(ChatColor.RED + "You're exempted from this command! (e.nightvision.exempt)");
            return true;
        }
        if(!p.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 10, false, false));
            p.sendMessage(PREFIX + "You have been granted nightvision!");
        }else {
            try {
                p.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }catch(Exception ignored) {}
            p.sendMessage(PREFIX + "Your nightvision have been revoked!");
        }
        return true;
    }
}
