package me.bramar.extracommands;

import me.bramar.extracommands.AnvilGUI.AnvilSlot;
import net.dev.eazynick.api.NickManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static me.bramar.extracommands.Main.PREFIX;

public class NickInfo {
    String name = "";
    String prefix = "";
    String suffix = "";
    String skin = "";
    Player p;
    boolean hasSender;
    Player sender;
    String senderName = "";
    String senderChatPrefix = "";
    String senderChatSuffix = "";
    String senderTagPrefix = "";
    String senderTagSuffix = "";
    String senderTabPrefix = "";
    String senderTabSuffix = "";
    boolean isNicked = false;
    Main main;
    public NickInfo(Main main, Player p, Player sender, boolean hasSender) {
        this.sender = sender;
        if(hasSender) {
            NickManager api = new NickManager(sender);
            if(api.isNicked()) {
                isNicked = api.isNicked();
                senderName = api.getNickName();
                senderChatPrefix = api.getChatPrefix();
                senderChatSuffix = api.getChatSuffix();
                senderTagPrefix = api.getTagPrefix();
                senderTagSuffix = api.getTagSuffix();
                senderTabPrefix = api.getTabPrefix();
                senderTabSuffix = api.getTabSuffix();
                api.unnickPlayer();
            }
        }
        this.hasSender = hasSender;
        this.main = main;
        this.p = p;
    }
    public void nick() {
        NickManager api = new NickManager(p);
        api.unnickPlayer();
        api.updatePlayer();
        if(prefix.equalsIgnoreCase("NULL")) prefix = "";
        if(suffix.equalsIgnoreCase("NULL")) suffix = "";
        if(skin.equalsIgnoreCase("NULL")) skin = api.getRandomName();
        prefix = prefix.replace("%s%", " ");
        suffix = suffix.replace("%s%", " ");
        main.getServer().getScheduler().scheduleSyncDelayedTask(main, () -> {
            api.unnickPlayer();api.updatePlayer();
            api.nickPlayer(name, skin);
            api.updatePlayer();
            // TODO
            //api.updatePrefixSuffix("", "", prefix, suffix, prefix, suffix);
            api.updatePrefixSuffix(senderName, name, senderTagPrefix, senderTagSuffix, senderChatPrefix, senderChatSuffix, "", "", 0, "");
            api.updatePlayer();
        }, 40L);
        if(p.getUniqueId() != sender.getUniqueId() && this.isNicked) {
            NickManager sender = new NickManager(this.sender);
            sender.nickPlayer(senderName, "Steve");
            sender.updatePlayer();
            // TODO
            //api.updatePrefixSuffix("", "", prefix, suffix, prefix, suffix);
            api.updatePrefixSuffix(senderName, name, senderTagPrefix, senderTagSuffix, senderChatPrefix, senderChatSuffix, "", "", 0, "");
            sender.updatePlayer();
            this.sender.sendMessage(PREFIX + "Your nick was updated. Your name, prefix, and suffix nick is still the same! One thing that is changed is your skin. Feel free to change your skin by doing /changeskin");
        }
    }
    public class NickAnvilGUI {
        Player p;
        NickInfo nickInfo;
        public NickAnvilGUI(Player p) {
            this.p = p;
            this.nickInfo = NickInfo.this;
            p.sendMessage(ChatColor.GOLD + "Tip: Use 'NULL' on prefix, suffix, and skin to enter nothing!");
            AnvilGUI g1 = new AnvilGUI(p, event -> {
                System.out.println("AnvilClickEvent: g1(output=" + (event.getSlot() == AnvilSlot.OUTPUT) + ")");
                if(event.getSlot() == AnvilSlot.OUTPUT) {
                    event.setWillClose(true);
                    event.setWillDestroy(true);
                    nickInfo.name = event.getName();
                    AnvilGUI g2 = new AnvilGUI(p, event1 -> {
                        System.out.println("AnvilClickEvent: g2(output=" + (event1.getSlot() == AnvilSlot.OUTPUT) + ")");
                        if(event1.getSlot() == AnvilSlot.OUTPUT) {
                            event1.setWillClose(true);
                            event1.setWillDestroy(true);
                            nickInfo.prefix = event1.getName();
                            AnvilGUI g3 = new AnvilGUI(p, event11 -> {
                                System.out.println("AnvilClickEvent: g3(output=" + (event11.getSlot() == AnvilSlot.OUTPUT) + ")");
                                if(event11.getSlot() == AnvilSlot.OUTPUT) {
                                    event11.setWillClose(true);
                                    event11.setWillDestroy(true);
                                    nickInfo.suffix = event11.getName();
                                    AnvilGUI g4 = new AnvilGUI(p, event111 -> {
                                        System.out.println("AnvilClickEvent: g4(output=" + (event111.getSlot() == AnvilSlot.OUTPUT) + ")");
                                        if(event111.getSlot() == AnvilSlot.OUTPUT) {
                                            event111.setWillClose(true);
                                            event111.setWillDestroy(true);
                                            nickInfo.skin = event111.getName();
                                            if(p.getUniqueId().equals(nickInfo.p.getUniqueId())) p.sendMessage((ChatColor.translateAlternateColorCodes('&', "&aYou are &cCUSTOM-NICKED &aas: '" + nickInfo.prefix + nickInfo.name + nickInfo.suffix + "&a' with skin of " + nickInfo.skin).replace("%s%", " ")));
                                            else p.sendMessage((ChatColor.translateAlternateColorCodes('&', "&aYou have &cCUSTOM-NICKED &a" + name + " as: '" + nickInfo.prefix + nickInfo.name + nickInfo.suffix + "&a' with skin of " + nickInfo.skin).replace("%s%", " ")));
                                            nickInfo.nick();
                                        }else {
                                            event111.setWillClose(false);
                                            event111.setWillDestroy(false);
                                        }
                                    });
                                    g4.setSlot(AnvilSlot.INPUT_LEFT, InventoryEvents.rename(Material.PAPER, "Enter skin here"));
                                    try {
                                        g4.open();
                                    }catch(Exception e1) {
                                        p.sendMessage(ChatColor.RED + "An unexpected error occured! Message: " + e1.getMessage());
                                    }
                                }else {
                                    event11.setWillClose(false);
                                    event11.setWillDestroy(false);
                                }
                            });
                            g3.setSlot(AnvilSlot.INPUT_LEFT, InventoryEvents.rename(Material.PAPER, "Enter suffix here"));
                            try {
                                g3.open();
                            }catch(Exception e1) {
                                p.sendMessage(ChatColor.RED + "An unexpected error occured! Message: " + e1.getMessage());
                            }
                        }else {
                            event1.setWillClose(false);
                            event1.setWillDestroy(false);
                        }
                    });
                    g2.setSlot(AnvilSlot.INPUT_LEFT, InventoryEvents.rename(Material.PAPER, "Enter prefix here"));
                    try {
                        g2.open();
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.RED + "An unexpected error occured! Message: " + e1.getMessage());
                    }
                }else {
                    event.setWillClose(false);
                    event.setWillDestroy(false);
                }
            });
            g1.setSlot(AnvilSlot.INPUT_LEFT, InventoryEvents.rename(Material.PAPER, "Enter name here"));
            try {
                g1.open();
            }catch(Exception e1) {
                p.sendMessage(ChatColor.RED + "An unexpected error occured! Message: " + e1.getMessage());
            }
        }
    }
}
