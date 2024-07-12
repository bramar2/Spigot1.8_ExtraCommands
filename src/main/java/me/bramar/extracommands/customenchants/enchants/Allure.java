package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

public class Allure extends CustomEnchantment {

	public Allure() {
		super(3502, EnchantmentTarget.SWORDS, 3, 1, "Allure", "&2Allure", "&aMobs you attack will be pulled towards you!");
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
			// Vector
			Vector playerVec = e.getPlayer().getLocation().getDirection();
			Vector vec = new Vector(playerVec.getX(), playerVec.getY(), playerVec.getZ());
			vec = vec.multiply(-1); // Reverse the direction. So it's GOING towards the entity.
			int multiplier = 2;
			int enchantLevel = getEnchantLevel(e.getPlayer(), false);
			multiplier += enchantLevel - 1;
			double x = vec.getX();
			double z = vec.getZ();
			if(x < 0) {
				x *= -1;
				x *= multiplier;
				x *= -1;
			}else x*=multiplier;
			if(z < 0) {
				z *= -1;
				z *= multiplier;
				z *= -1;
			}else z*=multiplier;
			vec = vec.setX(x);
			vec = vec.setZ(z);
			vec = vec.normalize();
			vec = vec.setY(0);
			final Vector cloneDebug = vec.clone();
			EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
			event.getEntity().setVelocity(vec);
			Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> event.getEntity().setVelocity(cloneDebug), 1L);
		}catch(Exception ignored) {}
	}

}
