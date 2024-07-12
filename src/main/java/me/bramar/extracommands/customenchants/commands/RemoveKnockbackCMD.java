package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class RemoveKnockbackCMD extends EnchantCommand {
    public RemoveKnockbackCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "REMOVE_KNOCKBACK";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }

    @Override
    public void run() {
        LivingEntity e = getInput(LivingEntity.class);
        Vector vel = e.getVelocity().clone().normalize();
        double x = vel.getX(), y = vel.getY(), z = vel.getZ();
        double p = getNumber(eachLine[1]);
        x = x - (x * p / 100);
        y = y - (y * p / 100);
        z = z - (z * p / 100);
        vel.setX(x);
        vel.setY(y);
        vel.setZ(z);
        e.setVelocity(vel);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> e.setVelocity(vel), 1);
    }
}
