package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Explode extends CustomEnchantment {
    public Explode() {
        super(3538, EnchantmentTarget.TOOLS, 3, 1, "Explode", "&3Explode", "&3A chance to create an explosion upon breaking a block");
    }

    @Override
    public int getMultiplier() {
        return 5;
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
            if(!returnIf(event.getBlock().getLocation()) && e.getType() == EventType.BLOCK_BREAK && checkSuccess(getEnchantLevel(e.getPlayer(), false) * 3)) {
                float power = (float) getEnchantLevel(e.getPlayer(), false) * 2.25f;
                event.getBlock().getWorld().createExplosion(event.getBlock().getLocation(), power, false);
            }
        }catch(Exception ignored) {}
    }
}
