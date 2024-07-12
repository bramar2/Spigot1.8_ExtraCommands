package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class AttackNearbyCMD extends EnchantCommand {
    public AttackNearbyCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "ATTACK_NEARBY";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }
    @Override
    public void run() {
        LivingEntity p = getInput(LivingEntity.class);
        double radius = getNumber(eachLine[1]);
        EntityType type = eachLine[2].equalsIgnoreCase("*") || eachLine[2].equalsIgnoreCase("all") ? null : EntityType.valueOf(eachLine[2].toUpperCase());
        double dmg = getNumber(eachLine[3]);
        p.getWorld().getNearbyEntities(p.getLocation(), radius, radius, radius)
                .stream().filter((e) -> (e instanceof Damageable && (type == null || e.getType() == type)))
                .map((e) -> ((Damageable) e))
                .forEach((d) -> d.damage(dmg));
    }
}
