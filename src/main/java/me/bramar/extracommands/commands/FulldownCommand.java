package me.bramar.extracommands.commands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.bramar.extracommands.Main;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.ChatModifier;
import net.minecraft.server.v1_8_R3.EnumChatFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FulldownCommand implements CommandExecutor, Listener {
    private boolean fullDown = false;
    private String fulldownPass;
    private PacketAdapter fulldownPacket;

    public FulldownCommand(Main plugin) {
        fulldownPacket = new PacketAdapter(plugin, PacketType.values()) {
            @Override
            public void onPacketReceiving(PacketEvent e) {
                if(!fullDown) return;
                e.setCancelled(true);
            }
            @Override
            public void onPacketSending(PacketEvent e) {
                if(!fullDown) return;
                if(e.getPacketType() == PacketType.Status.Server.SERVER_INFO) {
                    e.getPacket().getServerPings().read(0).setMotD(WrappedChatComponent.fromHandle(new ChatComponentText("This server is currently in full-down mode!\nCheck again later").setChatModifier(new ChatModifier().setColor(EnumChatFormat.RED))));
                    return;
                }
                e.setCancelled(true);
            }
        };
        new BukkitRunnable() {
            public void run() {
//                plugin.getProtocolManager().addPacketListener(fulldownPacket);
                plugin.getLogger().info("If a MASS-SPAM text was shown in the console, that was because the registration of fulldown packet listener for /fulldown");
            }
        }.runTaskLater(plugin, 1000L);
        fulldownPass = plugin.getNormalConfig().getString("fulldown-pass");
    }
    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        if(this.fullDown) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&', "&4Server is currently on full-down mode."));
        }
    }
    @EventHandler
    public void onServerCommand(ServerCommandEvent e) {
        if(this.fullDown && !this.fulldownPass.equalsIgnoreCase("")) {
            if(!e.getCommand().contains("#" + this.fulldownPass)) {
                e.setCancelled(true);
                System.out.println("Server is on full-down mode. Put the correct password in any arguments to run the command.");
            }else {
                e.setCommand(e.getCommand().replace(" #" + this.fulldownPass, ""));
                e.setCommand(e.getCommand().replace("#" + this.fulldownPass, ""));
                System.out.println("Bypassed with password!");
            }
        }
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            commandSender.sendMessage(ChatColor.RED + "No permission!");
        }else if(commandSender instanceof ConsoleCommandSender) {// no command blocks, only specifically console
            if(!fullDown) {
                fullDown = true;
                for(Player p : Bukkit.getOnlinePlayers()) {
                    try {
                        p.kickPlayer(ChatColor.translateAlternateColorCodes('&', "You have been kicked because: Server has been made fully down!"));
                    }catch(Exception ignored) {}
                }
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4Server is fully down!"));
            }else {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&aServer is now up and running!"));
            }

        }
        return true;
    }
}
