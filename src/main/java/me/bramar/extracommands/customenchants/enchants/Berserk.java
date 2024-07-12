package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Berserk extends CustomEnchantment {

	public Berserk() {
		super(3515, EnchantmentTarget.SWORDS_AND_AXES, 5, 1, "Berserk", "&c&lBerserk", "&cChance to give mining fatigue and strength");
	}

	@Override
	public int getMultiplier() {
		return 5;
	}

	@Override
	public List<EventType> listeningEventsTo() {
		return Collections.singletonList(EventType.PLAYER_DAMAGE_ENTITY);
	}
	@Override
	public void onEvent(EventStore e) {
		try {
			int enchantLevel = getEnchantLevel(e.getPlayer(), false);
			if(checkSuccess(enchantLevel * 2)) {
				int miningFatigue = (enchantLevel <= 3 ? 1 : 2);
				int strength = (enchantLevel <= 3 ? 0 : 1);
				int time = (enchantLevel <= 4 ? 5 : 8) * 20 /* In ticks */;
				e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, time, miningFatigue, false, true), true);
				e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, time, strength, false, true), true);
				Main.getInstance().sendActionBar(e.getPlayer(), "&4Berserk gave you strength " + toRoman(strength) + " and mining fatigue " + toRoman(miningFatigue));
			}
		}catch(Exception ignored) {}
	}

}
