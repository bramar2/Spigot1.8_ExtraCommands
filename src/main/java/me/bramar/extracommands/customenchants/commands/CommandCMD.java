package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.entity.Player;

public class CommandCMD extends EnchantCommand {
    public CommandCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "COMMAND";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Player.class};
    }
    @Override
    public void run() {
        Player p = getInput(Player.class);
        StringBuilder builder = new StringBuilder();
        if(eachLine.length < 2) return;
        for(int i = 1; i < eachLine.length; i++) {
            builder.append(eachLine[i]).append(":");
        }
        builder = new StringBuilder(builder.substring(0, builder.length() - 1));
        p.performCommand(builder.toString());
    }
}
