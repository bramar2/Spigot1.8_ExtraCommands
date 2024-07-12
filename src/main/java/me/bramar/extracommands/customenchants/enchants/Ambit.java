package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Ambit extends CustomEnchantment {

	public Ambit() {
		super(3503, EnchantmentTarget.SWORDS_AND_AXES, 7, 1, "Ambit", "&6Ambit", "&6Damages mobs within a radius");
	}

	@Override
	public int getMultiplier() {
		return 3;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.PLAYER_DAMAGE_ENTITY);
	}

	@Override
	public void onEvent(EventStore e) {
		try {
			UUID playerUUID = e.getPlayer().getUniqueId();
			UUID damagedUUID = e.cast(EntityDamageByEntityEvent.class).getEntity().getUniqueId();
			int radius = getEnchantLevel(e.getPlayer(), false) * 2;
			double damage = getEnchantLevel(e.getPlayer(), false) * 0.75;
			int yRadius = radius;
			if(yRadius > 5) yRadius = 5;
			for(Entity entity : e.getPlayer().getWorld().getNearbyEntities(e.getPlayer().getLocation(), radius, yRadius, radius)) {
				if(entity.getUniqueId().equals(playerUUID)) continue;
				if(entity.getUniqueId().equals(damagedUUID)) continue;
				if(entity instanceof Damageable) {
					Damageable dmg = (Damageable) entity;
					dmg.damage(damage);
					dmg.setLastDamageCause(new EntityDamageByEntityEvent(e.getPlayer(), dmg, EntityDamageEvent.DamageCause.CUSTOM, damage));
				}
			}
		}catch(Exception ignored) {}
		
	}
	
}
