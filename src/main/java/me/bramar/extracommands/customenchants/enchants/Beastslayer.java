package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import net.minecraft.server.v1_8_R3.EntityMonster;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.List;

public class Beastslayer extends CustomEnchantment {

	public Beastslayer() {
		super(3514, EnchantmentTarget.SWORDS_AND_AXES, 4, 1, "Beastslayer", "&4Beastslayer", "&cDeal more damage to hostile mobs");
	}

	@Override
	public int getMultiplier() {
		return 5;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.PLAYER_DAMAGE_ENTITY);
	}
	
	private boolean isHostile(Entity entity) {
		Validate.notNull(entity, "Unable to check a Null entity hostility");
		net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity)entity).getHandle();
		return nmsEntity instanceof EntityMonster /* Hostile Mob */;
	}
	
	@Override
	public void onEvent(EventStore e) {
		try {
			EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
			if(isHostile(event.getEntity())) {
				// Add damage
				double damage = event.getDamage();
				double multiplier = 1.25;
				int lvl = getEnchantLevel(e.getPlayer(), false);
				multiplier += 0.25 * (lvl - 1);
				// Adds 0.25x multiplier every level AFTER lvl 1
				// lvl 1 = 1.25x more damage, lvl 2 = 1.5x more damage, lvl 3 = 1.75x more damage
				// lvl 4 = 2x more damage
				
				damage *= multiplier;
				event.setDamage(damage);
			}
		}catch(Exception ignored) {}
	}
	
	
}
