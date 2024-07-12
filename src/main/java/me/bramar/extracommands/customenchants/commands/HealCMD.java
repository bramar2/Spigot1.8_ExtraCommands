package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import me.bramar.extracommands.customenchants.objects.NumberModifier;
import org.bukkit.entity.LivingEntity;

public class HealCMD extends EnchantCommand {
    public HealCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "HEAL";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }
    @Override
    public void run() {
        LivingEntity p = getInput(LivingEntity.class);
        p.setHealth(Math.min(p.getMaxHealth(), NumberModifier.getModifier(eachLine[1]).test(p.getHealth(), getNumber(eachLine[2]))));
    }
}
