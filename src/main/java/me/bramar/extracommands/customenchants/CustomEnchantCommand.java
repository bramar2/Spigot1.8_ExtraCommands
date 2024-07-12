package me.bramar.extracommands.customenchants;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomEnchantCommand implements CommandExecutor {
    private final EnchantLoader loader;
    public CustomEnchantCommand(EnchantLoader loader) {
        this.loader = loader;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            System.out.println("This command can only be used by a entity!");
            return true;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("e.customenchantment")) {
            p.sendMessage(ChatColor.RED + "No permision! (e.customenchantment)");
            return true;
        }
        if(args.length == 0) {
            p.sendMessage(ChatColor.BLUE + "----- CustomEnchantment commands -----\n"
                    + "This is a part feature of plugin 'ExtraCommands'\n\n"
                    + "- /ce get <enchantment_name> <level>\n"
                    + "- /ce get <enchantment_id> <level>\n"
                    + "- /ce give <player_name> <enchantment_name> <level>\n"
                    + "- /ce give <player_name> <enchantment_id> <level>\n"
                    + "- /ce list [enchantment_id/name]\n"
                    + "- /ce enchant <enchant_name/id> <level>: Enchants current item holded with that CustomEnchantment\n"
                    + "Enchantment ID/Name is optional (Get info of that Enchantment ID/Name)\n\n"
                    + "Replace space with '_' for Enchantment names\n"
                    + "Argument 'get' and 'give' gives enchantment books\n\n"
                    + "Custom Enchantments (built-in) start with ID 3500 and so on. Custom Enchantments from 'enchants' folder start with ID 35300 and so on, unless the ID is changed in the config file.\n\n"
                    + "There is " + ChatColor.DARK_AQUA + loader.getEnchants().size() + ChatColor.BLUE + " custom enchantments.");
        }else if(args[0].equalsIgnoreCase("get")) {
            try {
                String nameOrId = args[1].replace("_", " ");
                int level = Short.parseShort(args[2]);
                CustomEnchantment ce = loader.getByString(nameOrId);
                Validate.notNull(ce, "Enchantment doesn't exist");
                if(p.getInventory().firstEmpty() == -1) {
                    p.sendMessage(ChatColor.RED + "Your inventory is full!");
                    return true;
                }
                p.getInventory().addItem(ce.getBook(level));
                p.sendMessage(ChatColor.GREEN + "Successfully given the " + ChatColor.GRAY + ce.getDisplayName() + ChatColor.BLUE + " [" + ce.getId() + "] " + ChatColor.GREEN + "custom enchantment book.");
            }catch(ArrayIndexOutOfBoundsException e) {
                p.sendMessage(ChatColor.RED + "Too less argument!");
            }catch(NullPointerException e1) {
                p.sendMessage(ChatColor.RED + "A NullPointerException error occured. Perhaps you put a wrong id or name?");
                p.sendMessage(ChatColor.RED + "ERROR: " + e1.getMessage());
            }catch(Exception e1) {
                p.sendMessage(ChatColor.RED + "ERROR: " + e1.getMessage());
            }
        }else if(args[0].equalsIgnoreCase("give")) {
            try {
                Player player = Bukkit.getPlayer(args[1]);
                if(player == null) {
                    p.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                String nameOrId = args[2].replace("_", " ");
                int level = Short.parseShort(args[3]);
                CustomEnchantment ce = loader.getByString(nameOrId);
                Validate.notNull(ce, "Enchantment doesn't exist");
                if(player.getInventory().firstEmpty() == -1) {
                    p.sendMessage(ChatColor.RED + "Their inventory is full! Dropping it on the floor...");
                    player.getWorld().dropItem(player.getLocation(), ce.getBook(level));
                    return true;
                }
                player.getInventory().addItem(ce.getBook(level));
                p.sendMessage(ChatColor.GREEN + "Successfully given " + player.getName() + " the " + ChatColor.GRAY + ce.getDisplayName() + ChatColor.BLUE + " [" + ce.getId() + "] " + ChatColor.GREEN + "custom enchantment book.");
            }catch(ArrayIndexOutOfBoundsException e) {
                p.sendMessage(ChatColor.RED + "Too less argument!");
            }catch(NullPointerException e1) {
                p.sendMessage(ChatColor.RED + "A NullPointerException error occured. Perhaps you put a wrong id or name?");
                p.sendMessage(ChatColor.RED + "ERROR: " + e1.getMessage());
            }catch(Exception e1) {
                p.sendMessage(ChatColor.RED + "ERROR: " + e1.getMessage());
            }
        }else if(args[0].equalsIgnoreCase("list")) {
            try {
                if(args.length == 1) {
                    p.sendMessage(ChatColor.BLUE + "List of Custom Enchantments:");
                    IChatBaseComponent msg = IChatBaseComponent.ChatSerializer.a("\"\"");
                    for(int i = 0; i < loader.getEnchants().size(); i++) {
                        CustomEnchantment enchant = loader.getEnchants().get(i);
                        IChatBaseComponent text = IChatBaseComponent.ChatSerializer.a(
                                String.format("{\"text\":\"%s\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/ce list %s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[\"%s\\n%s\"]}}",
                                        enchant.getDisplayName(), enchant.getName().replace(" ", "_"), enchant.getDisplayName(), ChatColor.translateAlternateColorCodes('&', enchant.getDescription())));
                        msg.addSibling(text);
                        if(i != loader.getEnchants().size() - 1)
                            msg.addSibling(IChatBaseComponent.ChatSerializer.a("{\"text\":\", \",\"color\":\"aqua\"}"));
                    }
                    ((CraftPlayer) p).getHandle().sendMessage(msg);
                }else {
                    CustomEnchantment ench = loader.getByString(args[1]);
                    if(ench == null) ench = loader.getByString(args[1].replace("_", " "));
                    Validate.notNull(ench, "Enchantment doesn't exist");
                    String conflictStr = "";
                    boolean conflictIsFilled = false;
                    for(int i = 0; i < ench.getConflictedEnchantment().size(); i++) {
                        Enchantment conflicted = ench.getConflictedEnchantment().get(i);
                        if(!conflictIsFilled) {
                            conflictStr = "\n&2Conflicts with: &b";
                            conflictIsFilled = true;
                        }
                        conflictStr += conflicted.getName();
                        if(i != ench.getConflictedEnchantment().size() - 1)
                            conflictStr += ", ";
                    }
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            String.format(
                                    "\n&bEnchantment Info [ID %s]:\n" +
                                            "%s\n" +
                                            "%s\n" +
                                            "&2Level: &b%s - %s\n" +
                                            "&2Items: &b%s%s\n",
                                    ench.getId(), ench.getDisplayName(), ench.getDescription(), ench.getStartLevel(), ench.getMaxLevel(), ench.getTargets(),
                                    conflictStr
                            )
                    ));
                }
            }catch(Exception e1) {
                p.sendMessage(ChatColor.RED + "ERROR: " + e1.getMessage());
            }
        }else if(args[0].equalsIgnoreCase("enchant")) {
            try {
                if(args.length < 3) {
                    p.sendMessage(ChatColor.RED + "Too less arguments!");
                    return true;
                }
                CustomEnchantment ench = loader.getByString(args[1].replace("_", " "));
                Validate.notNull(ench, "Enchantment doesn't exist");
                p.getInventory().setItemInHand(loader.enchant(p.getInventory().getItemInHand().clone(), ench, Integer.parseInt(args[2])));
                if(!ench.hasEnchant(p.getInventory().getItemInHand())) p.sendMessage(ChatColor.RED + "Failed to enchant item! Use /ce list <name or id> to get the info. Things that might've happened: Item is not compatible with the enchantment (sword book with a shovel), there is a conflicting enchantment");
                else p.sendMessage(ChatColor.GREEN + "Successfully enchanted item!");
            }catch(NumberFormatException e1) {
                p.sendMessage(ChatColor.RED + "You didn't put a valid integer value!");
            }catch(NullPointerException e1) {
                p.sendMessage(ChatColor.RED + "An error occured [NullPointerException] Are you holding an item?");
            }catch(Exception e1) {
                p.sendMessage(ChatColor.RED + "ERROR: " + e1.getMessage());
            }
        }
        else p.sendMessage(ChatColor.RED + "Invalid argument! Use /ce to get all the commands.");
        if(args.length >= 1) if(args[0].equalsIgnoreCase("test")) {
			/*
			 /ce test
			*/
            p.resetMaxHealth();
            p.setMaxHealth(20);
            try {
                // Stuff
                p.setFoodLevel(Integer.parseInt(args[1]));
                p.sendMessage("success set food lvl");
            }catch(Exception e1) {
                p.sendMessage("error: " + e1);
                e1.printStackTrace();
            }
        }
        return true;
    }
}
