package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.List;

public class Chunky extends CustomEnchantment {

	public Chunky() {
		super(3524, EnchantmentTarget.CHESTPLATE, 6, 1, "Chunky", "&6Chunky", "&6Chance to receive less damage");
	}

	@Override
	public int getMultiplier() {
		return 5;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.DAMAGE_BY_ENTITY);
	}

	@Override
	public void onEvent(EventStore e) {
		try {
			EntityDamageByEntityEvent event = e.cast();
			int lvl = getEnchantLevel(e.getPlayer(), true);
			if(checkSuccess(lvl * 2)) {
				double multiplier = 0.8 - ((lvl - 1) * 0.1);
				double damage = event.getDamage() * multiplier;
				event.setDamage(damage);
			}
		}catch(Exception ignored) {}
	}

}
