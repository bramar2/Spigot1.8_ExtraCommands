package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import me.bramar.extracommands.events.BlockDropItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

import java.util.*;
import java.util.function.Predicate;

public class Veinminer extends CustomEnchantment {

    public Veinminer() {
        super(3542, EnchantmentTarget.TOOLS_NO_HOE, 3, 1, "Veinminer", "&6Veinminer", "&6Vein mines items depending on the tools, and increases limit by level");
    }

    @Override
    public int getMultiplier() {
        return 4;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return Collections.singletonList(EventType.BLOCK_DROP_ITEM);
    }

    private final HashMap<Location, Location> tp = new HashMap<>();
    public <K, V> V getIf(Map<K, V> map, Predicate<K> predicate) {
        for(Map.Entry<K, V> entry : map.entrySet()) {
            if(predicate.test(entry.getKey())) return entry.getValue();
        }
        return null;
    }
    public <K, V> void removeIf(Map<K, V> map, Predicate<K> predicate) {
        new HashMap<>(map).forEach((key, val) -> {
            if(predicate.test(key)) map.remove(key);
        });
    }
    private List<Location> sphere(Location loc) {
        List<Location> list = new ArrayList<>();
        // is not actually a sphere, but a 3x3 square around the block
        for(int xOffset = -1; xOffset <= 1; xOffset++) {
            for(int yOffset = -1; yOffset <= -1; yOffset++) {
                for(int zOffset = -1; zOffset <= -1; zOffset++) {
                    list.add(loc.add(xOffset, yOffset, zOffset));
                }
            }
        }

        Random rnd = new Random();
        Collections.shuffle(list, rnd);
        return list;
    }

    public boolean sameBlock(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ();
    }

    public void getVein(List<Block> toAdded, MaterialData data, int limit, Location current) {
        if(limit <= 0 || limit >= 1e5) throw new IllegalArgumentException("limit must be between 1 - 1e5 (100k)");
        List<Block> blockNearby = new ArrayList<>();
        for(Location l : sphere(current)) {
            Block block = l.getBlock();
            System.out.println(data + " vs " + block.getState().getData());
            if(data.equals(block.getState().getData())) blockNearby.add(block);
        }
        if(blockNearby.size() == 0)
            return;
        // Remove duplicates
        for(Block b1 : toAdded) blockNearby.removeIf(b2 -> sameBlock(b1.getLocation(), b2.getLocation()));
        if(toAdded.size() > limit) while (toAdded.size() > limit) toAdded.remove(limit + 1);
        else if(toAdded.size() < limit) {
            if(toAdded.size() + blockNearby.size() > limit) {
                while(toAdded.size() + blockNearby.size() > limit && blockNearby.size() > 0) blockNearby.remove(blockNearby.size()-1);
                toAdded.addAll(blockNearby);
            }else {
                toAdded.addAll(blockNearby);
                for(Block b : blockNearby) getVein(toAdded, data, limit, b.getLocation());
            }
        }
        //
    }

    @Override
    public void onEvent(EventStore e) {
        try {
            if(e.getPlayer().getGameMode() == GameMode.CREATIVE || e.getPlayer().getGameMode() == GameMode.SPECTATOR)
                return;
            final BlockDropItemEvent event = e.cast();
            Location loc = getIf(tp, (t) -> sameBlock(t, event.getLocation()));
            if(loc != null) {
                event.runWhenFinished((i) -> i.teleport(loc));
                removeIf(tp, (t) -> sameBlock(t, event.getLocation()));
            }else {
                Material type = event.getBlockType();
                Set<Material> allowed = getAllowed(e.getPlayer().getItemInHand().getType());
                System.out.println("Material: " + type);
                if(!allowed.contains(type)) return;
                ArrayList<Block> vein = new ArrayList<>();
                int limit = 15 + ((getEnchantLevel(event.getPlayer().getItemInHand()) - 1) * 10);
                getVein(vein, event.getMaterialData(), limit, event.getLocation());
                vein.removeIf((b) -> sameBlock(b.getLocation(), event.getLocation())); // Same block
                exemptPlayerBreak(e.getPlayer());
                for(Block b : vein) {
                    tp.put(b.getLocation(), event.getLocation());
                    breakAsPlayer(e.getPlayer(), b);
                }
                unexemptPlayerBreak(e.getPlayer());
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                    // Makes sure everything IS indeed cleared
                    vein.forEach((b) -> tp.remove(b.getLocation()));
                }, 20L);
            }
        }catch(Exception e1) {
            e1.printStackTrace();
        }
    }
}
