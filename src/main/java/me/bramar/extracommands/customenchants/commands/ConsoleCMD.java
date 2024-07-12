package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ConsoleCMD extends EnchantCommand {

    public ConsoleCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "CONSOLE";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[0];
    }

    @Override
    public void run() {
        StringBuilder msg = new StringBuilder();
        for(int i = 1; i < eachLine.length; i++) msg.append(eachLine[i]).append(":");
        msg = new StringBuilder(msg.substring(0, msg.length() - 1));
        Player p = getInput(Player.class);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), (p != null) ? msg.toString().replace("%entity%", p.getName()) : msg.toString());
    }
}
