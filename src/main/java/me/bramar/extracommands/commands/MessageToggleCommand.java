package me.bramar.extracommands.commands;

import me.bramar.extracommands.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import static me.bramar.extracommands.Main.PREFIX;

public class MessageToggleCommand implements CommandExecutor {
    private final Main main;

    public MessageToggleCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(main.getMessageToggle().contains(p.getUniqueId())) {
                main.getMessageToggle().remove(p.getUniqueId());
                p.sendMessage(PREFIX + "You have enabled receiving messages");
            }else {
                main.getMessageToggle().add(p.getUniqueId());
                p.sendMessage(PREFIX + "You have disabled receiving messages");
            }
            main.getDatabaseConfig().set("msgtoggle", main.getMessageToggle().stream().map(UUID::toString).collect(Collectors.toList()));
            try {
                main.getDatabaseConfig().save(main.getDatabaseFile());
            }catch(IOException e) {
                e.printStackTrace();
                System.out.println("Failed to save database (MessageToggle)");
            }
        }else sender.sendMessage("This command only works for players!");
        return true;
    }
}
