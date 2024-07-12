package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Chaos extends CustomEnchantment {

	public Chaos() {
		super(3523, EnchantmentTarget.BOW, 5, 1, "Chaos", "&8Chaos", "&8Chance to deal weakness and wither effects.");
	}

	@Override
	public int getMultiplier() {
		return 5;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.ENTITY_DAMAGED_BY_ARROW);
	}

	@Override
	public void onEvent(EventStore e) {
		try {
			EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
			if(event.getEntity() instanceof LivingEntity && event.getDamager() instanceof Arrow)
				if(EnchantLoader.getInstance().bowData.containsKey(event.getDamager())) {
					ItemStack bow = EnchantLoader.getInstance().bowData.get(event.getDamager());
					if(hasEnchant(bow)) {
						int enchantLevel = getEnchantLevel(bow);
						if(checkSuccess(enchantLevel * 3)) {
							double duration = 5 + (enchantLevel - 1.5);
							int amplifier = (enchantLevel <= 2 ? 0 : 1);
							LivingEntity entity = (LivingEntity) event.getEntity();
							int ticks = String.valueOf(duration).endsWith(".5") ? (int) (duration - 0.5) * 20 + 10 : (int) (duration) * 20;
							entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, ticks, amplifier, true, true), true);
							entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, ticks, amplifier, true, true), true);
						}
					}
				}
		}catch(Exception ignored) {}
	}
	
}
