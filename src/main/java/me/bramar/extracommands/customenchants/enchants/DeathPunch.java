package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.List;

public class DeathPunch extends CustomEnchantment {

    public DeathPunch() {
        super(3532, EnchantmentTarget.SWORDS_AND_AXES, 5, 1, "Death Punch", "&2Death Punch", "&2Deal more damage to zombies");
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
            EntityDamageByEntityEvent event = e.cast();
            double dmg = event.getDamage() * (1d + (double) getEnchantLevel(e.getPlayer(), false) * 0.25);
            event.setDamage(dmg);
        }catch(Exception ignored) {}
    }
}
