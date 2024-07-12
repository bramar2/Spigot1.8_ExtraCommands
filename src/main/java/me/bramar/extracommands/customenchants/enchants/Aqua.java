package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.List;

public class Aqua extends CustomEnchantment {

	public Aqua() {
		super(3505, EnchantmentTarget.BOOTS, 4, 1, "Aqua", "&bAqua", "&bChance to deal double damage underwater");
	}
	
	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.PLAYER_DAMAGE_ENTITY);
	}
	@Override
	public void onEvent(EventStore e) {
		try {
			Player p = e.getPlayer();
			if(p.getWorld().getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY()+1, p.getLocation().getBlockZ()).getType().name().toLowerCase().contains("water")) {
				int enchantLevel = getEnchantLevel(p, false);
				if(checkSuccess(enchantLevel * 9)) {
					// Deal double damage
					EntityDamageByEntityEvent event = e.cast(EntityDamageByEntityEvent.class);
					event.setDamage(event.getDamage() * 2);
				}
			}
		}catch(Exception ignored) {}
	}
	
	@Override
	public int getMultiplier() {
		return 5;
	}

}
