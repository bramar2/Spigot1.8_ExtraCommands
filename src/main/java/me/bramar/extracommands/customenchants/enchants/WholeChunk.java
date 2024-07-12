package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WholeChunk extends CustomEnchantment {

    public WholeChunk() {
        super(3543, EnchantmentTarget.TOOLS, 1, 1, "Whole Chunk", "&6Whole Chunk", "&6Mines the entire chunk the block is on");
    }

    @Override
    public int getMultiplier() {
        return 0x7fffff;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return Collections.singletonList(EventType.BLOCK_BREAK);
    }

    private List<Location> list = new ArrayList<>();

    private boolean returnIf(Location loc) {
        return list.removeIf((l) -> sameBlock(l, loc));
    }
    private boolean sameBlock(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
    }



    @Override
    public void onEvent(EventStore e) {
        try {
            BlockBreakEvent event = e.cast();
            if(returnIf(event.getBlock().getLocation())) return;
            Chunk c = event.getBlock().getChunk();
            if(!c.isLoaded()) return;
            long nano = System.nanoTime();
            int minX = c.getX() << 4;
            int minZ = c.getZ() << 4;
            int maxX = minX | 15;
            int maxY = c.getWorld().getMaxHeight();
            int maxZ = minZ | 15;
            ItemStack inHand = e.getPlayer().getItemInHand().clone();
            int slot = e.getPlayer().getInventory().getHeldItemSlot();
            exemptPlayerBreak(e.getPlayer());
            for(int x = minX; x <= maxX; x++) {
                for(int y = 0; y <= maxY; y++) {
                    for(int z = minZ; z <= maxZ; z++) {
                        Block b = c.getBlock(x, y, z);
                        if(b.getType() == Material.AIR) continue;
                        list.add(b.getLocation());
                        breakAsPlayer(e.getPlayer(), b);
                    }
                }
            }
            unexemptPlayerBreak(e.getPlayer());
            e.getPlayer().getInventory().setItem(slot, inHand);
            double inb = System.nanoTime() - nano;
            String time = (inb / 1000000.0d)+"";
            if(inb >= 10000.0) time = ChatColor.RED + time;
            else if(inb >= 1000.0) time = ChatColor.YELLOW + time;
            else time = ChatColor.GREEN + time;
            e.getPlayer().sendMessage("Chunk successfully mined! Took " + time + "ms");
        }catch(Exception ignored) {}
    }
}
