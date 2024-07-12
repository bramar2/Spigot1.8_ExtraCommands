package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public class LightningCMD extends EnchantCommand {
    public LightningCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "LIGHTNING";
    }

    @Override
    public boolean hasArguments() {
        return false;
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }
    @Override
    public void run() {
        LivingEntity p = getInput(LivingEntity.class);
        p.getWorld().strikeLightning(p.getLocation());
        Block b = p.getLocation().getBlock();
        if(!b.getType().isOccluding()) b.setType(Material.FIRE);
    }
}
