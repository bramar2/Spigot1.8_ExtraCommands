package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.entity.LivingEntity;

public class ExtinguishCMD extends EnchantCommand {
    public ExtinguishCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "EXTINGUISH";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }
    @Override
    public void run() {
        LivingEntity p = getInput(LivingEntity.class);
        p.setFireTicks(-1);
    }
}
