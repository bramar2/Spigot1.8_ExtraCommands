package me.bramar.extracommands.customenchants;

import com.google.common.collect.Lists;
import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.config.ConfigEnchantLoader;
import me.bramar.extracommands.customenchants.java.JavaEnchantLoader;
import me.bramar.extracommands.events.BlockDropItemEvent;
import me.bramar.extracommands.events.ItemList;
import me.bramar.extracommands.events.PlayerLandEvent;
import me.bramar.extracommands.events.PreRegisteringEnchant;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static me.bramar.extracommands.customenchants.CustomEnchantment.*;
public final class EnchantLoader implements Listener {
	private final Main main;
	private static EnchantLoader instance;
	YamlConfiguration config;
	List<String> disabled;
	int tick;

	public int getCurrentTick() {
		return tick;
	}
	
	private final HashMap<UUID, Vector> playerVectorData = new HashMap<>();
	private final HashMap<Location, Player> blockBreakInfo = new HashMap<>();
	private final HashMap<Location, ItemList> blockDrops = new HashMap<>();

	public List<CustomEnchantment> getCustomEnchantments(ItemStack item) {
		try {
			net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
			NBTTagList list = (NBTTagList) nmsStack.getTag().get(ENCHANTED_TAG);
			List<CustomEnchantment> itemEnchants = new ArrayList<>();
			for(int i = 0; i < list.size(); i++) {
				try {
					Enchantment en = getById(list.get(i).getInt("id"));
					if(en instanceof CustomEnchantment)
						itemEnchants.add((CustomEnchantment) en);
				}catch(Exception ignored) {}
			}
			return itemEnchants;
		}catch(Exception ignored) {}
		return new ArrayList<>();
	}

	public Map.Entry<ItemStack, Integer> enchant(ItemStack item, ItemStack add) {
		try {
			net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(add);
			int cost = 0;
			int repairCost = nmsStack.getRepairCost();

			boolean added = false;
			if(add.getType() == Material.ENCHANTED_BOOK) {
				EnchantmentStorageMeta meta = (EnchantmentStorageMeta) add.getItemMeta();
				for(Map.Entry<Enchantment, Integer> en : meta.getStoredEnchants().entrySet()) {
					if(en instanceof CustomEnchantment) {
						try {
							CustomEnchantment ce = (CustomEnchantment) en.getKey();
							if(!ce.canEnchantItem(item)) continue;
							ItemStack temp = enchant(item, ce, en.getValue());
							boolean eq = Objects.equals(temp, item);
							added |= !eq;
							if(!eq) {
								cost += ce.getMultiplier() * en.getValue();
								item = temp;
							}
						}catch(Exception ignored) {}
					}else {
						try {
							ItemStack temp = item.clone();
							int before = temp.getEnchantmentLevel(en.getKey());
							int lvl = before == 0 ? en.getValue() : before == en.getValue() ? en.getValue() + 1 : Math.min(Math.max(before, en.getValue()), en.getKey().getMaxLevel());
							if(!en.getKey().canEnchantItem(item)) continue;
							temp.addEnchantment(en.getKey(), lvl);
							boolean eq = Objects.equals(temp, item);
							added |= !eq;
							if(!eq) {
								cost += CraftEnchantment.getRaw(en.getKey()).getRandomWeight() * en.getValue();
								item = temp;
							}
						}catch(Exception ignored) {}
					}
				}
				cost += repairCost;
				if(added) {
					// Changing instance of nmsStack is not a problem because it will return afterwards
					nmsStack = CraftItemStack.asNMSCopy(item);
					nmsStack.setRepairCost(repairCost * 2 + 1);
					return Main.getInstance().immutableEntry(CraftItemStack.asBukkitCopy(nmsStack), cost);
				}else return null;
			}
			added = false;
			// Vanilla anvil implementation
			for(Map.Entry<Enchantment, Integer> vanillaEnchants : add.getEnchantments().entrySet()) {
				try {
					net.minecraft.server.v1_8_R3.Enchantment en = CraftEnchantment.getRaw(vanillaEnchants.getKey());
					int weight = en.getRandomWeight();
					ItemStack temp = item.clone();
					if(!vanillaEnchants.getKey().canEnchantItem(item)) continue;
					temp.addEnchantment(vanillaEnchants.getKey(), vanillaEnchants.getValue());
					boolean eq = Objects.equals(item, temp);
					added |= !eq;
					if(!eq) {
						cost += weight * vanillaEnchants.getValue();
						item = temp;
					}
				}catch(Exception ignored) {}
			}
			//
			if(nmsStack.hasTag()) {
				NBTTagList ce = (NBTTagList) nmsStack.getTag().get("CE");
				for(int i = 0; i < ce.size(); i++) {
					NBTTagCompound ench = ce.get(i);
					CustomEnchantment customEnchantment = getByString(String.valueOf(ench.getInt("id")));
					if(customEnchantment == null || !customEnchantment.canEnchantItem(item)) continue;
					ItemStack temp = enchant(item, customEnchantment, ench.getInt("lvl"));
					boolean eq = Objects.equals(temp, item);
					added |= !eq;
					if(!eq) item = temp;
				}
			}
			cost += repairCost;
			if(added) return Main.getInstance().immutableEntry(item, cost);
		}catch(Exception ignored) {}
		return null;
	}

