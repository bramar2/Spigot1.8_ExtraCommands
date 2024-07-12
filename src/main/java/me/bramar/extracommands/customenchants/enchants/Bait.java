package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class Bait extends CustomEnchantment {

	public Bait() {
		super(3512, EnchantmentTarget.FISHING_ROD, 3, 1, "Bait", "&1Bait", "&1Chance to get double drops from fishing");
	}

	@Override
	public int getMultiplier() {
		return 4;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.emptyList();
	}

	@Override
	public void onEvent(EventStore e) {  }
	
	@EventHandler
	public void onItemSpawn(PlayerFishEvent e) {
		if(e.getState() == PlayerFishEvent.State.CAUGHT_FISH && e.getCaught() instanceof Item) {
			try {
				if(!usingEnchantment(e.getPlayer())) return;
				if(!checkSuccess(3 * getEnchantLevel(e.getPlayer(), false))) return;
				// Double!
				Main.getInstance().sendActionBar(e.getPlayer(), "&1You have doubled your drop!");
				Item item = (Item) e.getCaught();
				ItemStack drops = item.getItemStack().clone();
				drops.setAmount(drops.getAmount() * 2);
				item.setItemStack(drops);
			}catch(Exception ignored) {}
		}
	}
}
