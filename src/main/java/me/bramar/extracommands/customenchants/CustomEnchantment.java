package me.bramar.extracommands.customenchants;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import me.bramar.extracommands.Main;
import me.bramar.extracommands.events.BlockDropItemEvent;
import me.bramar.extracommands.events.PlayerLandEvent;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CustomEnchantment extends EnchantmentWrapper implements Listener {
	private final static TreeMap<Integer, String> map = new TreeMap<>();

    static {
        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");
    }
    public static CustomEnchantment getEnchant(Class<? extends CustomEnchantment> clazz) {
    	try {
			return EnchantLoader.getInstance().getEnchants().stream().filter((ench) ->
					ench.getClass() == clazz).findFirst().get();
		}catch(Exception ignored) {}
    	return null;
	}
    // Tools -> blocks
	public Set<Material> getAllowed(Material mat) {
		if(!EnchantmentTarget.TOOLS_NO_HOE.includes(mat)) return Collections.emptySet(); // Hoes do not break anything faster in 1.8
		return EnchantmentTarget.PICKAXE.includes(mat) ? ToolBlocks.getPickaxe() : EnchantmentTarget.AXE.includes(mat) ? ToolBlocks.getAxe() : ToolBlocks.getShovel();
	}
	//
	// Prevent Anticheat from flagging
	public void exemptPlayerBreak(Player p) {
		if(Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
			NCPExemptionManager.exemptPermanently(p, CheckType.BLOCKBREAK);
		}
	}
	public void unexemptPlayerBreak(Player p) {
		if(Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
			NCPExemptionManager.unexempt(p, CheckType.BLOCKBREAK);
		}
	}
    public void breakAsPlayer(Player p, Block block) {
		if(p.getWorld() != block.getWorld()) throw new IllegalStateException("The entity's world is different than the block's world");
		PlayerInteractManager m = ((CraftPlayer) p).getHandle().playerInteractManager;
		m.breakBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
    }
    public static String toRoman(int number) {
    	int l =  map.floorKey(number);
        if ( number == l ) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }

    public int getEnchantLevel(Player p, boolean additive) {
    	try {
    		int level = 0;
    		ItemStack helm = p.getInventory().getHelmet();
    		ItemStack chest = p.getInventory().getChestplate();
    		ItemStack legs = p.getInventory().getLeggings();
    		ItemStack boots = p.getInventory().getBoots();
    		ItemStack hand = p.getInventory().getItemInHand();
    		if(helm != null) if(hasEnchant(helm)) {
    			if(additive) level += getEnchantLevel(helm);
    			else if(level < getEnchantLevel(helm)) level = getEnchantLevel(helm);
    		}
    		if(chest != null) if(hasEnchant(chest)) {
    			if(additive) level += getEnchantLevel(chest);
    			else if(level < getEnchantLevel(chest)) level = getEnchantLevel(chest);
    		}
    		if(legs != null) if(hasEnchant(legs)) {
    			if(additive) level += getEnchantLevel(legs);
    			else if(level < getEnchantLevel(legs)) level = getEnchantLevel(legs);
    		}
    		if(boots != null) if(hasEnchant(boots)) {
    			if(additive) level += getEnchantLevel(boots);
    			else if(level < getEnchantLevel(boots)) level = getEnchantLevel(boots);
    		}
    		if(hand != null) if(hasEnchant(hand)) {
    			int itemLvl = getEnchantLevel(hand);
    			if(additive) level += itemLvl;
    			else if(level < itemLvl) level = itemLvl;
    		}
    		return level;
    	}catch(Exception ignored) {}
    	return 0;
    }
    
	private final int maxLevel;
	private final EnchantmentTarget target;
	private final int startLevel;
	private final String name;
	private List<Enchantment> conflictsWith = new ArrayList<>();
	private final String description;
	private final String displayName;
	// Enchantment GLOBAL (can be accessed across different CustomEnchantments) variables EMPTY

	//
	public static final String ENCH_TAG = "CustomEnchantment";
	public static final String ENCHANTED_TAG = "CE";

	public final Throwable registerEnchantment() {
		try {
			Enchantment.registerEnchantment(this);
			return null;
		}catch(Exception e) {return e;}
	}
	public int getEnchantLevel(ItemStack item) {
		try {
			if(!hasEnchant(item)) return 0;
			net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
			if(!nmsStack.hasTag()) return 0;
			if(!nmsStack.getTag().hasKey(ENCHANTED_TAG)) return 0;
			NBTTagList list = (NBTTagList) nmsStack.getTag().get(ENCHANTED_TAG);
			for(int i = 0; i < list.size(); i++) {
				if(list.get(i).hasKey("id") && list.get(i).hasKey("lvl")) {
					if(list.get(i).getInt("id") == getId()) return list.get(i).getInt("lvl");
				}
			}
		}catch(Exception ignored) {}
		return 0;
	}
	public ItemStack addEnchantTag(ItemStack item, int lvl) {
		Validate.notNull(item, "Can't add an enchant tag to a Null itemstack");
		try {
			net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
			NBTTagCompound tag = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();
			NBTTagList CE = tag.hasKey(ENCHANTED_TAG) ? (NBTTagList) tag.get(ENCHANTED_TAG) : new NBTTagList();
			NBTTagCompound enchTag = new NBTTagCompound();
			enchTag.setInt("id", getId());
			enchTag.setInt("lvl", lvl);
			enchTag.setString("name", getName());

			boolean hasDuplicate = false;
			List<Integer> duplicates = new ArrayList<>();
			for(int i = 0; i < CE.size(); i++) {
				if(CE.get(i).hasKey("id")) if(CE.get(i).getInt("id") == getId()) {
					hasDuplicate = true;
					duplicates.add(i);
				}
			}
			if(hasDuplicate) for(int i : duplicates) {
				CE.a(i); // Remove
			}
			CE.add(enchTag);
			tag.set(ENCHANTED_TAG, CE);
			nmsStack.setTag(tag);
			item = CraftItemStack.asBukkitCopy(nmsStack);
		}catch(Exception ignored) {}
		return item;
	}
	public boolean hasEnchant(ItemStack item) {
		try {
			Validate.notNull(item, "Can't check an enchant tag to a Null itemstack");
			net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
			if(!nmsStack.hasTag()) return false;
			if(!nmsStack.getTag().hasKey(ENCHANTED_TAG)) return false;
			NBTTagList list = (NBTTagList) nmsStack.getTag().get(ENCHANTED_TAG);
			for(int i = 0; i < list.size(); i++) {
				NBTTagCompound compound = list.get(i);
				if(compound.hasKey("id")) if(compound.getInt("id") == getId()) return true;
			}
		}catch(Exception ignored) {}
		return false;
	}

	/**
	 * Gets entity's armor and item in hand to an array if it contains the enchant
	 * @param p The entity
	 * @return The array in which 0 = Hand, 1 - 4 = Helmet - Boots. Null if no enchant
	 */
	public ItemStack[] currentlyEnchanted(Player p) {
		PlayerInventory inv = p.getInventory();
		ItemStack[] array = {inv.getItemInHand(), inv.getHelmet(), inv.getChestplate(), inv.getLeggings(), inv.getBoots()};
		for(int i = 0; i < 5; i++) {
			if(!hasEnchant(array[i])) array[i] = null;
		}
		return array;
	}
	
	// for onEvent()
	
	/**
	 * Anvil Multiplier Combining:
	 * https://minecraft.fandom.com/wiki/Anvil_mechanics
	 * @return The multiplier
	 */
	public abstract int getMultiplier();

	@EventHandler
	public final void onExpDrop(final BlockExpEvent e) {
		if(listeningEventsTo() != null && listeningEventsTo().contains(EventType.BLOCK_DROP_EXP)) {
			final Player[] p = new Player[] {null};
			for(Map.Entry<Location, Player> entry : EnchantLoader.getInstance().blockExpInfo.entrySet()) {
				Location loc = entry.getKey();
				Player player = entry.getValue();
				if(EnchantLoader.getInstance().sameBlock(loc, e.getBlock().getLocation())) {
					p[0] = player;
					break;
				}
			}
			if(p[0] != null) this.onEvent(new EventStore(EventType.BLOCK_DROP_EXP, e).setPlayer(p[0]));
		}
	}

	@EventHandler
	public final void onBlockDrop(BlockDropItemEvent e) {
		if(listeningEventsTo() == null) return;
		if(usingEnchantment(e.getPlayer()) && listeningEventsTo().contains(EventType.BLOCK_DROP_ITEM)) onEvent(new EventStore(EventType.BLOCK_DROP_ITEM, e).setPlayer(e.getPlayer()));
	}

	@EventHandler
	public final void onLand(PlayerLandEvent e) {
		if(listeningEventsTo() == null) return;
		if(usingEnchantment(e.getPlayer()) && listeningEventsTo().contains(EventType.LAND)) onEvent(new EventStore(EventType.LAND, e).setPlayer(e.getPlayer()));
	}
	@EventHandler
	public final void entityDamage(EntityDamageEvent e) {
		if(listeningEventsTo() == null) return;
		if(e.getEntity() instanceof Player) if(usingEnchantment(((Player) e.getEntity())) && listeningEventsTo().contains(EventType.DAMAGED)) onEvent(new EventStore(EventType.DAMAGED, e).setPlayer((Player)e.getEntity()));
		if(listeningEventsTo().contains(EventType.ENTITY_DAMAGE_EVENT)) onEvent(new EventStore(EventType.ENTITY_DAMAGE_EVENT, e));
	}
	@EventHandler
	public final void entityDamage2(EntityDamageByEntityEvent e) {
		if(listeningEventsTo() == null) return;
		if(e.getEntity() instanceof Player) if(usingEnchantment(((Player) e.getEntity())) && listeningEventsTo().contains(EventType.DAMAGE_BY_ENTITY)) onEvent(new EventStore(EventType.DAMAGE_BY_ENTITY, e).setPlayer((Player)e.getEntity()));
		if(listeningEventsTo().contains(EventType.ENTITY_DAMAGED_BY_ENTITY)) onEvent(new EventStore(EventType.ENTITY_DAMAGED_BY_ENTITY, e));
		if(e.getDamager() instanceof Player) if(listeningEventsTo().contains(EventType.PLAYER_DAMAGE_ENTITY)) if(usingEnchantment((Player)e.getDamager())) onEvent(new EventStore(EventType.PLAYER_DAMAGE_ENTITY, e).setPlayer((Player) e.getDamager()));

		if(e.getCause() == DamageCause.PROJECTILE && e.getDamager() instanceof Arrow &&
				listeningEventsTo().contains(EventType.ENTITY_DAMAGED_BY_ARROW) && hasEnchant(EnchantLoader.getInstance().getBow(e.getDamager()))) onEvent(new EventStore(EventType.ENTITY_DAMAGED_BY_ARROW, e));
		try {
			boolean isPlayer = e.getEntity() instanceof Player;
			boolean correctCause = e.getCause() == DamageCause.PROJECTILE;
			boolean damagerIsArrow = e.getDamager() instanceof Arrow;
			boolean listeningToDamageByProj = listeningEventsTo().contains(EventType.DAMAGED_BY_PROJECTILE);
			boolean hasEnchant = usingEnchantment((Player) e.getEntity());
			if(isPlayer && correctCause && damagerIsArrow && listeningToDamageByProj && hasEnchant)
				this.onEvent(new EventStore(EventType.DAMAGED_BY_PROJECTILE, e).setPlayer((Player) e.getEntity()));
		}catch(Exception ignored) {}

	}
	@EventHandler
	public final void entityDamage3(EntityDamageByBlockEvent e) {
		if(listeningEventsTo() == null) return;
		if(e.getEntity() instanceof Player) if(usingEnchantment(((Player) e.getEntity())) && listeningEventsTo().contains(EventType.DAMAGE_BY_BLOCK)) onEvent(new EventStore(EventType.DAMAGE_BY_BLOCK, e).setPlayer((Player)e.getEntity()));
	}
	@EventHandler
	public final void blockBreak(BlockBreakEvent e) {
		if(listeningEventsTo() == null) return;
		if(usingEnchantment(e.getPlayer()) && listeningEventsTo().contains(EventType.BLOCK_BREAK)) onEvent(new EventStore(EventType.BLOCK_BREAK, e).setPlayer(e.getPlayer()));
	}
	@EventHandler
	public final void blockPlace(BlockPlaceEvent e) {
		if(listeningEventsTo() == null) return;
		if(usingEnchantment(e.getPlayer()) && listeningEventsTo().contains(EventType.BLOCK_PLACE)) onEvent(new EventStore(EventType.BLOCK_PLACE, e).setPlayer(e.getPlayer()));
	}
	@EventHandler
	public final void consumeItem(PlayerItemConsumeEvent e) {
		if(listeningEventsTo() == null) return;
		if(usingEnchantment(e.getPlayer()) && listeningEventsTo().contains(EventType.CONSUME_ITEM)) onEvent(new EventStore(EventType.CONSUME_ITEM, e).setPlayer(e.getPlayer()));
	}
	@EventHandler
	public final void breakItem(PlayerItemBreakEvent e) {
		if(listeningEventsTo() == null) return;
		if(hasEnchant(e.getBrokenItem()) && listeningEventsTo().contains(EventType.BREAK_ITEM)) onEvent(new EventStore(EventType.BREAK_ITEM, e).setPlayer(e.getPlayer()));
	}
	@EventHandler
	public final void interactAtEntity(PlayerInteractAtEntityEvent e) {
		if(listeningEventsTo() == null) return;
		if(usingEnchantment(e.getPlayer()) && listeningEventsTo().contains(EventType.INTERACT_AT_ENTITY)) onEvent(new EventStore(EventType.INTERACT_AT_ENTITY, e).setPlayer(e.getPlayer()));
	}
	@EventHandler
	public final void interactEntity(PlayerInteractEntityEvent e) {
		if(listeningEventsTo() == null) return;
		if(usingEnchantment(e.getPlayer()) && listeningEventsTo().contains(EventType.INTERACT_ENTITY)) onEvent(new EventStore(EventType.INTERACT_ENTITY, e).setPlayer(e.getPlayer()));
	}
	@EventHandler
	public final void interact(PlayerInteractEvent e) {
		if(listeningEventsTo() == null) return;
		if(usingEnchantment(e.getPlayer()) && listeningEventsTo().contains(EventType.INTERACT)) onEvent(new EventStore(EventType.INTERACT, e).setPlayer(e.getPlayer()));
	}
	@EventHandler
	public final void xpChange(PlayerExpChangeEvent e) {
		if(listeningEventsTo() == null) return;
		if(usingEnchantment(e.getPlayer()) && listeningEventsTo().contains(EventType.XP_CHANGE)) onEvent(new EventStore(EventType.XP_CHANGE, e).setPlayer(e.getPlayer()));
	}
	@EventHandler
	public final void shootBow(EntityShootBowEvent e) {
		if(listeningEventsTo() == null) return;
		if(listeningEventsTo().contains(EventType.SHOOT_BOW)) onEvent(new EventStore(EventType.SHOOT_BOW, e));
	}
	@EventHandler
	public final void entityExplode(EntityExplodeEvent e) {
		if(listeningEventsTo() == null) return;
		if(listeningEventsTo().contains(EventType.ENTITY_EXPLODE)) onEvent(new EventStore(EventType.ENTITY_EXPLODE, e));
	}
	@EventHandler
	public final void potionSplash(PotionSplashEvent e) {
		if(listeningEventsTo() == null) return;
		if(listeningEventsTo().contains(EventType.POTION_SPLASH)) onEvent(new EventStore(EventType.POTION_SPLASH, e));
	}
	@EventHandler
	public final void projectileHit(ProjectileHitEvent e) {
		if(listeningEventsTo() == null) return;
		if(listeningEventsTo().contains(EventType.PROJECTILE_HIT)) onEvent(new EventStore(EventType.PROJECTILE_HIT, e));
	}
	@EventHandler
	public final void projectileLaunch(ProjectileLaunchEvent e) {
		if(listeningEventsTo() == null) return;
		if(listeningEventsTo().contains(EventType.PROJECTILE_LAUNCH)) onEvent(new EventStore(EventType.PROJECTILE_LAUNCH, e));
	}

	public final int getEnchantLevel(ItemStack[] armor, boolean additive) {
		try {
			int lvl = 0;
			for(ItemStack item : armor) {
				int armorLvl = getEnchantLevel(item);
				if(additive) lvl += armorLvl;
				else lvl = Math.max(lvl, armorLvl);
			}
			return lvl;
		}catch(Exception ignored) {}
		return 0;
	}

	@EventHandler
	public final void onEntityDeath(EntityDeathEvent e) {
		if(listeningEventsTo() == null) return;
		if(listeningEventsTo().contains(EventType.ENTITY_DEATH_EVENT)) onEvent(new EventStore(EventType.ENTITY_DEATH_EVENT, e));
	}
	
	//
	public final boolean checkSuccess(double percentage) {
		if(percentage <= 0) return false;
		if(percentage >= 100) return true;
		double per = (new Random().nextInt(10000) + 1) / 100.0;
        return per <= percentage;
    }
	private List<PotionEffect> removeDupe(List<PotionEffect> list) {
		final PotionEffectType[] existed = new PotionEffectType[list.size()];
		final int[] count = {0};
		new ArrayList<>(list).forEach((pot) -> {
			if(Arrays.stream(existed).filter((pot2) -> pot2 != null && pot.getType() != pot2).count() <= 0) {
				list.remove(pot);
			}else {
				existed[count[0]] = pot.getType();
				count[0]++;
			}
		});
		return list;
	}
	// Only purpose for ConfigEnchantment (does nothing but set variables so it can setup in ConfigEnchantment itself)
	public CustomEnchantment(int id, EnchantmentTarget target, int maxLevel, int startLevel, String name, String displayName, String description, Enchantment[] conflictsWith, boolean _ThisIsForConfigEnchantment) {
		super(id);
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
		this.maxLevel = maxLevel;
		this.startLevel = startLevel;
		this.name = name;
		this.target = target;
		this.description = description;
		this.displayName = displayName;
		this.conflictsWith = new ArrayList<>(Arrays.asList(conflictsWith));
	}
	//

	public CustomEnchantment(int id, EnchantmentTarget target, int maxLevel, int startLevel, String name, String displayName, String description) {
		super(id);
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
		if(listeningEventsTo() != null) if(listeningEventsTo().contains(EventType.TICK)) Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
			if(listeningEventsTo().contains(EventType.TICK)) for(Player p : Bukkit.getOnlinePlayers()) {
                if(usingEnchantment(p)) onEvent(new EventStore(EventType.TICK).setPlayer(p));
            }
		}, 0L, 1L);
		this.maxLevel = maxLevel;
		this.startLevel = startLevel;
		this.name = name;
		this.target = target;
		this.description = description;
		this.displayName = displayName;
		if(amountOfTicks() >= 1) {
			Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (usingEnchantment(player)) {
						CustomEnchantment.this.repeatingTask(player);
					}
				}
			}, amountOfTicks(), amountOfTicks());
		}
		Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
			try {
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(usingEnchantment(p)) playerEffects(p).forEach(p::addPotionEffect);
				}
			}catch(Exception ignored) {}
		}, 80L, 80L);
	}
	public CustomEnchantment(int id, EnchantmentTarget target, int maxLevel, int startLevel, String name, String displayName, String description, Enchantment... conflictsWith) {
		this(id, target, maxLevel, startLevel, name, displayName, description);
		try {
			this.conflictsWith = Arrays.asList(conflictsWith);
		}catch(Exception e1) {
			this.conflictsWith = Collections.emptyList();
		}
	}
	private Set<PotionEffect> playerEffects(Player p) {
		return effects(p).stream().map(
				(pot) -> new PotionEffect(pot.getType(),
						100,
						pot.getAmplifier(),
						pot.isAmbient(),
						pot.hasParticles()))
				.collect(Collectors.toSet());
	}

	public List<PotionEffect> effects(Player p) { return Collections.emptyList(); }
	
	public String getDisplayName() {
		return ChatColor.translateAlternateColorCodes('&', displayName);
	}
	public String getRawDisplayName() {
		return displayName; // with & symbol not replaced
	}
	
	private ItemStack storeEnchantments(ItemStack item, int level) {
		net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();
		NBTTagCompound ench = tag.hasKey(ENCH_TAG) ? tag.getCompound(ENCH_TAG) : new NBTTagCompound();
		ench.setInt("id", this.getId());
		ench.setInt("lvl", level);
		tag.set(ENCH_TAG, ench);
		nmsStack.setTag(tag);
		item = CraftItemStack.asBukkitCopy(nmsStack);
		return item;
	}
	
	public ItemStack getBook(int level) {
		// Can be overrided in enchantments. But this is the default one.
		ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
		ItemMeta meta = book.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', getDisplayName()) + " " + toRoman(level));
		meta.setLore(Arrays.asList(
				ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', getDescription()),
				ChatColor.AQUA + "Shift right click on anvil for Custom Anvil GUI",
				"",
				ChatColor.BLUE + "Enchantment Info",
				ChatColor.BLUE + "Level: " + (getStartLevel() == getMaxLevel() ? getMaxLevel() : getStartLevel() + " - " + getMaxLevel()),
				ChatColor.BLUE + "Allowed Items: " + getTargets(),
				conflictsWith == null ? "" : conflictsWith.size() == 0 ? "" : ChatColor.DARK_AQUA + "Conflicted Enchantments: " + getConflictInfo()
				));
		book.setItemMeta(meta);
		return storeEnchantments(book, level);
	}

	private String getConflictInfo() {
		StringBuilder str = new StringBuilder();
		for(Enchantment ench : getConflictedEnchantment()) {
			str.append(ChatColor.WHITE).append(ench instanceof CustomEnchantment ? ((CustomEnchantment) ench).displayName : ench.getName()).append(ChatColor.YELLOW).append(", ");
		}
		return str.substring(0, str.length() - 2).replace(ChatColor.COLOR_CHAR+"", "");
	}
	
	public final boolean usingEnchantment(LivingEntity e) {
		if(getTarget() == null) return false;
		EntityEquipment equip = e.getEquipment();
		PlayerInventory inv = e instanceof Player ? ((Player) e).getInventory() : null;
		ItemStack head = inv == null ? equip.getHelmet() : inv.getHelmet();
		ItemStack chest = inv == null ? equip.getChestplate() : inv.getChestplate();
		ItemStack legs = inv == null ? equip.getLeggings() : inv.getLeggings();
		ItemStack feet = inv == null ? equip.getBoots() : inv.getBoots();
		ItemStack hand = inv == null ? equip.getItemInHand() : inv.getItemInHand();
		if(head != null) if(hasEnchant(head) && (getTarget() == EnchantmentTarget.ALL || getTarget() == EnchantmentTarget.ARMOR || getTarget() == EnchantmentTarget.HELMET)) return true;
		if(chest != null) if(hasEnchant(chest) && (getTarget() == EnchantmentTarget.ALL || getTarget() == EnchantmentTarget.ARMOR || getTarget() == EnchantmentTarget.CHESTPLATE)) return true;
		if(legs != null) if(hasEnchant(legs) && (getTarget() == EnchantmentTarget.ALL || getTarget() == EnchantmentTarget.ARMOR || getTarget() == EnchantmentTarget.LEGGINGS)) return true;
		if(feet != null) if(hasEnchant(feet) && (getTarget() == EnchantmentTarget.ALL || getTarget() == EnchantmentTarget.ARMOR || getTarget() == EnchantmentTarget.BOOTS)) return true;
		if(hand == null) return false;
		if(!hasEnchant(hand)) return false;
		try {
			return getTarget().includes(hand.getType());
		}catch(Exception e1) {
			return false;
		}
	}

	public final void removeItem(Player p, Material mat, int amount) {
		if(!p.getInventory().containsAtLeast(new ItemStack(Material.CARROT_ITEM), amount)) throw new IllegalStateException("The entity has less " + mat + " amount than the amount needed to be removed (" + amount + "). Is the server lagging?");
		ItemStack item = new ItemStack(mat);
		item.setAmount(amount);
		p.getInventory().removeItem(item);
	}

	/**
	 * Amount of ticks for repeating task
	 * @return The amount of ticks
	 * @deprecated This could cause lag if the tick is close to <code>1</code> and the code is big.<br>If the tick is at least above 20 or the code is light-weight, this should be fine.
	 */
	@Deprecated
	public int amountOfTicks() {
		return -1; // -1 for Not enabled
	}
	/**
	 * Repeating task every <code>CustomEnchantment#amountOfTicks()</code> ticks.
	 * Already checked if the entity is using the enchantment
	 * @param player The entity that is currently using the enchantment
	 * @deprecated This could cause lag if the tick is close to <code>1</code> and the code is big.<br>If the tick is at least above 20 or the code is simple, this should be fine.
	 */
	@Deprecated
	public void repeatingTask(Player player) {   }
	public abstract List<EventType> listeningEventsTo();
	/**
	 * On event. Note: The enchantment is checked (not for entities) and Only PLAYER is always returned EXCEPT for Entity events.
	 * @param e Event stored (Multi-events)
	 */
	public abstract void onEvent(EventStore e);
	
	public String getTargets() {
		if(getTarget() == null) return "None";
		return getTarget().toString();
	}
	
	public String getDescription() {
		return description;
	}
	@Override
	public org.bukkit.enchantments.EnchantmentTarget getItemTarget() {
		return org.bukkit.enchantments.EnchantmentTarget.ALL;// Just to override! To avoid StackOverflowError!
	}
	public EnchantmentTarget getTarget() {
		return target;
	}
	@Override
	public int getMaxLevel() {
		return maxLevel;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public int getStartLevel() {
		return startLevel;
	}
	public List<Enchantment> getConflictedEnchantment() {
		return conflictsWith;
	}
	// canEnchantItem() not overrided || CHANGED: Overrided to avoid StackOverflowError
	@Override
	public boolean canEnchantItem(ItemStack item) {
		return getTarget().includes(item) && !conflict(item);
	}
	public boolean conflict(ItemStack item) {
		if(item.getItemMeta().hasEnchants()) {
            try {
                for(Enchantment ench : item.getItemMeta().getEnchants().keySet()) {
                    if(conflictsWith(ench)) return true;
                }
            }catch(Exception ignored) {}
        }
		return false;
	}
	@Override
	public boolean conflictsWith(Enchantment other) {
		try {
            if(getConflictedEnchantment().contains(other)) return true;
            for(Enchantment ench : getConflictedEnchantment()) {
                if(ench.getId() == other.getId()) return true;
            }
        }catch(Exception ignored) {}
		return false;
	}

	@Override
	public String toString() {
		return String.format("%s{id=%s,display:\"%s\",target=%s,lvl=%s-%s}", getName(), getId(), getDisplayName(), getItemTarget().name(), getStartLevel(), getMaxLevel());
	}
}