	public ItemStack enchant(ItemStack item, CustomEnchantment ench, int level) {
		try {
			if(!ench.canEnchantItem(item)) return item;
			if(ench.getConflictedEnchantment() != null) for(Enchantment en : ench.getConflictedEnchantment()) {
				if(item.containsEnchantment(en)) return item;
				if(en instanceof CustomEnchantment) if(ench.hasEnchant(item)) return item;
			}
			if(ench.hasEnchant(item)) {
				// Upgrading system
				// EXAMPLE: (variables= x:OldLevel z:newLevel)
				// if z < x: keep level as x
				// if x == z: add level to 1
				// if z > x: keep new level
				int x = ench.getEnchantLevel(item);
				if(level >= x) {
					if(x == level)
						item = ench.addEnchantTag(item, Math.min(x+1,ench.getMaxLevel()));
					else
						item = ench.addEnchantTag(item, Math.min(x,ench.getMaxLevel()));
				}
			}else item = ench.addEnchantTag(item, level);
			ItemMeta meta = item.getItemMeta();
			List<String> list = new ArrayList<>(meta.getLore() == null ? new ArrayList<>() : meta.getLore());
			List<String> addedLore = new ArrayList<>();
			if(list.size() != 0) for(CustomEnchantment e : EnchantLoader.getInstance().getEnchants())
				list.removeIf(str -> str.startsWith(e.getDisplayName() + " "));
			for(CustomEnchantment en : getCustomEnchantments(item)) {
				addedLore.add(en.getDisplayName() + " " + toRoman(en.getEnchantLevel(item)));
			}
			list.addAll(addedLore);
			meta.setLore(list);
			item.setItemMeta(meta);
		}catch(Exception ignored) {}
		return item;
	}

