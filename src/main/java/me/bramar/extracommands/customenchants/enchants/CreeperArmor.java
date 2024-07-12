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

public class CreeperArmor extends CustomEnchantment {

	public CreeperArmor() {
		super(3528, EnchantmentTarget.ARMOR, 3, 1, "Creeper Armor", "&2Creeper Armor", "&2Chance to be immune to explosive damage or even heal on higher levels.");
	}

	@Override
	public int getMultiplier() {
		return 5;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.DAMAGED);
	}

	@Override
	public void onEvent(EventStore e) {
		try {
			EntityDamageEvent event = e.cast(EntityDamageEvent.class);
			int additive = getEnchantLevel(e.getPlayer(), true);
			if(event.getCause() == DamageCause.BLOCK_EXPLOSION || event.getCause() == DamageCause.ENTITY_EXPLOSION) if(checkSuccess(additive * 2)) {
				event.setDamage(0);
				int lvl = getEnchantLevel(e.getPlayer(), false);
				// Regen or not
				if(lvl >= 3) if(checkSuccess(5))
					e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5, 2, true, true), true);
			}
		}catch(Exception ignored) {}
	}

}
