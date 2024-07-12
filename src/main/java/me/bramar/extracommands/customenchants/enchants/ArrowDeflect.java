package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.List;

public class ArrowDeflect extends CustomEnchantment {

	public ArrowDeflect() {
		super(3510, EnchantmentTarget.ARMOR, 4, 1, "Arrow Deflect", "&bArrow Deflect", "&bChance for arrows to deflect");
	}

	@Override
	public int getMultiplier() {
		return 4;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.DAMAGED_BY_PROJECTILE);
	}

	@Override
	public void onEvent(EventStore e) {
		try {
			int enchantLevel = getEnchantLevel(e.getPlayer(), true);
			if(checkSuccess(enchantLevel * 2.25)) {
				EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
				event.setCancelled(true);
			}
		}catch(Exception ignored) {}
	}

}
