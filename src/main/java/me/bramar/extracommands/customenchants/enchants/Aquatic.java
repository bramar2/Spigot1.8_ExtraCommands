package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class Aquatic extends CustomEnchantment {

	public Aquatic() {
		super(3506, EnchantmentTarget.HELMET, 1, 1, "Aquatic", "&aAquatic", "&aBreathe underwater.");
	}

	@Override
	public int getMultiplier() {
		return 2;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.emptyList();
	}

	@Override
	public void onEvent(EventStore e) { /* Unused check repeatingTask() */ }
	
	@Override
	public void repeatingTask(Player p) {
		if(p.getWorld().getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY()+1, p.getLocation().getBlockZ()).getType().name().toLowerCase().contains("water")) {
			p.setRemainingAir(p.getMaximumAir() + 50);
		}
	}
	@Override
	public int amountOfTicks() {
		return 50;
	}
	
}
