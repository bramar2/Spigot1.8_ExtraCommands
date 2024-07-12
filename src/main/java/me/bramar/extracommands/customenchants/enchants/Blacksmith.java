package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class Blacksmith extends CustomEnchantment {

	public Blacksmith() {
		super(3516, EnchantmentTarget.AXE, 5, 1, "Blacksmith", "&7Blacksmith", "&7Repair your weapon in exchange for less damage.");
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
			int durabilityRepair = getEnchantLevel(e.getPlayer(), false) * 5;
			int durabilityCheck = (durabilityRepair <= 5 ? 5 : durabilityRepair - 3);
			ItemStack item = e.getPlayer().getInventory().getItemInHand();
			if(item.getDurability() >= durabilityCheck) {
				EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
				double dmg = event.getDamage();
				event.setDamage(dmg * 0.75 /* 25% less damage */);
				short asetDurability = (short) (item.getDurability() - durabilityRepair);
				final short setDurability = (short) (asetDurability < 0 ? 0 : asetDurability);
				item.setDurability(setDurability);
			}
		}catch(Exception ignored) {}
	}

}
