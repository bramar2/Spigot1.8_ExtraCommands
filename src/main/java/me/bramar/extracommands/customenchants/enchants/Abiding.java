package me.bramar.extracommands.customenchants.enchants;

import java.util.Arrays;
import java.util.List;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Abiding extends CustomEnchantment {

	public Abiding() {
		super(3500, EnchantmentTarget.SWORDS, 1, 1, "Abiding", "&9Abiding", "&9Your weapons will sure be unbreakable", new Enchantment[] {
				// None (Mending doens't exist in 1.8)
		});
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Arrays.asList(EventType.TICK, EventType.BREAK_ITEM);
	}

	@Override
	public void onEvent(EventStore e) {
		if(e.getType() == EventType.TICK) {
			Player p = e.getPlayer();
			ItemStack item = p.getInventory().getItemInHand();
			try {
				item.getType().getMaxDurability(); // To produce NullPointerException IF the item does NOT have durability
				if(item.getDurability() != 0) {item.setDurability((short) 0);
				p.getInventory().setItemInHand(item);}
			}catch(Exception ignored) {}
		}else if(e.getType() == EventType.BREAK_ITEM) {
			try {
				ItemStack item = e.cast(PlayerItemBreakEvent.class).getBrokenItem();
				item.getType().getMaxDurability();
				item.setDurability((short) 0);
				if(e.getPlayer().getInventory().getItemInHand() == null) e.getPlayer().getInventory().setItemInHand(item);
				else {
					if(e.getPlayer().getInventory().firstEmpty() == -1) {
						// Inventory full
						Item itemEntity = e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), item);
						itemEntity.setVelocity(new Vector(0,0,0));
						e.getPlayer().sendMessage(ChatColor.RED + "Your weapon randomly broke! Your inventory is full, it has been dropped on the ground!");
					}else {
						e.getPlayer().getInventory().addItem(item);
						e.getPlayer().sendMessage(ChatColor.RED + "Your weapon randomly broke! We have added it to your inventory!");
					}
				}
			}catch(Exception ignored) {}
		}
	}

	@Override
	public int getMultiplier() {
		return 4;
	}
}
