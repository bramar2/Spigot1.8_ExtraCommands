package me.bramar.extracommands.customenchants.enchants;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Drill extends CustomEnchantment {

    public Drill() {
        super(3537, EnchantmentTarget.PICKAXE, 3, 1, "Drill", "&cDrill", "&cDig multiple blocks at the same time");
    }
    class RemoveBF extends BukkitRunnable {
        Location loc;
        public RemoveBF(int ticks, Location loc) {
            this.loc = loc;
            runTaskLaterAsynchronously(Main.getInstance(), ticks);
        }

        @Override
        public void run() {
            blockFaces.remove(loc);
        }
    }


    // To avoid a LOOP
    private ArrayList<Location> returnIf = new ArrayList<>();
    // To get block faces
    private HashMap<Location, BlockFace> blockFaces = new HashMap<>();
    // BukkitRunnable's to remove blockFaces
    private ArrayList<RemoveBF> runnables = new ArrayList<>();

    @Override
    public int getMultiplier() {
        return 4;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return Arrays.asList(EventType.BLOCK_BREAK, EventType.INTERACT);
    }

    private boolean doesReturn(Location loc) {
        List<Location> list = returnIf.stream().filter((loc2) -> sameBlock(loc,loc2)).collect(Collectors.toList());
        if(!list.isEmpty()) returnIf.removeAll(list);
        return !list.isEmpty();
    }
    private boolean sameBlock(Location loc, Location loc2) {
        return loc.getBlockX() == loc2.getBlockX() &&
                loc.getBlockY() == loc2.getBlockY() &&
                loc.getBlockZ() == loc2.getBlockZ();
    }

    @Override
    public void onEvent(EventStore e) {
        try {
            if(e.getType() == EventType.INTERACT) {
                PlayerInteractEvent event = e.cast();
                // Remove already ran
                System.out.println("Event: " + event);
                new ArrayList<>(runnables).stream()
                        .filter((r) ->
                                sameBlock(event.getClickedBlock().getLocation(), r.loc))
                        .forEach((r) -> {
                            r.cancel();
                            runnables.remove(r);
                        });
                //
                runnables.add(new RemoveBF(15000, event.getClickedBlock().getLocation()));
            }else {
                BlockBreakEvent event = e.cast();
                if(doesReturn(event.getBlock().getLocation())) return;
                BlockFace face = null;
                for(Location key : blockFaces.keySet()) {
                    if(sameBlock(key, event.getBlock().getLocation())) {
                        face = blockFaces.get(key);
                        break;
                    }
                }
                assert face != null;
                int radius = 3 + (getEnchantLevel(e.getPlayer(),false)-1);
                int x, y, z;
                x = y = z = radius;
                if(face == BlockFace.DOWN || face == BlockFace.UP) y = 0;
                else if(face == BlockFace.EAST || face == BlockFace.WEST) x = 0;
                else if(face == BlockFace.NORTH || face == BlockFace.SOUTH) z = 0;
                else return;
                exemptPlayerBreak(e.getPlayer());
                for(int bX = -x; bX <= x; bX++) {
                    for(int bY = -y; bY <= y; bY++) {
                        for(int bZ = -z; bZ <= z; bZ++) {
                            breakAsPlayer(e.getPlayer(), event.getBlock().getRelative(bX, bY, bZ));
                        }
                    }
                }
                unexemptPlayerBreak(e.getPlayer());
            }
        }catch(Exception e1) {
            e1.printStackTrace();
        }
    }
    public void untilZero(int radius, Consumer<Vector> consumer, List<BlockFace> faces) {
        if(radius == 0) return;
        for(BlockFace face : faces) {
            consumer.accept(toVector(face).multiply(radius));
        }
        untilZero(radius-1, consumer, faces);
    }
    public Vector toVector(BlockFace face) {
        return new Vector(face.getModX(), face.getModY(), face.getModZ());
    }
    private Random r = new Random();
}
