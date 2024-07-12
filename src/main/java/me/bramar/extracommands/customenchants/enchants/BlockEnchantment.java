package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.Damageable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.List;

public class BlockEnchantment extends CustomEnchantment {

	public BlockEnchantment() {
		super(3519, EnchantmentTarget.SWORDS, 3, 1, "Block", "&6Block", "&6Chance to negate an attack and deal up to 3 hearts back on blocking");
		
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
			if(e.getPlayer().isBlocking()) {
				int lvl = getEnchantLevel(e.getPlayer(), false);
				if(checkSuccess(lvl * 2.5)) {
					int damaged = lvl * 2;
					EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
					event.setCancelled(true);
					if(event.getDamager() instanceof Damageable)
						( (Damageable) event.getDamager() ).damage(damaged, event.getEntity());
				}
			}
		}catch(Exception ignored) {}
	}
}
