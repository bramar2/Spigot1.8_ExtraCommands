package me.bramar.extracommands.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.List;
import java.util.function.Consumer;

/**
 * BlockDropItemEvent for 1.8
 * Used in CustomEnchantment
 */
public class BlockDropItemEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player p;
    private final Location loc;
    private final List<Item> list;
    private final Material oldType;
    private final MaterialData oldData;
    private boolean isCancelled = false;
    public BlockDropItemEvent(Player p, Location loc, List<Item> list, Material oldType, MaterialData oldData) {
        super(loc.getBlock());
        this.list = list;
        this.loc = loc;
        this.p = p;
        this.oldType = oldType;
        this.oldData = oldData;
    }
    // Required since the event runs 2 ticks after the block is broken
    public Material getBlockType() {
        return oldType;
    }
    public Player getPlayer() {
        return p;
    }
    public Location getLocation() {
        return loc.clone();
    }

    public MaterialData getMaterialData() {
        return oldData;
    }

    // remove() to remove, add() to add
    // Mutable
    public List<Item> getItemList() {
        return list;
    }
    public void spawnItem(ItemStack item) {
        list.add(loc.getWorld().dropItemNaturally(loc, item));
    }
    public void runWhenFinished(Consumer<Item> consumer) {
        ((ItemList) list).doWhenDisposed(consumer);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }
}
