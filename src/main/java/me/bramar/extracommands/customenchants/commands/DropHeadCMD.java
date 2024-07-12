package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class DropHeadCMD extends EnchantCommand {
    public DropHeadCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "DROP_HEAD";
    }

    @Override
    public boolean hasArguments() {
        return false;
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Player.class};
    }
    @Override
    public void run() {
        Player p = getInput(Player.class);
        ItemStack i = new ItemStack(Material.SKULL_ITEM);
        SkullMeta meta = (SkullMeta) i.getItemMeta();
        meta.setOwner(p.getName());
        meta.setDisplayName(p.getName() + "'s owner");
        i.setItemMeta(meta);
        p.getWorld().dropItemNaturally(p.getLocation(), i);
    }
}
