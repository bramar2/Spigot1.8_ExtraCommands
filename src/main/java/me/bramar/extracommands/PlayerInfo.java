package me.bramar.extracommands;

import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Getter
public class PlayerInfo {
    String displayName;
    Group group;
    boolean nicked;
    Main main;
    LuckPerms lp;
    public PlayerInfo(Main main, Player p) {
        this.main = main;
        this.lp = main.lp;
        if(p == null) throw new NullPointerException("Player can't be null!");
//        NickManager api = new NickManager(p);
        User user = lp.getPlayerAdapter(Player.class).getUser(p);
//        if(api.isNicked()) {
//            this.nicked = true;
//            this.displayName = api.getChatPrefix() + api.getNickName() + api.getChatSuffix();
//            this.group = Group.fromNickPrefix(api.getChatPrefix(), main);
//        }else {
            this.nicked = false;
            String prefix = user.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getPrefix();
            String suffix = user.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getSuffix();
            this.displayName = ChatColor.translateAlternateColorCodes('&', (prefix == null ? "" : prefix) + p.getName() + (suffix == null ? "" : suffix));
            this.group = Group.fromGroupName(user.getPrimaryGroup(), main);
//        }
    }
    public enum Group {
        ADMINS, STAFF, YOUTUBE, MVPPLUSPLUS, MVPPLUS, MVP, DEFAULT;

        public static Group fromNickPrefix(String prefix, Main main) {
            for(String sectionName : main.getGroupConfig().getKeys(false)) {
                try {
                    ConfigurationSection section = main.getGroupConfig().getConfigurationSection(sectionName);
                    String sp = section.getString("prefix");
                    sp = ChatColor.translateAlternateColorCodes('&', sp);
                    if(!sp.equalsIgnoreCase("multiple_prefix") && prefix.equalsIgnoreCase(sp)) {
                        return Group.valueOf(section.getString("id").toUpperCase());
                    }else if(sp.equalsIgnoreCase("multiple_prefix")) {
                        try {
                            boolean contains = false;
                            for(String s : section.getStringList("prefixes")) {
                                if(s.endsWith(" ")) s = s.substring(0, s.length() - 1);
                                s = ChatColor.translateAlternateColorCodes('&', s);
                                if(prefix.equalsIgnoreCase(s)) {
                                    contains = true;
                                    break;
                                }
                            }
                            if(!contains) continue;
                            return Group.valueOf(section.getString("id").toUpperCase());
                        }catch(Exception ignored) {

                        }
                    }
                }catch(Exception ignored) {

                }
            }
            return Group.DEFAULT;
        }
        public static Group fromGroupName(String groupName, Main main) {
            for(String sectionName : main.getGroupConfig().getKeys(false)) {
                try {
                    ConfigurationSection section = main.getGroupConfig().getConfigurationSection(sectionName);
                    for(String s : section.getStringList("groups")) {
                        if(groupName.equalsIgnoreCase(s)) return Group.valueOf(section.getString("id").toUpperCase());
                    }
                }catch(Exception e1) {
                    e1.printStackTrace();
                }
            }
            return Group.DEFAULT;
        }
        public static String getDisplayName(Group group, Main main) {
            for(String sectionName : main.getGroupConfig().getKeys(false)) {
                try {
                    ConfigurationSection section = main.getGroupConfig().getConfigurationSection(sectionName);
                    if(group.toString().equalsIgnoreCase(section.getString("id"))) return section.getString("displayname");
                }catch(Exception ignored) {

                }
            }
            return "Default";
        }
    }
}