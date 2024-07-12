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

public class Diminish extends CustomEnchantment {

    public Diminish() {
        super(3536, EnchantmentTarget.AXE, 1, 1, "Diminish", "&bDiminish", "&bChance to give your enemy mining fatigue");
    }

    @Override
    public int getMultiplier() {
        return 4;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return Collections.singletonList(EventType.PLAYER_DAMAGE_ENTITY);
    }

    @Override
    public void onEvent(EventStore e) {
        try {
            int lvl = getEnchantLevel(e.getPlayer(), true);
            int duration = lvl * 4 * 20;
            if(checkSuccess(lvl * 5)) {
                ((LivingEntity) e.cast(EntityDamageByEntityEvent.class).getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, duration, lvl, true, true));
            }
        }catch(Exception ignored) {}
    }
}
