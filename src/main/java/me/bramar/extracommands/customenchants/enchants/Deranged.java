package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Deranged extends CustomEnchantment {

    public Deranged() {
        super(3534, EnchantmentTarget.SWORDS, 3, 1, "Deranged", "&7Deranged", "&7Strike lightnings at nearby players");
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
            final int lvl = getEnchantLevel(e.getPlayer(), true);
            int dist = lvl*2;
            event.getEntity().getWorld().getNearbyEntities(event.getEntity().getLocation(), dist, dist, dist)
                    .stream().filter((entity) -> entity.getType() == EntityType.PLAYER || entity instanceof Player)
                    .filter((entity) -> entity.getUniqueId() != e.getPlayer().getUniqueId())
                    .forEach((player) -> {
                        LightningStrike l = player.getWorld().strikeLightningEffect(player.getLocation().add(0, 0.1d, 0)); // Lightning effect
                        Player p = (Player) player;
                        p.damage((double) lvl * 3.0d, l);
                        p.setLastDamageCause(new EntityDamageByEntityEvent(e.getPlayer(), p, EntityDamageEvent.DamageCause.LIGHTNING, (double) lvl * 3.0d));
                    });
        }catch(Exception ignored) {}
    }
}
