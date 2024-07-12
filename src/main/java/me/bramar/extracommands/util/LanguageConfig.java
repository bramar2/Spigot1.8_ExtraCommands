package me.bramar.extracommands.util;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class LanguageConfig extends YamlConfiguration {
    private final Plugin plugin;
    private final File file;
    private char colorCode = '&';
    public LanguageConfig(Plugin plugin, String fileName) {
        this(plugin, fileName, null);
    }
    public LanguageConfig(Plugin plugin, String fileName, Configuration defaults) {
        try {
            this.plugin = plugin;
            load(this.file = new File(this.plugin.getDataFolder().getPath() + File.separator + fileName));
            if(defaults != null) this.defaults = defaults;
        }catch(IOException | InvalidConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public char getColorCode() {
        return colorCode;
    }
    public boolean setColorCode(char colorCode) {
        boolean o = this.colorCode != colorCode;
        this.colorCode = colorCode;
        return o;
    }
    public void reload() throws IOException, InvalidConfigurationException {
        this.save(file);
        this.load(file);
    }
    private String colored(String str) {
        if(str == null) return null;
        return ChatColor.translateAlternateColorCodes('&', str);
    }
    public String getLanguage(String path) {
        return colored(this.getString(path,null));
    }
    public String getDefaultLanguage(String path) {
        return colored(this.defaults.getString(path,null));
    }
    public void setLanguage(String path, String language) {
        this.set(path, language);
    }
    public void resetLanguage(String path) {
        String defaults = this.defaults.getString(path,null);
        Validate.notNull(defaults, "Path doesn't have a default value!");
        this.set(path, defaults);
    }
    public boolean isDefault(String path, boolean ignoreCase) {
        String s = this.getString(path,null);
        String d = this.getDefaults().getString(path,null);
        return (s == null && d == null) || (s != null && (ignoreCase ? s.equalsIgnoreCase(d) : s.equals(d)));
    }
}
