package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collections;
import java.util.List;

public class Angelic extends CustomEnchantment {

	public Angelic() {
		super(3504, EnchantmentTarget.ARMOR, 5, 1, "Angelic", "&fAngelic", "&fHeals health when damaged");
	}

	@Override
	public int getMultiplier() {
		return 3;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.DAMAGED);
	}

	@Override
	public void onEvent(EventStore e) {
		try {
			final Player p = e.getPlayer();
			double damage = e.cast(EntityDamageEvent.class).getFinalDamage();
			int percentage = getEnchantLevel(p, true) * 8;
			final double healedHP = damage * percentage / 100;
			// Damage reduction
			if(p.getHealth() + healedHP > p.getMaxHealth()) {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> p.setHealth(Math.min(p.getHealth() + healedHP, p.getMaxHealth())), 2L);
			}else p.setHealth(p.getHealth() + healedHP);
		}catch(Exception ignored) {}
	}

}
