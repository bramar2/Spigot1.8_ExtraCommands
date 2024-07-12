package me.bramar.extracommands.commands;

import me.bramar.extracommands.Main;
import net.minecraft.server.v1_8_R3.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DoubleJumpCommand extends BukkitRunnable implements CommandExecutor {

    private final List<UUID> doubleJump = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            boolean state = doubleJump.contains(p.getUniqueId());
            if(!state) doubleJump.add(p.getUniqueId());
            else doubleJump.remove(p.getUniqueId());
            state = !state;
            if(state) p.sendMessage(Main.PREFIX + "Successfully enabled double jump");
            else {
                if(p.getGameMode() == GameMode.ADVENTURE || p.getGameMode() == GameMode.SURVIVAL) {
                    // Set allow flight to false
                    if(p.isFlying()) p.setFlying(false);
                    p.setAllowFlight(false);
                }
                p.sendMessage(Main.PREFIX + "Successfully disabled double jump");
            }
        }else sender.sendMessage("This command only works for players!");
        return true;
    }

    // 5L
    @Override
    public void run() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(!doubleJump.contains(p.getUniqueId())) continue;
            if(p.isOnGround() && !p.getAllowFlight()) p.setAllowFlight(true);
            if(p.getAllowFlight() && p.isFlying()) {
                p.setAllowFlight(false);
                p.setFlying(false); // Removes flight (Flight is only to check if entity pressed space
                Vector velocity = p.getVelocity().clone();
                double y = 0.42;
                y += 5f * 0.1F;
                velocity.setY(y);
                if(p.isSprinting() || velocity.getX() > 0.3 || velocity.getZ() > 0.3) {
                    float f = p.getLocation().getYaw() * 0.017453292F;
                    velocity.setX(velocity.getX() - ((double)(MathHelper.sin(f) * 0.2F)));
                    velocity.setZ(velocity.getZ() - ((double)(MathHelper.cos(f) * 0.2F)));
                }
                p.setVelocity(velocity);
            }
        }
    }
}
