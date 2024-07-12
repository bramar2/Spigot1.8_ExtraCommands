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

public class Blind extends CustomEnchantment {

	public Blind() {
		super(3518, EnchantmentTarget.SWORDS, 3, 1, "Blind", "&cBlind", "&cA chance to make your opponents blind");
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
			int l = getEnchantLevel(e.getPlayer(), false);
			if(checkSuccess(l * 3)) {
				int seconds = 8 + (l - 1);
				int blindnessLevel = l - 1;
				EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
				if(event.getEntity() instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) event.getEntity();
					entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
							seconds * 20, blindnessLevel, true, true), true);
				}
			}
		}catch(Exception ignored) {}
	}
	
	
}
