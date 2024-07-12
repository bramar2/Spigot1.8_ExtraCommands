package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Confuse extends CustomEnchantment {

	public Confuse() {
		super(3526, EnchantmentTarget.SWORDS, 4, 1, "Confuse", "&6Confuse", "&6Chance to give your opponent nausea effect.");
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
			EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
			int lvl = getEnchantLevel(e.getPlayer(), false);
			if(checkSuccess(lvl * 2) && event.getEntity() instanceof LivingEntity) {
				int amplifier = lvl <= 2 ? 0 : 1;
				int duration = (lvl * 40);
				((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(
						PotionEffectType.CONFUSION,
						duration,
						amplifier,
						true,
						true
						), true);
			}
		}catch(Exception ignored) {}
	}

}