	public static EnchantLoader getInstance() {
		return instance;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBreak(BlockBreakEvent e) {
		final Material mat = e.getBlock().getType();
		final MaterialData matData = e.getBlock().getState().getData();
		if(e.getPlayer().getGameMode() == GameMode.CREATIVE || e.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
		blockBreakInfo.put(e.getBlock().getLocation(), e.getPlayer());
		// Schedule event
		schedule(() -> {
			Player p = e.getPlayer();
			Location loc = e.getBlock().getLocation();
			ItemList drops = get(blockDrops, (loc2) -> sameBlock(loc, loc2)); // item drops of block
			if(drops == null)
				return; // Don't call the event, if there are no drops e.g doTileDrops gamerule is set to false
			BlockDropItemEvent event = new BlockDropItemEvent(p, loc, drops, mat, matData);
			Bukkit.getServer().getPluginManager().callEvent(event);
			if(!drops.same(event.getItemList())) throw new UnsupportedOperationException("A plugin has done an illegal reflection action, that modifies a final variable.");
			ItemList il = (ItemList) event.getItemList();
			il.runConsumers();
			if(event.isCancelled()) il.despawnAll();
			else il.despawnUnused();
			// Dispose
			removeIf(blockBreakInfo, (loc2) -> sameBlock(loc, loc2));
			removeIf(blockDrops, (loc2) -> sameBlock(loc, loc2));
		}, 2);
		//
	}
	public void schedule(Runnable run, int tickDelay) {
		main.getServer().getScheduler().
				scheduleSyncDelayedTask(main, run, tickDelay);
	}
	public boolean sameBlock(Location loc1, Location loc2) {
		return loc1.getBlockX() == loc2.getBlockX() &&
				loc1.getBlockY() == loc2.getBlockY() &&
				loc1.getBlockZ() == loc2.getBlockZ();
	}
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent e) {
		Location loc = e.getLocation();
		final boolean[] bool = new boolean[] {false};
		blockBreakInfo.forEach((loc2, p) -> {
			if(sameBlock(loc, loc2)) {
				bool[0] = true;
			}
		});
		if(!bool[0]) return;
		bool[0] = false;
//		final List<ItemList> list = new ArrayList<>(10);
		final ItemList[] array = new ItemList[] {null};
		blockDrops.forEach((loc2, itemList) -> {
			if(sameBlock(loc2, loc)) {
				array[0] = itemList;
				bool[0] = true;
			}
		});
		if(bool[0]) array[0].add(e.getEntity());
		else array[0] = new ItemList(e.getEntity());
		removeIf(blockDrops, (loc2) -> sameBlock(loc, loc2));
		blockDrops.put(loc, new ItemList(array[0]));
	}
	public <K, V> void removeIf(Map<K, V> map, Predicate<K> predicate) {
		Map<K, V> newMap = new HashMap<>();
		map.forEach((a, b) -> {
			if(!predicate.test(a)) newMap.put(a, b);
		});
		map.clear();
		map.putAll(newMap);
	}
	public <K, V> V get(HashMap<K, V> map, Predicate<K> predicate) {
		for(Map.Entry<K, V> entry : map.entrySet()) {
			if(predicate.test(entry.getKey())) return entry.getValue();
		}
		return null;
	}
	private <E extends String> boolean containsIgnoreCase(List<E> l, String str) {
		return l.stream().anyMatch((s) -> s.equalsIgnoreCase(str));
	}
	private List<CustomEnchantment> enchants = new ArrayList<>();
	public void registerEnchant(CustomEnchantment ench) {
		enchants.add(ench);
	}
	public EnchantLoader() {
		if(instance != null) throw new UnsupportedOperationException();
		instance = this;
		main = Main.getInstance();
		main.getServer().getPluginManager().registerEvents(this, main);
		main.getCommand("customenchantment").setExecutor(new CustomEnchantCommand(this));
		main.getServer().getPluginManager().registerEvents(new EnchantRegister(), main);
		new ConfigEnchantLoader();
		new JavaEnchantLoader(this);
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {
			for(Player p : main.getServer().getOnlinePlayers()) {
				if(((LivingEntity)p).isOnGround()) {
					if(playerVectorData.containsKey(p.getUniqueId())) {
						if(p.getLocation().getBlock().getType() != Material.WATER) main.getServer().getPluginManager().callEvent(new PlayerLandEvent(p, p.getUniqueId(), playerVectorData.get(p.getUniqueId())));
						playerVectorData.remove(p.getUniqueId());
					}
				}else {
					if(p.isFlying()) {
						playerVectorData.remove(p.getUniqueId());
					}else {
						playerVectorData.remove(p.getUniqueId());
						playerVectorData.put(p.getUniqueId(), p.getVelocity());
					}
				}
			}
		}, 0L, 5L);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> tick++, 1L, 1L);
		main.getLogger().info("[CustomEnchants] Loading configuration...");
		main.saveResource("enchants.yml", false);
		this.config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + File.separator + "enchants.yml"));
		this.disabled = config.getStringList("disabled");
		try {
			Bukkit.getServer().getPluginManager().callEvent(new PreRegisteringEnchant(this));
			final Field enchField = Enchantment.class.getDeclaredField("acceptingNew");
			enchField.setAccessible(true);
			enchField.set(null, true);
			int count = 0;
			int failCount = 0;
			List<String> disabledEnchants = new ArrayList<>();
			enchants = enchants.stream().filter((c) -> {
				try {
					if(disabledEnchants.contains(String.valueOf(c.getId())) || containsIgnoreCase(disabledEnchants, c.getName())) {
						disabledEnchants.add(c.getName());
						return false;
					}
				}catch(Exception ignored) {}
				return true;
			}).collect(Collectors.toList());
			main.getLogger().info("[CustomEnchants] The following enchants were disabled in config: " + disabledEnchants);
			for(CustomEnchantment ench : enchants) {
				Throwable throwable = ench.registerEnchantment();
				if(throwable != null) {
					main.getLogger().info("[CustomEnchants] Enchantment " + ench.getName() + " failed to register: " + throwable);
					failCount++;
				}else count++;
			}
			enchField.set(null, false);
			if(failCount<=0) main.getLogger().info("[CustomEnchants] All enchantments successfully registered! [" + count + "]");
			else {
				if(count <= 0) main.getLogger().severe("[CustomEnchants] No enchants was successfully registered! Did the server reload? A total of " + failCount + " (all) enchantments did not register properly.");
				else main.getLogger().warning("[CustomEnchants] " + count + " enchantments successfully registered, but " + failCount + " enchantments did not");
			}
		}catch(Exception e1) {
			e1.printStackTrace();
			main.getLogger().severe("[CustomEnchants] Failed to load enchants internally! Simplified: " + main.getSimplified(e1,
					CustomEnchantment.class,
					ArrayList.class,
					List.class,
					Collection.class,
					Set.class,
					EnchantLoader.class,
					Enchantment.class));
		}
	}
	public void openGui(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9*3, "Custom Enchantment Anvil GUI");
		ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15);
		for(int i = 0; i < 9*3; i++)
			inv.setItem(i, glass);
		glass = new ItemStack(glass); // Cloning process
		net.minecraft.server.v1_8_R3.ItemStack nmsGlass = CraftItemStack.asNMSCopy(glass);
		NBTTagCompound tag = nmsGlass.hasTag() ? nmsGlass.getTag() : new NBTTagCompound();
		tag.setBoolean(ENCH_TAG + "Anvil", true);
		nmsGlass.setTag(tag);
		glass = CraftItemStack.asBukkitCopy(nmsGlass);
		inv.setItem(0, glass);
		inv.setItem(10, null);
		inv.setItem(13, null);
		inv.setItem(16, getItem(Material.WOOL, "&aOutput", 1, (short) 14));
		
		// Items
		ItemStack wool = getItem(Material.WOOL, "&cInvalid item!", 1, (short) 14);
		ItemStack xp = setLore(getItem(Material.EXP_BOTTLE, "&eLevel Cost [Vanilla]"), "&cNone!");
		ItemStack paper = setLore(getItem(Material.PAPER, "&fAnvil GUI Info"),
				"&aThis is a Custom Enchantment Anvil GUI",
				"",
				"&bYou can combine an item with a custom",
				"&benchantment book to enchant it.",
				"",
				"&9Put weapon/armor/tools in Slot 1",
				"&9Put custom enchantment book in Slot 2",
				"&9Result is at slot 3",
				"&9If it doesn't work check at the &cred",
				"&9wool");
		inv.setItem(4, wool);
		inv.setItem(18, paper);
		inv.setItem(22, xp);
		p.openInventory(inv);
	}

	@EventHandler
	public void anvilGUI(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) return;
		if(e.getClickedInventory() instanceof PlayerInventory) return;
		if(e.getView().getTopInventory().getItem(0) == null) return;
		net.minecraft.server.v1_8_R3.ItemStack firstItem = CraftItemStack.asNMSCopy(e.getView().getTopInventory().getItem(0));
		if(!firstItem.hasTag()) return;
		if(!firstItem.getTag().hasKey(ENCH_TAG + "Anvil")) return;
		// Anvil GUI
		e.setCancelled(e.getSlot() != 10 && e.getSlot() != 13 && e.getSlot() != 16);
		
		Inventory inv = e.getView().getTopInventory();
		
		Player p = (Player) e.getWhoClicked();
		if(e.getSlot() == 16) {
			// Take????
			if(e.getAction() == InventoryAction.DROP_ALL_CURSOR ||
					e.getAction() == InventoryAction.DROP_ALL_SLOT ||
					e.getAction() == InventoryAction.DROP_ONE_CURSOR || 
					e.getAction() == InventoryAction.DROP_ONE_SLOT ||
					e.getAction() == InventoryAction.HOTBAR_SWAP || 
					e.getAction() == InventoryAction.NOTHING || 
					e.getAction() == InventoryAction.SWAP_WITH_CURSOR || 
					e.getAction() == InventoryAction.PLACE_ALL ||
					e.getAction() == InventoryAction.PLACE_ONE || 
					e.getAction() == InventoryAction.PLACE_SOME) {
				e.setCancelled(true);
				return;
			}
			System.out.println("Action: " + e.getAction() + " | ClickedInventory: " + e.getClickedInventory().getClass());
			if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && p.getInventory().firstEmpty() == -1) {
				e.setCancelled(true);
				return;
			}
			if(inv.getItem(16) == null) {
				e.setCancelled(true);
				return;
			}else if(inv.getItem(16).getType() == Material.AIR || inv.getItem(16).getType() == Material.WOOL) {
				e.setCancelled(true);
				return;
			}
			try {
				int neededLevels = Integer.parseInt(inv.getItem(22).getItemMeta().getLore().get(0).replace("&a", "").replace(ChatColor.COLOR_CHAR + "a", "").replace("levels", "").replace("level", "").replace("xp", "").replace(" ", ""));
				if(p.getGameMode() != GameMode.CREATIVE && p.getLevel() < neededLevels) {
					e.setCancelled(true);
					p.sendMessage(ChatColor.RED + "You don't have the needed EXP!");
					return;
				}
				if(p.getGameMode() != GameMode.CREATIVE)
					p.setLevel(p.getLevel() - neededLevels);
				p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 0);
				inv.setItem(10, null);
				inv.setItem(13, null);
				p.playSound(p.getLocation(), Sound.ANVIL_USE, 1, 0);
				if(p.getGameMode() == GameMode.CREATIVE)
					p.sendMessage(ChatColor.RED + "Your levels did not get removed because of your creative mode");
				else
					p.sendMessage(ChatColor.RED + "Your levels have been removed by " + neededLevels);
				inv.setItem(4, getItem(Material.WOOL, "&cInvalid item!", 1, (short) 14));
				Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> inv.setItem(16, getItem(Material.WOOL, "&aOutput", 1, (short) 14)), 1L);
				Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> inv.setItem(16, getItem(Material.WOOL, "&aOutput", 1, (short) 14)), 2L);
				Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> p.updateInventory(), 3L);
				return;
			}catch(Exception e1) {
				e.setCancelled(true);
			}
		}
		
		ItemStack item = null;
		try {
			item = new ItemStack(inv.getItem(10));
		}catch(Exception ignored) {}
		// Refresh EXP and Wool
		boolean calculateXP = false;
		CustomEnchantment ce = null;
		int lvl = -1;
		// Wool
