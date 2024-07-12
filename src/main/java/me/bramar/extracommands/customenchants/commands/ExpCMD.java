package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import me.bramar.extracommands.customenchants.objects.NumberModifier;
import org.bukkit.entity.Player;

public class ExpCMD extends EnchantCommand {

    public ExpCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "EXP";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Player.class};
    }

    @Override
    public void run() {
        NumberModifier m = NumberModifier.getModifier(eachLine[1]);
        boolean isLevel = eachLine[2].toLowerCase().endsWith("l");
        float n = Float.parseFloat((isLevel) ? eachLine[2].substring(0, eachLine[2].length() - 1) : eachLine[2]);
        Player p = getInput(Player.class);
        if(isLevel) p.setLevel((int) m.test(p.getLevel(), n));
        else {
            int tl = p.getExpToLevel();
            float addedXp = (float) m.test(p.getExp(), n);
            if(addedXp > p.getExpToLevel()) {
                p.setLevel(p.getLevel() + 1);
                p.setExp(addedXp - tl);
            }else p.setExp(addedXp);
        }
    }
}
