package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class Armored extends CustomEnchantment {

	public Armored() {
		super(3508, EnchantmentTarget.ARMOR, 4, 1, "Armored", "&6&lArmored", "&6Decreases damage from enemy's swords by 2% per level");
	}

	@Override
	public int getMultiplier() {
		return 4;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.DAMAGE_BY_ENTITY);
	}

	@Override
	public void onEvent(EventStore e) {
		try {
			EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
			int enchantLevel = getEnchantLevel(e.getPlayer(), true);
			double per = enchantLevel * 1.5;
			if(event.getDamager() instanceof LivingEntity) {
				ItemStack item = ((LivingEntity) event.getDamager()).getEquipment().getItemInHand();
				if(EnchantmentTarget.SWORDS_AND_AXES.includes(item)) per = enchantLevel * 4;
			}
			double damage = event.getDamage();
			damage -= (damage * per / 100);
			event.setDamage(damage);
		}catch(Exception ignored) {}
	}

}
