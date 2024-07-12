package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class BoostCMD extends EnchantCommand {
    public BoostCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "BOOST";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }
    @Override
    public void run() {
        LivingEntity p = getInput(LivingEntity.class);
        double speed = getNumber(eachLine[1]);
        Vector v = p.getLocation().getDirection();
        double x = v.getX();
        double y = v.getY();
        double z = v.getZ();
        if(x < 0) {
            x *= -1;
            x *= speed/2f;
            x *= -1;
        }else x*=speed/2f;
        if(y < 0) {
            y *= -1;
            y *= speed/2f;
            y *= -1;
        }else y*=speed/2f;
        if(z < 0) {
            z *= -1;
            z *= speed/2f;
            z *= -1;
        }else z*=speed/2f;
        v = v.setX(x)
                .setY(y)
                .setZ(z)
                .normalize();
        v = p.getVelocity().clone().add(v);
        Vector vC = v.clone();
        p.setVelocity(v);
        Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> p.setVelocity(vC), 1L);
    }
}
