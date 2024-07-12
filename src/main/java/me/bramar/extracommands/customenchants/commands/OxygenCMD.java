package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import me.bramar.extracommands.customenchants.objects.NumberModifier;
import org.bukkit.entity.Player;

public class OxygenCMD extends EnchantCommand {
    public OxygenCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "OXYGEN";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Player.class};
    }

    @Override
    public void run() {
        Player p = getInput(Player.class);
        p.setRemainingAir((int) NumberModifier.getModifier(eachLine[1]).test(p.getRemainingAir(), getNumber(eachLine[2])));
    }
}
