package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Critical extends CustomEnchantment {

    public Critical() {
        super(3529, EnchantmentTarget.SWORDS, 3, 1, "Critical", "&6Critical", "&6Increases damage done by critical hit");
    }

    @Override
    public int getMultiplier() {
        return 4;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return Collections.singletonList(EventType.PLAYER_DAMAGE_ENTITY);
    }

    @Override
    public void onEvent(EventStore e) {
        try {
            EntityDamageByEntityEvent event = e.cast();
            // Critical Requirements
            Player p = e.getPlayer();
            Material mat = p.getLocation().getBlock().getType() == null ? Material.AIR : p.getLocation().getBlock().getType();
            if(p.getFallDistance() > 0.0f // The entity must be falling
                    && !p.isOnGround() // The entity must not be on ground
                    && Main.getInstance().getActivePotionEffect(p, PotionEffectType.BLINDNESS) == null
                    // The entity must not be affected by blindness ^^^
                    && !p.isSprinting() // The entity must not be sprinting
                    && p.getVehicle() == null // The entity must not be riding an entity (Minecart,boat,pig,etc)

                    // The entity must not be on the following: Climb blocks (Ladders and Vines), and liquid.
                    && !(mat == Material.LADDER || mat == Material.VINE || mat == Material.WATER || mat == Material.LAVA || mat == Material.STATIONARY_WATER || mat == Material.STATIONARY_LAVA)
            ) {
                // Critical hit!
                double multiplier = 1.0 + (getEnchantLevel(p, false) * 0.1);
                // 10% extra damage each level, making it a max of 30% extra damage
                double dmg = event.getDamage() * multiplier;
                event.setDamage(dmg);
            }
        }catch(Exception ignored) {}
    }
}
