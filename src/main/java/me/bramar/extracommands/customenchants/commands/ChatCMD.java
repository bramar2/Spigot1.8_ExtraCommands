package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.entity.Player;

public class ChatCMD extends EnchantCommand {

    public ChatCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "CHAT";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Player.class};
    }

    @Override
    public void run() {
        StringBuilder builder = new StringBuilder();
        for(int i = 1; i < eachLine.length; i++) builder.append(eachLine[i]).append(":");
        getInput(Player.class).chat(builder.substring(0, builder.length() - 1));
    }
}
