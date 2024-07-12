package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class Bowmaster extends CustomEnchantment {

	public Bowmaster() {
		super(3521, EnchantmentTarget.BOW, 5, 1, "Bowmaster", "&6Bowmaster", "&6Multiplies damage againt players "
				+ "using swords");
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
			ItemStack bow = EnchantLoader.getInstance().bowData.get((Arrow) event.getDamager());
			double multiplier = 1.0 + (getEnchantLevel(bow) * 0.2);
			if(EnchantmentTarget.SWORDS.includes(((LivingEntity) event.getEntity()).getEquipment().getItemInHand())) {
				// Swords
				double damage = event.getDamage() * multiplier;
				event.setDamage(damage);
			}
		}catch(Exception ignored) {}
	}
	
}
