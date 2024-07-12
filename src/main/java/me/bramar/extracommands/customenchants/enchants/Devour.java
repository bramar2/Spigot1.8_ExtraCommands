package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Collections;
import java.util.List;

public class Devour extends CustomEnchantment {
    public Devour() {
        super(3535, EnchantmentTarget.WEAPON, 3, 1, "Devour", "&cDevour", "&cChance to restore food when killing mobs");
    }

    @Override
    public int getMultiplier() {
        return 2;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return Collections.singletonList(EventType.ENTITY_DEATH_EVENT);
    }

    @Override
    public void onEvent(EventStore e) {
        try {
            EntityDeathEvent event = e.cast();
            Player p = event.getEntity().getKiller();
            int lvl = getEnchantLevel(p, true);
            if(usingEnchantment(p) && checkSuccess(lvl * 4) && p != null) {
                p.setFoodLevel(Math.max(20, p.getFoodLevel() + 4));
            }
        }catch(Exception ignored) {}
    }
}
