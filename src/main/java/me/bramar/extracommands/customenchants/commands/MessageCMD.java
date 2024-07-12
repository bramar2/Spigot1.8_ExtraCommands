package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageCMD extends EnchantCommand {
    public MessageCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "MESSAGE";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Player.class};
    }
    @Override
    public void run() {
        Player p = getInput(Player.class);
        StringBuilder string = new StringBuilder();
        if(eachLine.length < 2) return;
        for(int i = 1; i < eachLine.length; i++) {
            string.append(eachLine[i]).append(":");
        }
        string = new StringBuilder(string.substring(0, string.length()-1));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', string.toString().replace("%entity%", p.getName())));
    }
}
