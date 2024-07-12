package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionCMD extends EnchantCommand {
    public PotionCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "POTION";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }
    @Override
    public void run() {
        LivingEntity p = getInput(LivingEntity.class);
        final PotionEffectType type;
        PotionEffectType type1;
        try {
            type1 = PotionEffectType.getByName(eachLine[1]);
        }catch(Exception e1) {
            try {
                type1 = PotionEffectType.getByName(eachLine[1].toUpperCase());
            }catch(Exception e2) {
                type1 = PotionEffectType.getById((int) getNumber(eachLine[1]));
            }
        }
        type = type1;
        int duration = (int) getNumber(eachLine[2]);
        int amplifier = (int) getNumber(eachLine[3]);
        p.addPotionEffect(new PotionEffect(type, duration, amplifier));
    }
}
