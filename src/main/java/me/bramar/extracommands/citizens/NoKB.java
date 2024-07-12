package me.bramar.extracommands.citizens;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import me.bramar.extracommands.Main;
import net.citizensnpcs.api.event.NPCDamageByBlockEvent;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.trait.Trait;

public class NoKB extends Trait {
    public NoKB() {
        super("NoKB");
    }
    @EventHandler
    public void npcDamaged(NPCDamageByEntityEvent e) {
        if(e.getNPC() != this.npc) return;
        e.getNPC().getEntity().setVelocity(new Vector(0, 0, 0));
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> e.getNPC().getEntity().setVelocity(new Vector(0, 0, 0)), 2L);
    }
    @EventHandler
    public void npcDamaged(NPCDamageByBlockEvent e) {
        if(e.getNPC() != this.npc) return;
        e.getNPC().getEntity().setVelocity(new Vector(0, 0, 0));
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> e.getNPC().getEntity().setVelocity(new Vector(0, 0, 0)), 2L);
    }
    @EventHandler
    public void npcDamaged(NPCDamageEvent e) {
        if(e.getNPC() != this.npc) return;
        e.getNPC().getEntity().setVelocity(new Vector(0, 0, 0));
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> e.getNPC().getEntity().setVelocity(new Vector(0, 0, 0)), 2L);
    }
}
