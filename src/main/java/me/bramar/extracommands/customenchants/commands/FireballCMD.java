package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class FireballCMD extends EnchantCommand {
    public FireballCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "FIREBALL";
    }

    @Override
    public boolean hasArguments() {
        return false;
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }
    @Override
    public void run() {
        LivingEntity p = getInput(LivingEntity.class);
        Vector vel = p.getLocation().getDirection();
        vel.multiply(4);
        p.launchProjectile(Fireball.class, vel);
    }
}
