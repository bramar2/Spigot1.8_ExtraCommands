package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Cleave extends CustomEnchantment {

	public Cleave() {
		super(3525, EnchantmentTarget.AXE, 7, 1, "Cleave", "&9Cleave", "&9Damages players within a radius that increases by level");
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
			int lvl = getEnchantLevel(e.getPlayer(), false);
			final int radius = Math.min(lvl, 5);
			int damage = 2;
			if(lvl >= 3 && lvl <= 5) damage += lvl - 2;
			else if(lvl >= 6) damage += (lvl - 2) * (1.25 + ((lvl - 6) * 0.25));
			UUID damagedUUID = event.getEntity().getUniqueId();
			UUID damagerUUID = e.getPlayer().getUniqueId();
			if(damagedUUID == null) damagedUUID = UUID.randomUUID();
			for(Entity entity : e.getPlayer().getWorld().getNearbyEntities(event.getEntity().getLocation(), radius, radius, radius)) {
				if(entity instanceof Player && entity.getUniqueId() != damagedUUID && entity.getUniqueId() != damagerUUID) {
					Damageable dmg = (Damageable) entity;
					dmg.damage(damage);
					dmg.setLastDamageCause(new EntityDamageByEntityEvent(e.getPlayer(), dmg, EntityDamageEvent.DamageCause.CUSTOM, damage));
				}
			}
		}catch(Exception ignored) {}
	}
	
}
