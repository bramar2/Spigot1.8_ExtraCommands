package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Aegis extends CustomEnchantment {
	
	public Aegis() {
		super(3501, EnchantmentTarget.BOOTS, 3, 1, "Aegis", "&aAegis", "&2A chance of getting speed on taking fall damage.");
	}
	@Override
	public int getMultiplier() {
		return 4;
	}
	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.DAMAGED);
	}
	@Override
	public void onEvent(EventStore e) {
		try {
			if(e.cast(EntityDamageEvent.class).getCause() != DamageCause.FALL) return;
			// 5% every level
			int enchantLevel = getEnchantLevel(e.getPlayer(), true);
			if(checkSuccess(enchantLevel * 5)) {
				// Success, give speed
				// Gives speed 2 75% speed 3 15%
				// For every level (after 1) 75% goes down by 5% and speed 3 increases by 5%
				// For every level, the duration increases by 5 seconds.
				if(checkSuccess(80 - (enchantLevel * 5))) {
					// speed 2
					e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (enchantLevel * 50) * 2, 1, true, true));
				}else {
					// speed 3
					e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (enchantLevel * 50) * 2, 2, true, true));
				}
			}
		}catch(Exception ignored) {}
	}
	
}
