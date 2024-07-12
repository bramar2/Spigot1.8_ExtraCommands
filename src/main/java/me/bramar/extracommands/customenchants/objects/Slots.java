package me.bramar.extracommands.customenchants.objects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public enum Slots {
	HAND,HELM,CHEST,LEGS,FEET,
	S0,S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,
	S11,S12,S13,S14,S15,S16,S17,S18,S19,S20,
	S21,S22,S23,S24,S25,S26,S27,S28,S29,S30,
	S31,S32,S33,S34,S35,S36,S37,S38,S39,S40;
	public static Slots getSlot(Object obj) {
		assert obj != null;
		if(obj instanceof Slots) return (Slots) obj;
		String str = obj.toString().toUpperCase();
		try {
			return Slots.valueOf(str);
		}catch(Exception ignored) {}
		for(int i = 0; i <= 40; i++) {
			if(str.equalsIgnoreCase(i+"")) return Slots.valueOf("S" + str);
		}
		if(str.equalsIgnoreCase("HELMET")) return HELM;
		if(str.equalsIgnoreCase("CHESTPLATE")) return CHEST;
		if(str.equalsIgnoreCase("LEGGINGS")) return LEGS;
		if(str.equalsIgnoreCase("BOOTS")) return FEET;
		return null;
	}
	public int getSlot(Player p) {
		if (this == HELM) return 36;
		if (this == CHEST) return 37;
		if (this == LEGS) return 38;
		if (this == FEET) return 39;
		if (this == HAND) return p.getInventory().getHeldItemSlot();
		try {
			return Integer.parseInt(name().substring(1));
		}catch(Exception ignored) {}
		throw new NullPointerException("slots invalid");
	}
	public ItemStack get(LivingEntity p) {
		EntityEquipment inv = p.getEquipment();
		return this == Slots.HELM ? inv.getHelmet() :
		this == Slots.CHEST ? inv.getChestplate() :
		this == Slots.LEGS ? inv.getLeggings() :
		this == Slots.FEET ? inv.getBoots() : (p instanceof Player) ? ((Player) p).getInventory().getItem(getSlot((Player) p)) : null;
	}
}