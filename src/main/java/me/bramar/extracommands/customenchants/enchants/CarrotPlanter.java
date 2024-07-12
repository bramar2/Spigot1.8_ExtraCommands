package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class CarrotPlanter extends CustomEnchantment {

	public CarrotPlanter() {
		super(3522, EnchantmentTarget.HOE, 3, 1, "Carrot Planter", "&aCarrot Planter", "&aPlant carrots in a"
				+ " 3x3 area by shift-right clicking");
	}

	@Override
	public int getMultiplier() {
		return 2;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.INTERACT);
	}
	
	public int getAmount(Player p, Material mat) {
		int amount = 0;
		for(ItemStack item : p.getInventory().getContents()) {
			if(item != null) if(item.getType() == mat) amount += item.getAmount();
		}
		return amount;
	}
	
	@Override
	public void onEvent(EventStore e) {
		try {
			PlayerInteractEvent event = e.cast(PlayerInteractEvent.class);
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) {
				if(event.getClickedBlock().getType() == Material.SOIL) {
					Location loc = event.getClickedBlock().getLocation().add(0, 1, 0);
					event.setCancelled(true);
					int radius = getEnchantLevel(e.getPlayer(), false);
					int carrots = getAmount(e.getPlayer(), Material.CARROT_ITEM);
					int removedCarrots = 0;
					if(carrots != 0) for(int x = -radius; x <= radius; x++) {
						for(int y = -radius; y < radius; y++) {
							for(int z = -radius; z <= radius; z++) {
								Block blockAt = loc.getBlock().getRelative(x, y, z);
								Block blockTop = blockAt.getRelative(0, 1, 0);
								Block blockDown;
								try {
									blockDown = blockAt.getRelative(0, -1, 0);
								}catch(Exception e1) {
									blockDown = null;
								}
								if(carrots <= 0) break;
								try {
									if(blockAt.getType() == Material.AIR && blockDown.getType() == Material.SOIL) {
										carrots--;
										removedCarrots++;
										blockAt.setType(Material.CARROT);
										e.getPlayer().playEffect(blockAt.getLocation().clone().add(0, 0.3, 0), Effect.HAPPY_VILLAGER, "No data needed for HAPPY_VILLAGER effect (according to sources)");
									}else if(blockAt.getType() == Material.SOIL && blockTop.getType() == Material.AIR) {
										carrots--;
										removedCarrots++;
										blockTop.setType(Material.CARROT);
										e.getPlayer().playEffect(blockTop.getLocation().clone().add(0, 0.3, 0), Effect.HAPPY_VILLAGER, "No data needed for HAPPY_VILLAGER effect (according to sources)");
									}
								}catch(Exception e1) {
									try {
										if(blockAt.getType() == Material.SOIL && blockTop.getType() == Material.AIR) {
											carrots--;
											removedCarrots++;
											blockTop.setType(Material.CARROT);
											e.getPlayer().playEffect(blockTop.getLocation().clone().add(0, 0.3, 0), Effect.HAPPY_VILLAGER, "No data needed for HAPPY_VILLAGER effect (according to sources)");
										}
									}catch(Exception ignored) {}
								}
							}
							if(carrots <= 0) break;
						}
						if(carrots <= 0) break;
					}
					//
					// Remove amount of carrots from entity inventory.
					removeItem(e.getPlayer(), Material.CARROT_ITEM, removedCarrots);
				}
			}
		}catch(Exception ignored) {}
	}

}
