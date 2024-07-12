package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.material.MaterialData;

import java.util.Collections;
import java.util.List;

public class Bleed extends CustomEnchantment {
	
	public Bleed() {
		super(3517, EnchantmentTarget.SWORDS_AND_AXES, 6, 1, "Bleed", "&4Bleed", "&4Make your opponents bleed and does more damage");
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
			Location loc = event.getEntity().getLocation();
			for(Entity entity : e.getPlayer().getWorld().getNearbyEntities(loc, 10, 10, 10)) {
				if(entity instanceof Player) {
					((Player)entity).playEffect(loc, Effect.TILE_BREAK, new MaterialData(Material.REDSTONE_BLOCK));
				}
			}
			e.getPlayer().playEffect(loc, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
			double damage = event.getDamage() * (1.2 + (0.1 * (getEnchantLevel(e.getPlayer(), false) - 1)));
			event.setDamage(damage);
		}catch(Exception ignored) {}
	}
	
}
