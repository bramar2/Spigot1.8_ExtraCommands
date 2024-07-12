package me.bramar.extracommands.customenchants.checks;

import me.bramar.extracommands.customenchants.config.Check;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class HasPotion extends Check {

    public HasPotion(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "HAS_POTION";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[]{Player.class};
    }

    @Override
    public boolean check() {
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
        return getInput(Player.class).hasPotionEffect(type);
    }
}
