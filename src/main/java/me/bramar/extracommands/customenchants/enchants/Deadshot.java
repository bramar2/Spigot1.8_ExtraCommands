package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Collections;
import java.util.List;

public class Deadshot extends CustomEnchantment {

    public Deadshot() {
        super(3531, EnchantmentTarget.BOW, 5, 1, "Deadshot", "&1Deadshot", "&1Headshots multiplies damage");
    }

    @Override
    public int getMultiplier() {
        return 4;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return Collections.singletonList(EventType.ENTITY_DAMAGED_BY_ARROW);
    }

    @Override
    public void onEvent(EventStore e) {
        try {
            EntityDamageByEntityEvent event = e.cast();
            if(event.getDamager().getLocation().getZ() - event.getEntity().getLocation().getZ() > 1.5D) {
                ProjectileSource shooter = ((Arrow) event.getDamager()).getShooter();
                if(shooter instanceof Player) Main.getInstance().sendActionBar((Player) shooter, "&cYou headshotted " + event.getEntity().getName() + "!");
                double dmg = event.getDamage() * (1d + ((double) getEnchantLevel(EnchantLoader.getInstance().getBow(event.getDamager())) * 0.25));
                event.setDamage(dmg);
            }
        }catch(Exception ignored) {}
    }
}
