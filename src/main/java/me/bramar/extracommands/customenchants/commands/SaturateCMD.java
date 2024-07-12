package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import me.bramar.extracommands.customenchants.objects.NumberModifier;
import org.bukkit.entity.Player;

public class SaturateCMD extends EnchantCommand {
    public SaturateCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "SATURATE";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Player.class};
    }
    @Override
    public void run() {
        Player p = getInput(Player.class);
        p.setFoodLevel(Math.min(20, (int) NumberModifier.getModifier(eachLine[1]).test(p.getFoodLevel(), getNumber(eachLine[2]))));
    }
}