//		Map.Entry<ItemStack, Integer> xp = null;
		if(inv.getItem(10) == null || inv.getItem(13) == null) {
			inv.setItem(4, getItem(Material.WOOL, "&cInvalid item!", 1, (short) 14));
			inv.setItem(16, getItem(Material.WOOL, "&aOutput", 1, (short) 14));
		}else {
			// Enchantment conflict and also Setting result.
			net.minecraft.server.v1_8_R3.ItemStack nmsBook = CraftItemStack.asNMSCopy(inv.getItem(13));
			if(!nmsBook.hasTag()) {
				inv.setItem(4, getItem(Material.WOOL, "&cInvalid custom enchantment book!", 1, (short) 14));
				inv.setItem(16, getItem(Material.WOOL, "&aOutput", 1, (short) 14));
			}else if(!nmsBook.getTag().hasKey(ENCH_TAG)) {
				inv.setItem(4, getItem(Material.WOOL, "&cInvalid custom enchantment book!", 1, (short) 14));
				inv.setItem(16, getItem(Material.WOOL, "&aOutput", 1, (short) 14));
			}else {
				Enchantment ench = Enchantment.getById(nmsBook.getTag().getCompound(ENCH_TAG).getInt("id"));
				lvl = nmsBook.getTag().getCompound(ENCH_TAG).getInt("lvl");
				if(!(ench instanceof CustomEnchantment)) {
					inv.setItem(4, getItem(Material.WOOL, "&cNot a custom enchantment book!", 1, (short) 14));
					inv.setItem(16, getItem(Material.WOOL, "&aOutput", 1, (short) 14));
				}else {
					ce = (CustomEnchantment) ench;
					boolean conflicts = false;
					List<String> conflicted = Lists.newArrayList();
					if(ce.getConflictedEnchantment() != null) for(Enchantment en : ce.getConflictedEnchantment()) {
						if(item.containsEnchantment(en)) {
							conflicts = true;
							conflicted.add(ChatColor.GRAY + en.getName());
						}
						if(en instanceof CustomEnchantment) if(((CustomEnchantment) en).hasEnchant(item)) {
							conflicts = true;
							conflicted.add(ChatColor.GRAY + ((CustomEnchantment)en).getDisplayName());
						}
					}
					if(conflicts) {
						inv.setItem(4, setLore(getItem(Material.WOOL, "&cItem has conflicted enchantment:", 1, (short) 14), conflicted.toArray(new String[conflicted.size() - 1])));
						inv.setItem(16, getItem(Material.WOOL, "&aOutput", 1, (short) 14));
					}else {
						if(ce.canEnchantItem(item)) {
							inv.setItem(4, getItem(Material.WOOL, "&aWorks!"));
							inv.setItem(16, enchant(item, ce, lvl));
							calculateXP = true;
						}else {
							inv.setItem(4, setLore(getItem(Material.WOOL, "&cItem is not compatible with enchantment:", 1, (short) 14), "&9Enchantment Target: " + ce.getTargets()));
							inv.setItem(16, getItem(Material.WOOL, "&aOutput", 1, (short) 14));
						}
					}
				}
			}
//			xp = enchant(item, inv.getItem(13));
//			if(xp != null) {
//				inv.setItem(16, xp.getKey());
//				inv.setItem(4, getItem(Material.WOOL, "&aWorks!"));
//			}else {
//				inv.setItem(4, setLore(getItem(Material.WOOL, "&cFailed to enchant, possible reasons:", 1, (short) 14), "- Not compatible with item (Sharpness with Smite)\n- Lvl above max/below min\n- Doesn't have any enchant"));
//				inv.setItem(16, getItem(Material.WOOL, "&aOutput", 1, (short) 14));
//			}
		}
		if(calculateXP && ce != null && lvl != -1 && inv.getItem(16) != null
//		xp != null && inv.getItem(16) != null
		) {
			int levels = 0;
			boolean hasRepairCost = false;
			int repairCost = 0;
			int newRepairCost;
			try {
				net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
				if(nmsItem.hasTag()) {
					if(nmsItem.getTag().hasKey("RepairCost")) {
						hasRepairCost = true;
						repairCost = nmsItem.getTag().getInt("RepairCost");
					}
				}
			}catch(Exception ignored) {}
			if(hasRepairCost) levels += repairCost;
			newRepairCost = repairCost * 2 + 1;
			int bookValue = ce.getMultiplier() * lvl;
			levels += bookValue;
			inv.setItem(22, setLore(getItem(Material.EXP_BOTTLE, "&eLevel Cost [Vanilla]"), "&a" + levels + " levels"));
			net.minecraft.server.v1_8_R3.ItemStack result = CraftItemStack.asNMSCopy(new ItemStack(inv.getItem(16)));
			NBTTagCompound tag = result.hasTag() ? result.getTag() : new NBTTagCompound();
			tag.setInt("RepairCost", newRepairCost);
			result.setTag(tag);
			inv.setItem(16, CraftItemStack.asBukkitCopy(result));
//			inv.setItem(22, setLore(getItem(Material.EXP_BOTTLE, "&eLevel Cost [Vanilla]"), "&a" + xp.getValue() + " levels"));
		}else inv.setItem(22, setLore(getItem(Material.EXP_BOTTLE, "&eLevel Cost [Vanilla]"), "&cNone! [Did not calculate]"));
		if(e.getAction() != InventoryAction.UNKNOWN) Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(main, () -> anvilGUI(new InventoryClickEvent(e.getView(), e.getSlotType(), e.getSlot(), e.getClick(), InventoryAction.UNKNOWN)), 2L);
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {
		try {
			if(!(e.getPlayer() instanceof Player)) return;
			Player p = (Player) e.getPlayer();
			net.minecraft.server.v1_8_R3.ItemStack firstItem = CraftItemStack.asNMSCopy(e.getView().getTopInventory().getItem(0));
			if(!firstItem.hasTag()) return;
			if(!firstItem.getTag().hasKey(ENCH_TAG + "Anvil")) return;
			ItemStack slotOne = e.getView().getTopInventory().getItem(10);
			ItemStack slotTwo = e.getView().getTopInventory().getItem(13);
			if(slotOne != null) if(slotOne.getType() != Material.AIR) {
				if(e.getInventory().firstEmpty() == -1) {
					p.getWorld().dropItem(p.getLocation(), slotOne);
					p.sendMessage(ChatColor.RED + "Your item has been dropped because your inventory was full!");
				}else p.getInventory().addItem(slotOne);
			}
			if(slotTwo != null) if(slotTwo.getType() != Material.AIR) {
				if(e.getInventory().firstEmpty() == -1) {
					p.getWorld().dropItem(p.getLocation(), slotTwo);
					p.sendMessage(ChatColor.RED + "Your item has been dropped because your inventory was full!");
				}else p.getInventory().addItem(slotTwo);
			}
		}catch(Exception ignored) {}
	}
	
	public ItemStack setLore(ItemStack item, String... lore) {
		if(lore == null) return item;
		if(lore.length == 0) return item;
		List<String> newLore = Arrays.stream(lore).map(l -> ChatColor.translateAlternateColorCodes('&', l)).collect(Collectors.toList());
		ItemMeta meta = item.getItemMeta();
		meta.setLore(newLore);
		item.setItemMeta(meta);
		return item;
	}
	
	public ItemStack getItem(Material mat, String name, int amount, short damage) {
		ItemStack item = new ItemStack(mat, amount, damage);
		ItemMeta meta = item.getItemMeta();
		if(name != null) meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		item.setItemMeta(meta);
		return item;
	}
	
	public ItemStack getItem(Material mat, String name) {
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		if(name != null) meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		item.setItemMeta(meta);
		return item;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		try {
			// Checks if the entity has an item
			if(e.getPlayer().getItemInHand().getType() == Material.AIR || e.getPlayer().getItemInHand().getType() == null) throw new Exception();
			if(e.getPlayer().getItemInHand().getType().isBlock()) return; // To disable the GUI opening when the entity is right-clicking block
		}catch(Exception ignored) {}
		if(e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(e.getClickedBlock().getType() == Material.ANVIL && e.getPlayer().isSneaking()) {
				e.setCancelled(true);
				openGui(e.getPlayer());
			}
		}
	}
	public final HashMap<Location, Player> blockExpInfo = new HashMap<>();

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if(e.getPlayer().getGameMode() == GameMode.SURVIVAL
		|| e.getPlayer().getGameMode() == GameMode.CREATIVE) {
			blockExpInfo.put(e.getBlock().getLocation(), e.getPlayer());
			schedule(() -> blockExpInfo.remove(e.getBlock().getLocation()), 200);
		}
	}

	public final HashMap<Entity, ItemStack> bowData = new HashMap<>();
	@EventHandler
	public void bowItemSave(EntityShootBowEvent e) {
		bowData.put(e.getProjectile(), e.getBow());
	}
	public ItemStack getBow(Entity arrow) {
		ItemStack bow = null;
		ArrayList<Map.Entry<Entity, ItemStack>> list = new ArrayList<>(bowData.entrySet());
		for(int i = 0; i < bowData.size(); i++) {
			ItemStack item = list.get(i).getValue();
			Entity arr = list.get(i).getKey();
			if(arrow.getEntityId() == arr.getEntityId() && arrow.getType() == arr.getType() && arrow.getUniqueId().equals(arr.getUniqueId())) {
				bow = item;
				break;
			}
		}
		return bow;
	}

	public void disable() {
		main.getLogger().info("[CustomEnchants] Disabling all enchants!");
		try {
			Map<?, ?> byId;
			Map<?, ?> byName;
			Field fieldById = Enchantment.class.getDeclaredField("byId");
			Field fieldByName = Enchantment.class.getDeclaredField("byName");
			fieldById.setAccessible(true);
			fieldByName.setAccessible(true);
			byId = (Map<?, ?>) fieldById.get(null);
			byName = (Map<?, ?>) fieldByName.get(null);
			new HashMap<>(byId).forEach((id, enchant) -> {
				if(enchant instanceof CustomEnchantment) byId.remove(id);
			});
			new HashMap<>(byName).forEach((name, enchant) -> {
				if(enchant instanceof CustomEnchantment) byName.remove(name, enchant);
			});
			main.getLogger().info("[CustomEnchants] Successfully disabled all enchants!");
		}catch(Exception e1) {
			e1.printStackTrace();
			main.getLogger().severe("[CustomEnchants] Failed to disable enchants (using reflection). If this is a reload, errors will occur!");
		}
	}

	/**
	 * Returns a <code>CustomEnchantment</code> from the string which will check for the ID and internal name.<br>
	 * @param s Either name (internal) or id
	 * @return The custom enchantment (null if not found)
	 */
	@Nullable
	public CustomEnchantment getByString(String s) {
		return enchants.stream().filter((c) -> c.getName().equalsIgnoreCase(s) || String.valueOf(c.getId()).equalsIgnoreCase(s)).findFirst().orElse(null);
	}

	HashMap<UUID, Long> updateInv = new HashMap<>();

	@EventHandler
	public void updateItems(PlayerInteractEvent e) {
		UUID uuid = e.getPlayer().getUniqueId();
		if(updateInv.containsKey(uuid)) {
			if(System.currentTimeMillis() - updateInv.get(uuid) <= TimeUnit.SECONDS.toMillis(30)) return;
			updateInv.remove(uuid);
		}
		PlayerInventory inv = e.getPlayer().getInventory();
		boolean putCooldown = false;
		for(int i = 0; i < inv.getSize(); i++) {
			ItemStack o = update(inv.getItem(i));
			putCooldown |= o != null;
			if(o != null) inv.setItem(i, o);
		}
		if(putCooldown) updateInv.put(uuid, System.currentTimeMillis());
	}
	ItemStack update(ItemStack item) {
		try {
			net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
			if(nmsStack.hasTag() && nmsStack.getTag().hasKey("CE")) {
				NBTTagList l = (NBTTagList) nmsStack.getTag().get("CE");
				boolean update = false;
				for(int i = 0; i < l.size(); i++) {
					try {
						NBTTagCompound ench = l.get(i);
						int id = ench.getInt("id");
						if(ench.hasKey("name")) {
							CustomEnchantment ce = getByString(ench.getString("name"));
							if(ce != null && ce.getId() != id) {
								ench.setInt("id", ce.getId());
								update = true;
							}
						}else {
							CustomEnchantment ce = getByString(String.valueOf(id));
							if(ce != null) {
								ench.setString("name", ce.getName());
								update = true;
							}
						}
					}catch(Exception ignored) {}
				}
				nmsStack.getTag().set("CE", l);
				return (update) ? CraftItemStack.asBukkitCopy(nmsStack) : null;
			}
		}catch(Exception ignored) {}
		return null;
	}

	public List<CustomEnchantment> getEnchants() {
		return enchants;
	}
}
