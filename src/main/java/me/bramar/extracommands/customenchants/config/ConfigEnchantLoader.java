package me.bramar.extracommands.customenchants.config;

import com.google.common.collect.ImmutableList;
import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantLoader;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.checks.*;
import me.bramar.extracommands.customenchants.commands.*;
import me.bramar.extracommands.events.PreRegisteringEnchant;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Class for Loading custom enchants in config
 * @author bramar
  */
public final class ConfigEnchantLoader implements Listener {
    private final Main main;
    private static ConfigEnchantLoader instance;
    private final File enchantsDir;
    final List<Class<? extends EnchantCommand>> commands = new ArrayList<>();
    final List<Class<? extends Check>> checks = new ArrayList<>();
    private final List<CustomEnchantment> registry = new ArrayList<>();
    int currentId = 35300;
    public EnchantListener listener;

    @EventHandler
    public void onRegister(PreRegisteringEnchant e) {
        e.registerEnchantment(registry);
    }
    public static ConfigEnchantLoader getInstance() {
        return instance;
    }
    public ConfigEnchantLoader() {
        if(instance != null) throw new UnsupportedOperationException("This can only be initialized once!");
        instance = this;
        this.main = Main.getInstance();
        main.getLogger().info("[CustomEnchants/ConfigEnchants] Loading built-in checks and commands...");
        main.getServer().getPluginManager().registerEvents(this, main);
        enchantsDir = new File(main.getDataFolder()+ File.separator+"enchants");
        if(!enchantsDir.exists())
            if(!enchantsDir.mkdir()) main.getLogger().warning("Unable to create a new folder!");
        main.saveResource("enchants" + File.separator + "-template.yml",false);
        // Loading checks and commands
        registerCheck(EnchantLevel.class,
                Percentage.class,
                BlockType.class,
                DamageCauseCheck.class,
                DamageCheck.class,
                EntityTypeCheck.class,
                ExpCheck.class,
                ExpToLevelCheck.class,
                FinalDamageCheck.class,
                FoodCheck.class,
                FullExpCheck.class,
                HasPotion.class,
                HealthCheck.class,
                LevelCheck.class,
                LocationCheck.class);
        registerCommand(ActionbarCMD.class,
                AttackNearbyCMD.class,
                BoostCMD.class,
                CancelEventCMD.class,
                ChangeDamageCMD.class,
                ChatCMD.class,
                CommandCMD.class,
                ConsoleCMD.class,
                DropHeadCMD.class,
                ExpCMD.class,
                ExtinguishCMD.class,
                FireballCMD.class,
                FlameCMD.class,
                HealCMD.class,
                InvincibleCMD.class,
                LightningCMD.class,
                MessageCMD.class,
                OxygenCMD.class,
                PlaySoundCMD.class,
                PotionCMD.class,
                RemoveKnockbackCMD.class,
                RepairCMD.class,
                SaturateCMD.class,
                SetItemDamageCMD.class,
                SubtitleCMD.class,
                TeleportCMD.class,
                TitleCMD.class);
        main.getServer().getPluginManager().registerEvents(listener = new EnchantListener(), main);
        main.getLogger().info("[CustomEnchants/ConfigEnchants] Config Enchants will be loaded once the server fully loads!");
        new BukkitRunnable() {
            @Override
            public void run() {
                main.getLogger().info("[CustomEnchants/ConfigEnchants] Unpacking enchants from config files...");
                try {
                    loadEnchants();
                }catch(Exception e) {
                    e.printStackTrace();
                    main.getLogger().warning("[CustomEnchants/ConfigEnchants] Failed to load enchants from config files!");
                }
            }
        }.runTaskLater(main, 20L);
    }
    public List<Class<? extends Check>> getChecks() {
        return ImmutableList.copyOf(checks);
    }
    public List<Class<? extends EnchantCommand>> getCommands() {
        return ImmutableList.copyOf(commands);
    }
    private void loadEnchants() throws Exception {
        long nanoTime = System.nanoTime();
        File[] files = enchantsDir.listFiles(pathname -> pathname.getName().toLowerCase().endsWith(".yml") && !pathname.getName().startsWith("-"));
        final int[] loaded = {0};
        if(files != null) Arrays.stream(files).forEach((file) -> {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name");
                int id = config.getInt("id", -1); // Optional
                if(id < 0) id = currentId++;
                else try {
                    if(Enchantment.getById(id) != null) id = currentId++;
                }catch(Exception ignored) {}
                String displayName = config.getString("display_name");
                String description = config.getString("description");
                EnchantmentTarget target = EnchantmentTarget.valueOf(config.getString("target").toUpperCase());
                int start = config.getInt("startlvl");
                int max = config.getInt("maxlvl");
                int multiplier = config.getInt("multiplier", 4);
                ArrayList<Enchantment> conflicts = new ArrayList<>();
                try {
                    config.getStringList("conflicts")
                            .forEach((str) -> {
                               try {
                                   conflicts.add(Enchantment.getByName(str));
                               }catch(Exception e1) {
                                   try {
                                       conflicts.add(Enchantment.getById(Integer.parseInt(str)));
                                   }catch(Exception ignored) {}
                               }
                            });
                }catch(Exception ignored) {}
                requireNonNull(new Object[] {name, displayName, description, target}, new String[] {"Enchant name", "Display name", "Description", "Enchant target"});
                List<Ability> abilities = new ArrayList<>();
                try {
                    ConfigurationSection abilitiesCfg = config.getConfigurationSection("abilities");
                    for(String key : abilitiesCfg.getKeys(false)) {
                        if(abilitiesCfg.isConfigurationSection(key)) {
                            try {
                                ConfigurationSection section = abilitiesCfg.getConfigurationSection(key);
                                String event = section.getString("event");
                                int ticks = section.getInt("ticks",0);
                                double chance = section.getDouble("chance",100d);
                                int cooldown = section.getInt("cooldown",0);
                                List<Check> checks = new ArrayList<>();
                                checks.add(new Percentage("PERCENTAGE:"+chance));
                                try {
                                    checks.addAll(
                                            section.getStringList("check")
                                            .stream()
                                            .map(this::getCheck)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList())
                                    );
                                }catch(Exception ignored) {}
                                List<EnchantCommand> commands = section
                                        .getStringList("command")
                                        .stream()
                                        .map(this::getCommand)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                                List<EnchantLevel> lvl = section
                                        .getStringList("lvl")
                                        .stream()
                                        .map(this::getCheck)
                                        .filter((c) -> c instanceof EnchantLevel)
                                        .map((c) -> (EnchantLevel) c)
                                        .collect(Collectors.toList());
                                boolean additive = section.getBoolean("additive", false);
                                Ability ability = new Ability(checks, commands, event, lvl, additive);
                                ability.setTicks(ticks);
                                ability.setCooldownTime(cooldown);
                                abilities.add(ability);
                            }catch(Exception e1) {
                                main.getLogger().warning("[CustomEnchants/ConfigEnchants] Failed to load ability " + file.getName() + ":" + key + "! Error: " + e1.getClass().getName() + ": " + e1.getMessage());
                            }
                        }
                    }
                }catch(Exception ignored) {}
                registry.add(new ConfigEnchantment(id, target, max, start, name, displayName, description, multiplier, abilities, conflicts.toArray(new Enchantment[0])));
                loaded[0]++;
            }catch(Exception e1) {
                e1.printStackTrace();
                main.getLogger().warning("[CustomEnchants/ConfigEnchants] Failed to load " + file.getName() + " custom enchant! Error: " + e1.getClass().getName() + ": " + e1.getMessage());
            }
        });
        main.getLogger().info((loaded[0] <= 0) ? "[CustomEnchants/ConfigEnchants] No enchants was loaded!" : "[CustomEnchants/ConfigEnchants] Loaded " + loaded[0] + " enchant" + ((loaded[0] > 1) ? "s!" : "!"));
        main.getLogger().info("[CustomEnchants/ConfigEnchants] Registering custom enchants...");
        final Field enchField = Enchantment.class.getDeclaredField("acceptingNew");
        enchField.setAccessible(true);
        enchField.set(null, true);
        registry.stream().filter(Objects::nonNull).forEach((s) -> {
            Throwable t = s.registerEnchantment();
            if(t != null) {
                main.getLogger().warning(Main.getInstance().getSimplified(t,
                        ConfigEnchantment.class,
                        ConfigEnchantLoader.class,
                        Enchantment.class));
                main.getLogger().warning("[CustomEnchants/ConfigEnchants] Failed to register " + s.getName() + " [" + s.getId() + "]");
            }else EnchantLoader.getInstance().getEnchants().add(s);
        });
        enchField.set(null, false);
        double time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nanoTime);
        main.getLogger().info("[CustomEnchants/ConfigEnchants] Finished unpacking custom enchants! [Took " + time + "ms]");
    }
    public Check getCheck(String checkName) {
        for(Class<? extends Check> checkClass : checks) {
            try {
                // Would return IllegalArgumentException if it is the wrong one
                return checkClass.getConstructor(String.class).newInstance(checkName);
            }catch(Exception ignored) {}
        }
        return null;
    }
    public EnchantCommand getCommand(String command) {
        for(Class<? extends EnchantCommand> commandClass : commands) {
            try {
                // Would return IllegalArgumentException if it is the wrong one
                return commandClass.getConstructor(String.class).newInstance(command);
            }catch(Exception ignored) {}
        }
        return null;
    }
    private void requireNonNull(Object[] objects, String[] messages) {
        for(int i = 0; i < objects.length; i++) {
            Object obj = objects[i];
            if(obj == null) {
                String msg = null;
                try {
                    msg = messages[i];
                }catch(Exception ignored) {}
                if(msg == null) throw new NullPointerException();
                else throw new NullPointerException(msg);
            }
        }
    }
    @SafeVarargs
    public final void registerCommand(Class<? extends EnchantCommand>... clazz) {
        commands.addAll(Arrays.asList(clazz));
    }
    @SafeVarargs
    public final void registerCheck(Class<? extends Check>... clazz) {
        checks.addAll(Arrays.asList(clazz));
    }
}
