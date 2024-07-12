package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.List;

public class Barbarian extends CustomEnchantment {

	public Barbarian() {
		super(3513, EnchantmentTarget.AXE, 4, 1, "Barbarian", "&6Barbarian", "&6Chance to deal more axe damage");
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
			int enchantLevel = getEnchantLevel(e.getPlayer(), false);
			System.out.println("DEBUG Barbarian: enchantLevel=" + enchantLevel);
			if(checkSuccess(enchantLevel * 3.5)) {
				EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
				double damage = event.getDamage();
				damage *= (1.2 + ((enchantLevel - 1) * 0.2));
				// Level 1 = 1.2
				// For each level after level 1, adds 0.2x multiplier
				event.setDamage(damage);
			}
		}catch(Exception ignored) {}
	}

}
