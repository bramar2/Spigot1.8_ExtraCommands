package me.bramar.extracommands.events;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import me.bramar.extracommands.Main;

public class HypixelTNT implements Listener, CommandExecutor {

    public HypixelTNT() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        Main.getInstance().getCommand("hypixeltnt").setExecutor(this);
    }

    // Spawns tnt
    @EventHandler
    public void onPlaceTNT(BlockPlaceEvent e) {
        if(!Main.getInstance().getNormalConfig().getBoolean("hypixel-tnt")) return;
        if(!(e.getBlock().getType() == Material.TNT)) return;
        e.getBlock().setType(Material.AIR);
        TNTPrimed tnt = (TNTPrimed) e.getPlayer().getWorld().spawn(e.getBlock().getLocation().add(0.5D, 0.5D, 0.5D), TNTPrimed.class);
        tnt.setFuseTicks(40);
        tnt.setVelocity(new Vector(0, 0, 0));
    }

    // Reduce damage
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if(e.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)
            return;
        if(!Main.getInstance().getNormalConfig().getBoolean("hypixel-tnt")) return;
        e.setDamage(0D);
        return;
        // Managed by EntityExplodeEvent
    }

    // TNT velocity explosion
    public void onExplode(EntityExplodeEvent e) {
        if(!Main.getInstance().getNormalConfig().getBoolean("hypixel-tnt")) return;
        for(Entity entity : e.getEntity().getLocation().getWorld().getNearbyEntities(e.getEntity().getLocation(), 8, 8, 8)) {
            if(entity instanceof Damageable) {
                try {
                    ((Damageable)entity).damage(Main.getInstance().getNormalConfig().getDouble("hypixel-tnt-damage"));
                }catch(Exception e1) {
                    ((Damageable)entity).damage(2D);
                }
            }
        }
        if(!(e.getEntity() instanceof TNTPrimed)) return;
        for(Entity entity : e.getEntity().getWorld().getEntities()) {
            if(entity.getLocation().distance(e.getEntity().getLocation()) < 10.0D) {
                Vector v = entity.getLocation().add(0.0D, 1.0D, 0.0D).toVector();
                if(!(entity instanceof Player)) return;
                v.length();
                v.normalize();
                v.multiply(4.0D / 1);
                Player p = (Player) entity;
                if(p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                    System.out.println("GameMode survival/adventure");
                    v.divide(new Vector(1, 7, 1));
                }else {
                    v.divide(new Vector(3, 10, 3));
                }
                v.multiply(new Vector(-1, 1, -1));
                entity.setVelocity(entity.getVelocity().add(v));
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            // Player
            Player p = (Player) sender;
            if(!p.hasPermission("e.hypixeltnt")) {
                p.sendMessage(ChatColor.RED + "No permission! (e.hypixeltnt)");
                return true;
            }
            try {
                boolean chf = new Boolean(Main.getInstance().getNormalConfig().getBoolean("hypixel-tnt"));
                if(!chf) Main.getInstance().getNormalConfig().set("hypixel-tnt", true);
                else Main.getInstance().getNormalConfig().set("hypixel-tnt", false);
                Main.getInstance().getNormalConfig().save(Main.getInstance().getNormalConfigFile());
                if(!chf) p.sendMessage(Main.PREFIX + "The tnt now spawns and work as a hypixel tnt.");
                else p.sendMessage(Main.PREFIX + "The tnt now dont spawn.");
            }catch(Exception e1) {
                p.sendMessage(ChatColor.DARK_RED + "ERROR: Contact an administrator! Message: " + e1.getMessage());
            }
        }else if(sender instanceof ConsoleCommandSender) {
            // Console

            // 'p' variable name so i can copy paste
            ConsoleCommandSender p = (ConsoleCommandSender) sender;
            try {
                boolean chf = new Boolean(Main.getInstance().getNormalConfig().getBoolean("hypixel-tnt"));
                if(!chf) Main.getInstance().getNormalConfig().set("hypixel-tnt", true);
                else Main.getInstance().getNormalConfig().set("hypixel-tnt", false);
                Main.getInstance().getNormalConfig().save(Main.getInstance().getNormalConfigFile());
                if(!chf) p.sendMessage(Main.PREFIX + "The tnt now spawns and work as a hypixel tnt.");
                else p.sendMessage(Main.PREFIX + "The tnt now dont spawn.");
            }catch(Exception e1) {
                p.sendMessage(ChatColor.DARK_RED + "ERROR: Contact an administrator! Message: " + e1.getMessage());
            }
        }else {
            // Other (CustomEnchantCommand blocks or smth, No message)
            try {
                boolean chf = new Boolean(Main.getInstance().getNormalConfig().getBoolean("hypixel-tnt"));
                if(!chf) Main.getInstance().getNormalConfig().set("hypixel-tnt", true);
                else Main.getInstance().getNormalConfig().set("hypixel-tnt", false);
                Main.getInstance().getNormalConfig().save(Main.getInstance().getNormalConfigFile());
            }catch(Exception e1) {}
        }
        return true;
    }
}
