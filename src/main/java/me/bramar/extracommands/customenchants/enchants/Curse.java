package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Curse extends CustomEnchantment {

    public Curse() {
        super(3530, EnchantmentTarget.ARMOR, 2, 1, "Curse", "&bCurse", "&bChance to give your enemy mining fatigue");
    }

    @Override
    public int getMultiplier() {
        return 4;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return Collections.singletonList(EventType.DAMAGE_BY_ENTITY);
    }

    @Override
    public void onEvent(EventStore e) {
        try {
            EntityDamageByEntityEvent event = e.cast();
            if(event.getDamager() instanceof LivingEntity) {
                double per = (double) getEnchantLevel(e.getPlayer(), true) * 2.5D;
                if(checkSuccess(per)) {
                    // Give mining fatigue
                    int ticks = getEnchantLevel(e.getPlayer(), true) * 50;
                    int amplifier = getEnchantLevel(e.getPlayer(), false) - 1;
                    ((LivingEntity) event.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, ticks, amplifier, true, true));
                }
            }
        }catch(Exception ignored) {}
    }
}
