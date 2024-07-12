package me.bramar.extracommands.commands;

import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import static me.bramar.extracommands.Main.PREFIX;

public class PushCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /push <player>");
            return true;
        }
        Player p;
        if(args[0].contains("-")) {
            sender.sendMessage(PREFIX + "Checking from UUID...");
            p = Bukkit.getPlayer(UUID.fromString(args[0]));
        }else p = Bukkit.getPlayer(args[0]);
        if(p == null || (sender instanceof Player && !VanishAPI.canSee((Player) sender, p))) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }
        Random r = new SecureRandom();
        int xA;
        int yA;
        int zA;
        boolean negX = r.nextBoolean();
        boolean negZ = r.nextBoolean();
        if(r.nextBoolean()) {
            // High X, Low Z
            xA = r.nextInt(8 - 2) + 2;
            zA = r.nextInt(3 - 1) + 1;
        }else {
            // High Z, Low X
            xA = r.nextInt(3 - 1) + 1;
            zA = r.nextInt(8 - 2) + 2;
        }
        if(negX) xA *= -1;
        if(negZ) zA *= -1;
        yA = r.nextInt(5 - 2) + 2;
        p.setVelocity(p.getVelocity().add(new Vector(xA, yA, zA)));
        return true;
    }
}
