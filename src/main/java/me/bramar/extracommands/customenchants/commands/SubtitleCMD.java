package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SubtitleCMD extends EnchantCommand {

    public SubtitleCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "SUBTITLE";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Player.class};
    }

    @Override
    public void run() {
        Player p = getInput(Player.class);
        StringBuilder msg = new StringBuilder();
        for(int i = 1; i < eachLine.length; i++) msg.append(eachLine[i]).append(":");
        msg = new StringBuilder(msg.substring(0, msg.length() - 1));
        p.sendTitle(null, ChatColor.translateAlternateColorCodes('&', msg.toString().replace("%entity%", p.getName())));
    }
}
