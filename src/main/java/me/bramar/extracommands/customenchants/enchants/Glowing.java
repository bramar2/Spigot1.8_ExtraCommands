package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Glowing extends CustomEnchantment {

    public Glowing() {
        super(3539, EnchantmentTarget.HELMET, 2, 1, "Glowing", "&aGlowing", "&aBe able to see in the dark!");
    }

    @Override
    public int getMultiplier() {
        return 3;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return null;
    }

    @Override
    public void onEvent(EventStore e) { /* EFFECTS */ }

    @Override
    public List<PotionEffect> effects(Player p) {
        return Collections.singletonList(
                new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, Math.min(getEnchantLevel(p, true)-1,0),true, true)
        );
    }
}
