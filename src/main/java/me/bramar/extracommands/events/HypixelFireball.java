package me.bramar.extracommands.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.bramar.extracommands.Main;

public class HypixelFireball implements Listener, CommandExecutor {

    public HypixelFireball() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        Main.getInstance().getCommand("hypixelfireball").setExecutor(this);
    }

    @EventHandler
    public void onItemUse(final PlayerInteractEvent e) {
        if(!Main.getInstance().getNormalConfig().getBoolean("hypixel-fireball")) return;
        if(!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if(e.getPlayer().getItemInHand().getType() != Material.FIREBALL) return;
        Fireball f = (Fireball) e.getPlayer().launchProjectile(Fireball.class);
        f.setVelocity(f.getVelocity().multiply(5));
        f.setYield(4.0F);
        try {
            if(e.getPlayer().getInventory().getItemInHand().getAmount() > 1) {
                e.getPlayer().getInventory().getItemInHand().setAmount(e.getPlayer().getInventory().getItemInHand().getAmount() - 1);
            }else e.getPlayer().getInventory().setItemInHand(null);
        }catch(Exception ignored) {}
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            // Player
            Player p = (Player) sender;
            if(!p.hasPermission("e.hypixelfireball")) {
                p.sendMessage(ChatColor.RED + "No permission! (e.hypixelfireball)");
                return true;
            }
            try {
                boolean chf = new Boolean(Main.getInstance().getNormalConfig().getBoolean("hypixel-fireball"));
                if(!chf) Main.getInstance().getNormalConfig().set("hypixel-fireball", true);
                else Main.getInstance().getNormalConfig().set("hypixel-fireball", false);
                Main.getInstance().getNormalConfig().save(Main.getInstance().getNormalConfigFile());
                if(!chf) p.sendMessage(Main.PREFIX + "The fireballs now work and function as a hypixel fireball.");
                else p.sendMessage(Main.PREFIX + "The fireballs now dont work and dont spawn.");
            }catch(Exception e1) {
                p.sendMessage(ChatColor.DARK_RED + "ERROR: Contact an administrator! Message: " + e1.getMessage());
            }
        }else if(sender instanceof ConsoleCommandSender) {
            // Console

            // 'p' variable name so i can copy paste
            ConsoleCommandSender p = (ConsoleCommandSender) sender;
            try {
                boolean chf = new Boolean(Main.getInstance().getNormalConfig().getBoolean("hypixel-fireball"));
                if(!chf) Main.getInstance().getNormalConfig().set("hypixel-fireball", true);
                else Main.getInstance().getNormalConfig().set("hypixel-fireball", false);
                Main.getInstance().getNormalConfig().save(Main.getInstance().getNormalConfigFile());
                if(!chf) p.sendMessage(Main.PREFIX + "The fireballs now work and function as a hypixel fireball.");
                else p.sendMessage(Main.PREFIX + "The fireballs now dont work and dont spawn.");
            }catch(Exception e1) {
                p.sendMessage(ChatColor.DARK_RED + "ERROR: Contact an administrator! Message: " + e1.getMessage());
            }
        }else {
            // Other (CustomEnchantCommand blocks or smth, No message)
            try {
                boolean chf = new Boolean(Main.getInstance().getNormalConfig().getBoolean("hypixel-fireball"));
                if(!chf) Main.getInstance().getNormalConfig().set("hypixel-fireball", true);
                else Main.getInstance().getNormalConfig().set("hypixel-fireball", false);
                Main.getInstance().getNormalConfig().save(Main.getInstance().getNormalConfigFile());
            }catch(Exception e1) {}
        }
        return true;
    }
}
