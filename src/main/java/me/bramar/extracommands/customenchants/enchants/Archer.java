package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.List;

public class Archer extends CustomEnchantment {

	public Archer() {
		super(3507, EnchantmentTarget.BOW, 4, 1, "Archer", "&dArcher", "&dIncreases bow damage");
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
			EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
			double extraDamage = getEnchantLevel(EnchantLoader.getInstance().bowData.get(event.getDamager())) * 1.3;
			event.setDamage(event.getDamage() + extraDamage);
		}catch(Exception ignored) {}
	}
	
}
