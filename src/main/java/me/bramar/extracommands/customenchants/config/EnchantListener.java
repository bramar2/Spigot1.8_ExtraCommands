package me.bramar.extracommands.customenchants.config;

import me.bramar.extracommands.customenchants.EnchantLoader;
import me.bramar.extracommands.customenchants.commands.RemoveKnockbackCMD;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnchantListener implements Listener {

    // UUID, Entry 1st param Tick it was launched 2nd param Amount of ticks until kb turns on
    public HashMap<UUID, Map.Entry<Integer, Integer>> noKnockback = new HashMap<>();
    private RemoveKnockbackCMD removeKB = new RemoveKnockbackCMD("REMOVE_KNOCKBACK:100");
    @EventHandler
    public void disableKB(EntityDamageByEntityEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        Map.Entry<Integer, Integer> disableKB = noKnockback.get(uuid);
        if(disableKB != null) {
            if(disableKB.getKey() + disableKB.getValue() < EnchantLoader.getInstance().getCurrentTick()) {
                noKnockback.remove(uuid);
                return;
            }
            removeKB.reset();
            removeKB.input(e.getEntity());
            removeKB.run();
        }
    }
    public HashMap<UUID, Map.Entry<Integer, Integer>> invincible = new HashMap<>();
    @EventHandler
    public void invincible(EntityDamageByEntityEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        Map.Entry<Integer, Integer> info = invincible.get(uuid);
        if(info != null) {
            if(info.getKey() + info.getValue() < EnchantLoader.getInstance().getCurrentTick()) {
                invincible.remove(uuid);
                return;
            }
            e.setCancelled(true);
        }
    }
}
