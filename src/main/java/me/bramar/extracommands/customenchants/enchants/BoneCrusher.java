package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.List;

public class BoneCrusher extends CustomEnchantment {

	public BoneCrusher() {
		super(3520, EnchantmentTarget.SWORDS_AND_AXES, 3, 1, "Bone Crusher", "&fBone Crusher", "&fIncreases damage dealt to skeletons");
	}

	@Override
	public int getMultiplier() {
		return 2;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.PLAYER_DAMAGE_ENTITY);
	}

	@Override
	public void onEvent(EventStore e) {
		try {
			EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
			if(event.getEntity() instanceof Skeleton || event.getEntity().getType() == EntityType.SKELETON) {
				double multiplier = 1.15 + (getEnchantLevel(e.getPlayer(), false) * 0.1);
				double newDamage = event.getDamage() * multiplier;
				event.setDamage(newDamage);
			}
		}catch(Exception ignored) {}
	}
	

}
