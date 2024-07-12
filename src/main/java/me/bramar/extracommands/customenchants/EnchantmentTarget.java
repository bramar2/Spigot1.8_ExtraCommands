package me.bramar.extracommands.customenchants;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Custom Enchantment Target enums.
 * To make a new CUSTOM one, see IEnchantmentTarget for interface
 */
public enum EnchantmentTarget {
	SWORDS(new Material[] { Material.WOOD_SWORD,Material.STONE_SWORD,
	Material.IRON_SWORD,Material.DIAMOND_SWORD,Material.GOLD_SWORD }, "Swords"),

	AXE(new Material[] { Material.WOOD_AXE,Material.STONE_AXE,Material.IRON_AXE,
			Material.DIAMOND_AXE,Material.GOLD_AXE }, "Axe"),

	SWORDS_AND_AXES("Swords and axes", SWORDS, AXE),

	HELMET(new Material[]{ Material.LEATHER_HELMET,Material.CHAINMAIL_HELMET,
	Material.DIAMOND_HELMET,Material.IRON_HELMET,Material.GOLD_HELMET }, "Helmet"),

	CHESTPLATE(new Material[]{ Material.LEATHER_CHESTPLATE,Material.CHAINMAIL_CHESTPLATE,
			Material.DIAMOND_CHESTPLATE,Material.IRON_CHESTPLATE,Material.GOLD_CHESTPLATE }, "Chestplate"),

	LEGGINGS(new Material[]{ Material.LEATHER_LEGGINGS,Material.CHAINMAIL_LEGGINGS,
			Material.DIAMOND_LEGGINGS,Material.IRON_LEGGINGS,Material.GOLD_LEGGINGS }, "Leggings"),

	BOOTS(new Material[]{ Material.LEATHER_BOOTS,Material.CHAINMAIL_BOOTS,
			Material.DIAMOND_BOOTS,Material.IRON_BOOTS,Material.GOLD_BOOTS }, "Boots"),

	ARMOR("Armor", HELMET, CHESTPLATE, LEGGINGS, BOOTS),
	
	BOW(new Material[] { Material.BOW }, "Bow"),

	FISHING_ROD(new Material[] { Material.FISHING_ROD }, "Fishing Rod"),

	BOW_AND_ROD("Bow and Fishing Rod", BOW, FISHING_ROD),
	
	PICKAXE(new Material[] { Material.WOOD_PICKAXE, Material.STONE_PICKAXE,
	Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.GOLD_PICKAXE }, "Pickaxe"),

	SHOVEL(new Material[] { Material.WOOD_SPADE,Material.STONE_SPADE,
	Material.IRON_SPADE,Material.DIAMOND_SPADE,Material.GOLD_SPADE }, "Shovel"),

	HOE(new Material[] { Material.WOOD_HOE,Material.STONE_HOE,Material.IRON_HOE,
	Material.DIAMOND_HOE,Material.GOLD_HOE }, "Hoe"),

	TOOLS("Tools", PICKAXE, AXE, SHOVEL, HOE),

	TOOLS_NO_HOE("Tools excluding hoes", PICKAXE, AXE, SHOVEL),

	SHEARS(new Material[]{ Material.SHEARS }, "Shears"),

	FLINT_AND_STEEL(new Material[]{ Material.FLINT_AND_STEEL }, "Flint and steel"),

	SHEARS_AND_C4("Shears and Flint and steel", SHEARS, FLINT_AND_STEEL),

	TOOLS_V2("Tools with Shears and Flint and steel", TOOLS, SHEARS, SHEARS_AND_C4),

	WEAPON("Weapon (swords)", SWORDS),

	ALL("All (Tools, Shears, Flint and steel, Armor, and swords)", SHEARS_AND_C4, BOW_AND_ROD, ARMOR, TOOLS_V2, SWORDS),

	BREAKABLE("Items with durability", ARMOR, TOOLS_V2, SWORDS, BOW_AND_ROD, SHEARS_AND_C4),

	ALL_ITEMS("All items") {
		@Override
		public boolean includes(Material item) { return true; }
	},
	TEMPORARY(null)
	;
	// Use this to override getTarget() on CustomEnchantment to make a custom one!
	public static EnchantmentTarget temporary(String name, EnchantmentTarget... targets) {
		return temporary(name, (mat) -> Arrays.stream(targets).anyMatch((target) -> target.includes(mat)));
	}
	public static EnchantmentTarget temporary(String name, Material... materials) {
		return temporary(name, (mat) -> Arrays.stream(materials).anyMatch((mat2) -> mat == mat2));
	}
	public static EnchantmentTarget temporary(String name, Predicate<Material> predicate) {
		TEMPORARY.name = name;
		TEMPORARY.forTemp = predicate;
		return TEMPORARY;
	}
	private List<Material> mat;
	private List<EnchantmentTarget> target;
	private String name;
	// Temporary
	private Predicate<Material> forTemp;

	EnchantmentTarget(String name) {
		this.name = name;
	}
	EnchantmentTarget(String name, EnchantmentTarget... targets) {
		this(name);
		this.target = Arrays.asList(targets);
	}
	EnchantmentTarget(List<Material> mat, String name) {
		this.mat = mat;
		this.name = name;
	}
	EnchantmentTarget(Material[] mat, String name) {
		this(Arrays.asList(mat), name);
	}

	public boolean includes(Material item) {
		if(forTemp != null) if(forTemp.test(item)) return true;
		if(target != null) for(EnchantmentTarget target : target) { if(target.includes(item)) return true; }
		if(mat != null) return mat.contains(item);
		return false;
	}
	public boolean includes(ItemStack item) {
		return includes(item.getType());
	}
	@Override
	public String toString() {
		return name == null ? "None" : name;
	}
}
