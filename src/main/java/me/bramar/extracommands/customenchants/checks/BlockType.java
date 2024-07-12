package me.bramar.extracommands.customenchants.checks;

import me.bramar.extracommands.customenchants.config.Check;
import org.bukkit.Material;
import org.bukkit.event.block.BlockEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// BLOCK_TYPE:DIRT
// on BlockBreakEvent or BlockPlaceEvent
public class BlockType extends Check {
    List<Material> materials = new ArrayList<>();
    boolean reverse = false;
    boolean all = false;

    public BlockType(String str) {
        super(str);

        for(int i = 1; i < eachLine.length; i++) {
            String s = eachLine[i];
            if(s.equals("*") || s.equalsIgnoreCase("all")) {
                all = true;
                break;
            }
            if(i == 1 && s.equals("!="))
                reverse = true;
            else
                try {
                    materials.add(Material.valueOf(s.toUpperCase()));
                }catch(Exception ignored) {}
        }
    }

    @Override
    public String name() {
        return "BLOCK_TYPE";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[]{BlockEvent.class};
    }

    @Override
    public boolean allowDamaged() {
        return false;
    }

    @Override
    public boolean check() {
        if(all)
            return !reverse; // True if all and not reverse
        Material mat = getInput(BlockEvent.class).getBlock().getType();
        boolean b = materials.contains(mat);
        return reverse != b; // IDEA's version of (reverse ? !b : b)
    }
}
