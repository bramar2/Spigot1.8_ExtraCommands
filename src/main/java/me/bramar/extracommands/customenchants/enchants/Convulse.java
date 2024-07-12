package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

public class Convulse extends CustomEnchantment {

	public Convulse() {
		super(3527, EnchantmentTarget.BOOTS, 6, 1, "Convulse", "&eConvulse", "&eChance to throw your attackers into the air");
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
			LivingEntity entity = (LivingEntity) e.cast(EntityDamageByEntityEvent.class).getDamager();
			int lvl = getEnchantLevel(e.getPlayer(), true);
			if(checkSuccess(lvl * 2)) {
				double yMultiplier = lvl * 7.0 / 10.0;
				if(yMultiplier < 1.3) yMultiplier = 1.3;
				double y = 0.42 * yMultiplier;
				Vector vec = entity.getVelocity().clone();
				vec.add(new Vector(0, y, 0));
				entity.setVelocity(vec);
			}
		}catch(Exception ignored) {}
	}

}
