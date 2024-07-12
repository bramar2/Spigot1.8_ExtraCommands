package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class Decapitation extends CustomEnchantment {

    public Decapitation() {
        super(3533, EnchantmentTarget.SWORDS_AND_AXES, 3, 1, "Decapitation", "&3Decapitation", "&3Chance to have opponents head drop on death");
    }

    @Override
    public int getMultiplier() {
        return 2;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return null;
    }

    @Override
    public void onEvent(EventStore e) {   }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        try {
            Player killed = (Player) e.getEntity();
            Player killer = (Player) e.getDamager();
            if(!(killed.getHealth() - e.getFinalDamage() < 0.0d && usingEnchantment(killer))) return;
            if(checkSuccess((double)getEnchantLevel(killer, true) * 3.0d)) {
                ItemStack skull = new ItemStack(Material.SKULL_ITEM);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setDisplayName(ChatColor.WHITE + killed.getName() + ChatColor.WHITE + "'s head");
                meta.setOwner(killed.getName());
                skull.setItemMeta(meta);
                killed.getWorld().dropItemNaturally(killed.getLocation().add(0d, 0.5d, 0d), skull);
            }
        }catch(Exception ignored) {}
    }
}
