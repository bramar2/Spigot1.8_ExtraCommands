package me.bramar.extracommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import me.bramar.extracommands.AnvilGUI.AnvilClickEvent;
import me.bramar.extracommands.AnvilGUI.AnvilSlot;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

public class InventoryEvents implements Listener {
    Main main;
    public InventoryEvents(Main main) {
        main.getServer().getPluginManager().registerEvents(this, main);
        this.main = main;
    }
    // All the inventory events over here

    public void grantMain(Player p, User user) {
        Inventory inv = Bukkit.createInventory(null, 4*9, ChatColor.GREEN + "User GUI [LuckPerms]");
        ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        for(int i = 0; i < 9; i++) {
            inv.setItem(i, pane);
        }
        inv.setItem(9, pane);
        inv.setItem(17, pane);
        inv.setItem(18, pane);
        inv.setItem(26, pane);
        for(int i = 27; i <= 35; i++) {
            inv.setItem(i, pane);
        }
        ItemStack name = new ItemStack(Material.SIGN);
        ItemMeta nameMeta = name.getItemMeta();
        try {
            nameMeta.setDisplayName(ChatColor.GRAY + "Name: " + ChatColor.WHITE + user.getUsername());
        }catch(Exception e1) {
            nameMeta.setDisplayName(ChatColor.GRAY + "Name: [Not found]");
        }
        name.setItemMeta(nameMeta);
        ItemStack uuid = new ItemStack(Material.SIGN);
        ItemMeta uuidMeta = uuid.getItemMeta();
        uuidMeta.setDisplayName(ChatColor.GRAY + "UUID: " + ChatColor.WHITE + user.getUniqueId());
        uuid.setItemMeta(uuidMeta);
        inv.setItem(30, name);
        inv.setItem(32, uuid);
        inv.setItem(10, rename(Material.BOOK_AND_QUILL, ChatColor.GREEN + "Player Information"));
        inv.setItem(11, rename(Material.BARRIER, ChatColor.RED + "Permission"));
        inv.setItem(12, rename(Material.TRAPPED_CHEST, ChatColor.YELLOW + "Parent"));
        inv.setItem(13, rename(Material.COMMAND, "Meta"));
        inv.setItem(14, rename(Material.WEB, ChatColor.WHITE + "Web Editor"));
        inv.setItem(15, rename(Material.WOOD_AXE, ChatColor.GREEN + "Clone"));
        p.closeInventory();
        p.openInventory(inv);
    }
    @SuppressWarnings("deprecation")
    public void grantPlayerInfo(Player p, User selected) {
        Inventory inv = Bukkit.createInventory(null, 4*9, ChatColor.GREEN + "User GUI [LuckPerms] [Info]");
        ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        for(int i = 0; i < 9; i++) {
            inv.setItem(i, pane);
        }
        inv.setItem(9, pane);
        inv.setItem(17, pane);
        inv.setItem(18, pane);
        inv.setItem(26, pane);
        for(int i = 27; i <= 35; i++) {
            inv.setItem(i, pane);
        }
        ItemStack name = new ItemStack(Material.SIGN);
        ItemMeta nameMeta = name.getItemMeta();
        try {
            nameMeta.setDisplayName(ChatColor.GRAY + "Name: " + ChatColor.WHITE + selected.getUsername());
        }catch(Exception e1) {
            nameMeta.setDisplayName(ChatColor.GRAY + "Name: [Not found]");
        }
        name.setItemMeta(nameMeta);
        ItemStack uuid = new ItemStack(Material.SIGN);
        ItemMeta uuidMeta = uuid.getItemMeta();
        uuidMeta.setDisplayName(ChatColor.GRAY + "UUID: " + ChatColor.WHITE + selected.getUniqueId());
        uuid.setItemMeta(uuidMeta);
        inv.setItem(30, name);
        inv.setItem(32, uuid);
        inv.setItem(31, rename(Material.WOOD_DOOR, ChatColor.GRAY + "Go back"));
        inv.setItem(10, rename((Bukkit.getPlayer(selected.getUniqueId()) == null ? DyeColor.RED.getWoolData() : DyeColor.GREEN.getWoolData()), (Bukkit.getPlayer(selected.getUniqueId()) == null ? Material.WOOL : Material.WOOL), (Bukkit.getPlayer(selected.getUniqueId()) == null ? ChatColor.WHITE + "Status: " + ChatColor.RED + "Offline" : ChatColor.WHITE + "Status: " + ChatColor.GREEN + "Online")));
        List<String> groupStrings = new ArrayList<>();
        List<Group> groups = new ArrayList<>(selected.getInheritedGroups(selected.getQueryOptions()));
        groups.forEach((groupObj) -> groupStrings.add(ChatColor.GREEN + "- " + groupObj.getIdentifier().getName()));
        inv.setItem(11, rename(Material.TRAPPED_CHEST, ChatColor.YELLOW + "Parent Groups", groupStrings));
        if(Bukkit.getPlayer(selected.getUniqueId()) != null) {
            Optional<ImmutableContextSet> optional = main.lp.getContextManager().getContext(selected);
            if(!optional.isPresent()) inv.setItem(12, rename(Material.SIGN, ChatColor.BLUE + "Contexts", ChatColor.GRAY + "None!"));
            else {
                ImmutableContextSet contextset = optional.get();
                List<String> contexts = new ArrayList<>();
                contextset.forEach((ctx) -> contexts.add(ChatColor.WHITE + "'" + ChatColor.BOLD + ctx.getKey() + ChatColor.RESET + ChatColor.WHITE + "': " + "'" + ChatColor.BOLD + ctx.getValue() + ChatColor.RESET + ChatColor.WHITE + "'"));
                inv.setItem(12, rename(Material.SIGN, ChatColor.BLUE + "Contexts", contexts));
            }
        }else inv.setItem(12, rename(Material.BARRIER, ChatColor.RED + "Context not available. The entity is not online!"));
        String prefix = selected.getCachedData().getMetaData(selected.getQueryOptions()).getPrefix();
        String suffix = selected.getCachedData().getMetaData(selected.getQueryOptions()).getSuffix();
        inv.setItem(13, rename(Material.PAPER, ChatColor.GREEN + "Prefix", ChatColor.WHITE + (prefix != null ? "'" + prefix + ChatColor.WHITE + "'" : "None!"), ChatColor.WHITE + (prefix != null ? "'" + ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.WHITE + "'" : "")));
        inv.setItem(14, rename(Material.PAPER, ChatColor.GREEN + "Suffix", ChatColor.WHITE + (suffix != null ? "'" + suffix + ChatColor.WHITE + "'" : "None!"), ChatColor.WHITE + (suffix != null ? "'" + ChatColor.translateAlternateColorCodes('&', suffix) + ChatColor.WHITE + "'" : "")));
        inv.setItem(15, rename(Material.DIAMOND_BLOCK, ChatColor.AQUA + "Primary Group", ChatColor.GREEN + selected.getPrimaryGroup()));
        List<String> meta = new ArrayList<>();
        for(Map.Entry<String, List<String>> metaValue : selected.getCachedData().getMetaData(selected.getQueryOptions()).getMeta().entrySet()) {
            StringBuilder val = new StringBuilder();
            for(int i = 0; i < metaValue.getValue().size(); i++) {
                val.append(metaValue.getValue().get(i));
                if(i != metaValue.getValue().size() - 1) val.append(",");
            }
            meta.add(ChatColor.translateAlternateColorCodes('&', String.format("&8[&7%s&f=&7%s&8]", metaValue.getKey(), val)));
        }
        inv.setItem(16, rename(Material.COMMAND, ChatColor.GOLD + "Meta", meta));
        playGUISound(p);
        p.closeInventory();
        p.openInventory(inv);
    }
    public void playGUISound(Player p) {
        p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1L, 0L);
    }
    @EventHandler
    public void grantPlayerInfo(InventoryClickEvent e) {
        if(e.getClickedInventory() instanceof PlayerInventory) return;
        if(!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        if(!e.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN + "User GUI [LuckPerms] [Info]")) return;
        e.setCancelled(true);
        if(e.getSlot() == 31) {
            // go back
            User user = new GetUser(UUID.fromString(e.getView().getTopInventory().getItem(32).getItemMeta().getDisplayName().replace("" + ChatColor.GRAY, "").replace(ChatColor.WHITE + "", "").replace("UUID: ", ""))).get();
            if(user == null) {
                p.sendMessage(ChatColor.RED + "User not found.");
                return;
            }
            grantMain(p, user);
        }
    }
    @EventHandler
    public void grantMain(InventoryClickEvent e) {
        // Main GUI of /grant
        if(e.getClickedInventory() instanceof PlayerInventory) return;
        if(!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        if(!e.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN + "User GUI [LuckPerms]")) return;
        e.setCancelled(true);
        User selected = new GetUser(UUID.fromString(e.getView().getTopInventory().getItem(32).getItemMeta().getDisplayName().replace("" + ChatColor.GRAY, "").replace(ChatColor.WHITE + "", "").replace("UUID: ", ""))).get();
        if(selected == null) {
            if(e.getSlot() >= 9 && e.getSlot() <= 16) {
                p.sendMessage(ChatColor.RED + "User not found.");
                return;
            }
        }
        if(e.getSlot() == 10) {
            // Player Info
            grantPlayerInfo(p, selected);
        }else if(e.getSlot() == 11) {
            // Permission
        }else if(e.getSlot() == 12) {
            // Parent
        }else if(e.getSlot() == 13) {
            // Meta
        }else if(e.getSlot() == 14) {
            // Web Editor
            p.closeInventory();
            p.performCommand("lp user " + selected.getUsername() + " editor");
            return;
        }else if(e.getSlot() == 15) {
            // Clone
            p.closeInventory();
            AnvilGUI gui = new AnvilGUI(p, event -> {
                if(event.getSlot() == AnvilSlot.OUTPUT) {
                    event.setWillClose(true);
                    event.setWillDestroy(true);
                    String playerName = event.getName();
                    try {
                        User unused = main.lp.getUserManager().getUser(playerName);
                        unused.data();
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.RED + "You didn't input a valid entity name!");
                        return;
                    }
                    p.performCommand("lp user " + playerName + " clone " + selected);
                }else {
                    event.setWillClose(false);
                    event.setWillDestroy(false);
                }
            });
            gui.setSlot(AnvilSlot.INPUT_LEFT, rename(Material.PAPER, "Enter cloned entity here"));
            try {
                gui.open();
            }catch(Exception e1) {
                p.sendMessage(ChatColor.RED + "ERROR: " + e1.getMessage());
            }
        }

    }

    public static UUID getUUID(Inventory topInventory) {
        return UUID.fromString(topInventory.getItem(32).getItemMeta().getDisplayName().replace("" + ChatColor.GRAY, "").replace(ChatColor.WHITE + "", "").replace("UUID: ", ""));
    }
    public static UUID getUUID(Inventory topInventory, int slot) {
        return UUID.fromString(topInventory.getItem(slot).getItemMeta().getDisplayName().replace("" + ChatColor.GRAY, "").replace(ChatColor.WHITE + "", "").replace("UUID: ", ""));
    }
    public static UUID getUUID(InventoryClickEvent e) {
        return UUID.fromString(e.getView().getTopInventory().getItem(32).getItemMeta().getDisplayName().replace("" + ChatColor.GRAY, "").replace(ChatColor.WHITE + "", "").replace("UUID: ", ""));
    }
    public static UUID getUUID(InventoryClickEvent e, int slot) {
        return UUID.fromString(e.getView().getTopInventory().getItem(slot).getItemMeta().getDisplayName().replace("" + ChatColor.GRAY, "").replace(ChatColor.WHITE + "", "").replace("UUID: ", ""));
    }

    public static ItemStack rename(Material mat, String displayName) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack rename(short damageValue, Material mat, String displayName) {
        ItemStack item = new ItemStack(mat, 1, damageValue);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack rename(Material mat, String displayName, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack rename(Material mat, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public static class GetUser {
        UUID uuid;
        boolean found = false;
        User userFound;
        public GetUser(UUID uuid) {
            this.uuid = uuid;
        }
        public User get() {
            CompletableFuture<User> future = Main.getInstance().lp.getUserManager().loadUser(uuid);
            future.thenAcceptAsync((user) -> {
                this.userFound = user;
                this.found = true;
            });
            if(found) return userFound;
            else return null;
        }
    }
}