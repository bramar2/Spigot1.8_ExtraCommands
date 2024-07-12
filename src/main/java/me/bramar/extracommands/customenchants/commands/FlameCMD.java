package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.entity.LivingEntity;

public class FlameCMD extends EnchantCommand {
    public FlameCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "FLAME";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }
    @Override
    public void run() {
        LivingEntity p = getInput(LivingEntity.class);
        p.setFireTicks((int) getNumber(eachLine[1]));
    }
}
