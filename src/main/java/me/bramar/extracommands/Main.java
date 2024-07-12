package me.bramar.extracommands;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.collect.Lists;
import lombok.Getter;
import me.bramar.extracommands.citizens.NoKB;
import me.bramar.extracommands.citizens.PunchBot;
import me.bramar.extracommands.commands.*;
import me.bramar.extracommands.customenchants.EnchantLoader;
import me.bramar.extracommands.customenchants.config.Check;
import me.bramar.extracommands.customenchants.config.ConfigEnchantLoader;
import me.bramar.extracommands.customenchants.config.EnchantCommand;
import me.bramar.extracommands.events.HypixelFireball;
import me.bramar.extracommands.events.HypixelTNT;
import me.bramar.extracommands.mobs.AngryVillager;
import me.bramar.extracommands.mobs.CustomMob;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import net.dev.eazynick.api.NickManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.apache.commons.lang.Validate;
import org.apache.commons.validator.routines.UrlValidator;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

public final class Main extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    public static final String PREFIX = ChatColor.GOLD + "ExtraCommands > ";
    private static final String MSG_SEND_FORMAT = ChatColor.GOLD + "[" + ChatColor.RED + "You" + ChatColor.GOLD + " -> " + ChatColor.RED + "%entity%" + ChatColor.GOLD + "] " + ChatColor.RESET + "%msg%";
    private static final String MSG_RECEIVE_FORMAT = ChatColor.GOLD + "[" + ChatColor.RED + "%entity%" + ChatColor.GOLD + " -> " + ChatColor.RED + "You" + ChatColor.GOLD + "] " + ChatColor.RESET + "%msg%";
    public LuckPerms lp;
    private InventoryEvents inv;
    private File db;
    private FileConfiguration database;
//    private final List<String> fakePlugins = new ArrayList<>();
    @Getter
    private final HashMap<UUID, UUID> lastMessage = new HashMap<>();
    @Getter
    private final ArrayList<UUID> socialSpy = new ArrayList<>();
    @Getter
    private final ArrayList<UUID> messageMute = new ArrayList<>();
    @Getter
    private final ArrayList<UUID> messageToggle = new ArrayList<>();
    @Getter
    public List<String> ignoreexempt = new ArrayList<>();
    @Getter
    public HashMap<UUID, List<UUID>> ignorelist = new HashMap<>();
    @Getter
    protected ProtocolManager protocolManager;
    private String joinMsg;
    private String leaveMsg;
    private String chatFormat;
    private File group;
    private FileConfiguration groupConfig;
    private File ud;
    private FileConfiguration udcfg;
    private File normalCfgFile;
    private FileConfiguration normalCfg;
    public boolean consoleSocialSpy = false;

    public <K, V> Map.Entry<K, V> immutableEntry(K obj1, V obj2) {
        return new Map.Entry<K, V>() {
            @Override
            public K getKey() {
                return obj1;
            }

            @Override
            public V getValue() {
                return obj2;
            }

            @Override
            public V setValue(Object value) {
                throw new UnsupportedOperationException();
            }
        };
    }
    @Override
    public void saveResource(String resourcePath, boolean replace) {
        Validate.notNull(resourcePath, "ResourcePath cannot be null");
        Validate.notEmpty(resourcePath, "ResourcePath cannot be empty");
        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        Validate.isTrue(in != null, String.format("The embedded resource '%s' cannot be found in %s", resourcePath, getFile()));
        File outFile = new File(getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));
        if(!outDir.exists()) {
            if(!outDir.mkdirs()) {
                getLogger().log(Level.SEVERE, String.format("Could not save %s because its folder could not be created (%s)", outFile.getName(), outDir.getPath()));
                return;
            }
        }

        try {
            if(!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, String.format("Could not save %s to %s", outFile.getName(), outFile), ex);
        }
    }

    /**
     * Make a LivingEntity jump, velocity code is taken from MCP source code and modified a bit:
     * EntityLiving.java
     * MathHelper.java
     *
     * Side note: MathHelper.java (from MCP) replaced by net.minecraft.server.{version}.MathHelper.java
     */
    public void jump(LivingEntity e) {
        Vector velocity = e.getVelocity();
        double y = 0.42;
        if(e.hasPotionEffect(PotionEffectType.JUMP)) y += (float)(getActivePotionEffect(e, PotionEffectType.JUMP).getAmplifier() + 1) * 0.1F;
        velocity.setY(y);
        if(e instanceof Player) {
            Player p = (Player) e;
            if(p.hasMetadata("NPC") /* NPCs technically don't have sprinting value */ || p.isSprinting()) {
                float f = e.getLocation().getYaw() * 0.017453292F;
                velocity.setX(velocity.getX() - ((double)(MathHelper.sin(f) * 0.2F)));
                velocity.setZ(velocity.getZ() - ((double)(MathHelper.cos(f) * 0.2F)));
            }
        }
        e.setVelocity(velocity);
    }
    public PotionEffect getActivePotionEffect(LivingEntity e, PotionEffectType type) {
        for(PotionEffect potion : e.getActivePotionEffects()) {
            if(potion.getType() == type) return potion;
        }
        return null;
    }
    public double calculatePercentageArmor1_8(DamageCause cause, LivingEntity damaged) {
        // Armor (1.8 doesn't have generic.armor and generic.armorToughness attribute)
        double armorPoints = 0;
        ItemStack helm = damaged.getEquipment().getHelmet();
        ItemStack chest = damaged.getEquipment().getChestplate();
        ItemStack legs = damaged.getEquipment().getLeggings();
        ItemStack feet = damaged.getEquipment().getBoots();
        /*  Mass coding: Wanted to use reflection but remembered still had to put Material Types and armor points*/
        if(helm != null) {if(helm.getType() == Material.LEATHER_HELMET) armorPoints += 1;
            if(helm.getType() == Material.GOLD_HELMET) armorPoints += 2;
            if(helm.getType() == Material.CHAINMAIL_HELMET) armorPoints += 2;
            if(helm.getType() == Material.IRON_HELMET) armorPoints += 2;
            if(helm.getType() == Material.DIAMOND_HELMET) armorPoints += 3;
            if(helm.getType() == Material.LEATHER_CHESTPLATE) armorPoints += 3;
            if(helm.getType() == Material.GOLD_CHESTPLATE) armorPoints += 5;
            if(helm.getType() == Material.CHAINMAIL_CHESTPLATE) armorPoints += 5;
            if(helm.getType() == Material.IRON_CHESTPLATE) armorPoints += 6;
            if(helm.getType() == Material.DIAMOND_CHESTPLATE) armorPoints += 8;
            if(helm.getType() == Material.LEATHER_LEGGINGS) armorPoints += 2;
            if(helm.getType() == Material.GOLD_LEGGINGS) armorPoints += 3;
            if(helm.getType() == Material.CHAINMAIL_LEGGINGS) armorPoints += 4;
            if(helm.getType() == Material.IRON_LEGGINGS) armorPoints += 5;
            if(helm.getType() == Material.DIAMOND_LEGGINGS) armorPoints += 6;
            if(helm.getType() == Material.LEATHER_BOOTS) armorPoints += 1;
            if(helm.getType() == Material.GOLD_BOOTS) armorPoints += 1;
            if(helm.getType() == Material.CHAINMAIL_BOOTS) armorPoints += 1;
            if(helm.getType() == Material.IRON_BOOTS) armorPoints += 2;
            if(helm.getType() == Material.DIAMOND_BOOTS) armorPoints += 3;}
        if(chest != null){if(chest.getType() == Material.LEATHER_HELMET) armorPoints += 1;
            if(chest.getType() == Material.GOLD_HELMET) armorPoints += 2;
            if(chest.getType() == Material.CHAINMAIL_HELMET) armorPoints += 2;
            if(chest.getType() == Material.IRON_HELMET) armorPoints += 2;
            if(chest.getType() == Material.DIAMOND_HELMET) armorPoints += 3;
            if(chest.getType() == Material.LEATHER_CHESTPLATE) armorPoints += 3;
            if(chest.getType() == Material.GOLD_CHESTPLATE) armorPoints += 5;
            if(chest.getType() == Material.CHAINMAIL_CHESTPLATE) armorPoints += 5;
            if(chest.getType() == Material.IRON_CHESTPLATE) armorPoints += 6;
            if(chest.getType() == Material.DIAMOND_CHESTPLATE) armorPoints += 8;
            if(chest.getType() == Material.LEATHER_LEGGINGS) armorPoints += 2;
            if(chest.getType() == Material.GOLD_LEGGINGS) armorPoints += 3;
            if(chest.getType() == Material.CHAINMAIL_LEGGINGS) armorPoints += 4;
            if(chest.getType() == Material.IRON_LEGGINGS) armorPoints += 5;
            if(chest.getType() == Material.DIAMOND_LEGGINGS) armorPoints += 6;
            if(chest.getType() == Material.LEATHER_BOOTS) armorPoints += 1;
            if(chest.getType() == Material.GOLD_BOOTS) armorPoints += 1;
            if(chest.getType() == Material.CHAINMAIL_BOOTS) armorPoints += 1;
            if(chest.getType() == Material.IRON_BOOTS) armorPoints += 2;
            if(chest.getType() == Material.DIAMOND_BOOTS) armorPoints += 3;}
        if(legs != null) {if(legs.getType() == Material.LEATHER_HELMET) armorPoints += 1;
            if(legs.getType() == Material.GOLD_HELMET) armorPoints += 2;
            if(legs.getType() == Material.CHAINMAIL_HELMET) armorPoints += 2;
            if(legs.getType() == Material.IRON_HELMET) armorPoints += 2;
            if(legs.getType() == Material.DIAMOND_HELMET) armorPoints += 3;
            if(legs.getType() == Material.LEATHER_CHESTPLATE) armorPoints += 3;
            if(legs.getType() == Material.GOLD_CHESTPLATE) armorPoints += 5;
            if(legs.getType() == Material.CHAINMAIL_CHESTPLATE) armorPoints += 5;
            if(legs.getType() == Material.IRON_CHESTPLATE) armorPoints += 6;
            if(legs.getType() == Material.DIAMOND_CHESTPLATE) armorPoints += 8;
            if(legs.getType() == Material.LEATHER_LEGGINGS) armorPoints += 2;
            if(legs.getType() == Material.GOLD_LEGGINGS) armorPoints += 3;
            if(legs.getType() == Material.CHAINMAIL_LEGGINGS) armorPoints += 4;
            if(legs.getType() == Material.IRON_LEGGINGS) armorPoints += 5;
            if(legs.getType() == Material.DIAMOND_LEGGINGS) armorPoints += 6;
            if(legs.getType() == Material.LEATHER_BOOTS) armorPoints += 1;
            if(legs.getType() == Material.GOLD_BOOTS) armorPoints += 1;
            if(legs.getType() == Material.CHAINMAIL_BOOTS) armorPoints += 1;
            if(legs.getType() == Material.IRON_BOOTS) armorPoints += 2;
            if(legs.getType() == Material.DIAMOND_BOOTS) armorPoints += 3;}
        if(feet != null) {if(feet.getType() == Material.LEATHER_HELMET) armorPoints += 1;
            if(feet.getType() == Material.GOLD_HELMET) armorPoints += 2;
            if(feet.getType() == Material.CHAINMAIL_HELMET) armorPoints += 2;
            if(feet.getType() == Material.IRON_HELMET) armorPoints += 2;
            if(feet.getType() == Material.DIAMOND_HELMET) armorPoints += 3;
            if(feet.getType() == Material.LEATHER_CHESTPLATE) armorPoints += 3;
            if(feet.getType() == Material.GOLD_CHESTPLATE) armorPoints += 5;
            if(feet.getType() == Material.CHAINMAIL_CHESTPLATE) armorPoints += 5;
            if(feet.getType() == Material.IRON_CHESTPLATE) armorPoints += 6;
            if(feet.getType() == Material.DIAMOND_CHESTPLATE) armorPoints += 8;
            if(feet.getType() == Material.LEATHER_LEGGINGS) armorPoints += 2;
            if(feet.getType() == Material.GOLD_LEGGINGS) armorPoints += 3;
            if(feet.getType() == Material.CHAINMAIL_LEGGINGS) armorPoints += 4;
            if(feet.getType() == Material.IRON_LEGGINGS) armorPoints += 5;
            if(feet.getType() == Material.DIAMOND_LEGGINGS) armorPoints += 6;
            if(feet.getType() == Material.LEATHER_BOOTS) armorPoints += 1;
            if(feet.getType() == Material.GOLD_BOOTS) armorPoints += 1;
            if(feet.getType() == Material.CHAINMAIL_BOOTS) armorPoints += 1;
            if(feet.getType() == Material.IRON_BOOTS) armorPoints += 2;
            if(feet.getType() == Material.DIAMOND_BOOTS) armorPoints += 3;}
        System.out.println("[DEBUG] armorPoints: " + armorPoints);
        double percentage = armorPoints * 4;
        System.out.println("[DEBUG] Percentage before ench: " + percentage);
        // Enchantment check (protection, proj prot, blast prot, etc)
        int protAll = getLevel(helm, Enchantment.PROTECTION_ENVIRONMENTAL) +
                getLevel(chest, Enchantment.PROTECTION_ENVIRONMENTAL) +
                getLevel(legs, Enchantment.PROTECTION_ENVIRONMENTAL) +
                getLevel(feet, Enchantment.PROTECTION_ENVIRONMENTAL);
        int protBlast = getLevel(helm, Enchantment.PROTECTION_EXPLOSIONS) +
                getLevel(chest, Enchantment.PROTECTION_EXPLOSIONS) +
                getLevel(legs, Enchantment.PROTECTION_EXPLOSIONS) +
                getLevel(feet, Enchantment.PROTECTION_EXPLOSIONS);
        int protFire = getLevel(helm, Enchantment.PROTECTION_FIRE) +
                getLevel(chest, Enchantment.PROTECTION_FIRE) +
                getLevel(legs, Enchantment.PROTECTION_FIRE) +
                getLevel(feet, Enchantment.PROTECTION_FIRE);
        int protProj = getLevel(helm, Enchantment.PROTECTION_PROJECTILE) +
                getLevel(chest, Enchantment.PROTECTION_PROJECTILE) +
                getLevel(legs, Enchantment.PROTECTION_PROJECTILE) +
                getLevel(feet, Enchantment.PROTECTION_PROJECTILE);
        percentage += protAll * 4;
        if(cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION) percentage += protBlast * 8;
        if(cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK) percentage += protFire * 8;
        if(cause == DamageCause.PROJECTILE) percentage += protProj * 8;
        if(cause == DamageCause.FALL) {
            int featherFalling = getLevel(helm, Enchantment.PROTECTION_FALL) +
                    getLevel(chest, Enchantment.PROTECTION_FALL) +
                    getLevel(legs, Enchantment.PROTECTION_FALL) +
                    getLevel(feet, Enchantment.PROTECTION_FALL);
            percentage += featherFalling * 12;
        }
        System.out.println("[DEBUG] Final percentage: " + percentage);
        if(percentage < 0) return 0;
        if(percentage > 100) return 100;
        return percentage;
    }
    public double calculateArmorProtection1_8(DamageCause cause, double damage, LivingEntity damaged) {
        return damage - (damage * calculatePercentageArmor1_8(cause, damaged) / 100);
    }
    public double calculateDamage1_8(DamageCause cause, LivingEntity damager, LivingEntity damaged) {
        if(cause == DamageCause.THORNS) return calculateArmorProtection1_8(cause, 4, damaged);
        if(cause == DamageCause.PROJECTILE) {
            if(damager instanceof Arrow) {
                Arrow arrow = (Arrow) damager;
                if(arrow.isCritical()) return calculateArmorProtection1_8(cause, 10, damaged);
                return calculateArmorProtection1_8(cause, 4, damaged);
            }else return calculateArmorProtection1_8(cause, 2, damaged);
        }
        if(cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION) {
            // Unable to calculate explosion damage. (No NMS calculate methods found)
            return calculateArmorProtection1_8(cause, 3, damaged);
        }
        if(cause == DamageCause.FALLING_BLOCK || cause == DamageCause.SUFFOCATION || cause == DamageCause.DROWNING) return calculateArmorProtection1_8(cause, 2, damaged);
        if(cause == DamageCause.FIRE) return calculateArmorProtection1_8(cause, 6, damaged);
        if(cause == DamageCause.FIRE_TICK || cause == DamageCause.STARVATION || cause == DamageCause.POISON) return calculateArmorProtection1_8(cause, 1, damaged);
        if(cause == DamageCause.SUICIDE) return calculateArmorProtection1_8(cause, 20, damaged);
        if(cause == DamageCause.FALL) return calculateArmorProtection1_8(cause, 4, damaged);
        if(cause == DamageCause.VOID) return 4;
        if(!(cause == DamageCause.ENTITY_ATTACK)) return calculateArmorProtection1_8(cause, 4, damaged);

        // Resistance check
        int resistance = 0;
        if(damaged.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            try {
                resistance = Main.getInstance().getActivePotionEffect(damaged, PotionEffectType.DAMAGE_RESISTANCE)
                        .getAmplifier();
                if(resistance >= 5) return 0; // Immune
            }catch(Exception ignored) {}
        }
        System.out.println("[DEBUG] Resistance: " + resistance);
        // Item Damage (Swords, axes, etc)
        double itemDamage = 1;
        ItemStack item = damager.getEquipment().getItemInHand();
        if(item == null) itemDamage = 0;
        else if(item.getType() == Material.WOOD_SWORD || item.getType() == Material.GOLD_SWORD
                || item.getType() == Material.STONE_AXE || item.getType()
                == Material.IRON_PICKAXE) itemDamage = 5;
        else if(item.getType() == Material.STONE_SWORD
                || item.getType() == Material.IRON_AXE || item.getType() == Material.
                DIAMOND_PICKAXE) itemDamage = 6;
        else if(item.getType() == Material.IRON_SWORD
                || item.getType() == Material.DIAMOND_AXE) itemDamage = 7;
        else if(item.getType() == Material.DIAMOND_SWORD) itemDamage = 8;
        else if(item.getType() == Material.WOOD_AXE ||
                item.getType() == Material.GOLD_AXE ||
                item.getType() == Material.STONE_PICKAXE) itemDamage = 4;
        else if(item.getType() == Material.WOOD_PICKAXE
                || item.getType() == Material.GOLD_PICKAXE) itemDamage = 3;
        else if(getShovelLevel(item) != null) {
            // Shovel
            String shovelLevel = getShovelLevel(item);
            if(shovelLevel.equalsIgnoreCase("wood") || shovelLevel.equalsIgnoreCase("wooden") || shovelLevel.equalsIgnoreCase("gold") || shovelLevel.equalsIgnoreCase("golden"))
                itemDamage = 2;
            if(shovelLevel.equalsIgnoreCase("stone")) itemDamage = 3;
            if(shovelLevel.equalsIgnoreCase("iron")) itemDamage = 4;
            if(shovelLevel.equalsIgnoreCase("diamond")) itemDamage = 5;
        }
        System.out.println("[DEBUG] Material type [DEV]: " + item.getType().name() + ", itemDamage: " + itemDamage);

        boolean hasAttackDamage = false;
        double attackDamage = itemDamage; // Custom items or if the item has one (like a sword that might have been not detected)
        double operation1 = 0;
        boolean firstOperation1 = false;
        // Attack damage attribute
        try {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            if(!nmsStack.hasTag()) throw new Exception("An easy way to return but only in this try-catch [Doesn't have a tag]");
            NBTTagCompound compound = nmsStack.getTag();
            NBTTagList list = compound.getList("AttributeModifiers", 0);
            for(int i = 0; i < list.size(); i++) {
                try {
                    NBTTagCompound attr = list.get(i);
                    if(attr.getString("AttributeName").equalsIgnoreCase(attr.getString("Name")) && (attr.getString("Name").equalsIgnoreCase("generic.attackDamage") || attr.getString("Name").equalsIgnoreCase("generic.attack_damage"))) {
                        // Attack Damage
                        hasAttackDamage = true;
                        try {
                            double amount = attr.getDouble("Amount");
                            int operation = attr.getInt("Operation");
                            if(operation == 0) attackDamage += amount;
                            else if(operation == 1) {
                                if(firstOperation1) operation1 += 1 + amount;
                                else operation1 += amount;
                                firstOperation1 = true;
                            }
                            else if(operation == 2) attackDamage *= (1 + amount);
                        }catch(Exception e) {
                            int amount = attr.getInt("Amount");
                            int operation = attr.getInt("Operation");
                            if(operation == 0) attackDamage += amount;
                            else if(operation == 1) {
                                if(firstOperation1) operation1 += 1 + amount;
                                else operation1 += amount;
                                firstOperation1 = true;
                            }
                            else if(operation == 2) attackDamage *= (1 + amount);
                        }
                    }
                }catch(Exception ignored) {}
            }
            if(hasAttackDamage) {
                // Calculate operation 1
                attackDamage *= operation1;
            }
        }catch(Exception ignored) { /* Just incase if the item doesnt have an attack damage attribute */}
        System.out.println("Has attack damage: " + hasAttackDamage + ", Attack damage base: " + attackDamage);
        // Fields: resistance, itemDamage (Sword), attackDamage (Attribute)
        double semiTotal = (hasAttackDamage ? attackDamage : itemDamage);
        System.out.println("[DEBUG] semiTotal = " + semiTotal);
        // Damage multiplier enchantment
        double damageEnch = 0;
        int mobType = 0; // 0 = Normal (For sharp), Undead (For smite and sharp), Arthropods (For bane of arthropods and sharp)
        EntityType type = damaged.getType();
        try {
            boolean undead = false;
            boolean arthropods = false;
            for(String s : this.undead) {
                if(type.toString().toLowerCase().contains(s.toLowerCase()) && type.toString().toLowerCase().replace(s.toLowerCase(), "").toCharArray().length <= 3) {
                    undead = true;
                    break;
                }
            }
            if(!undead) for(String s : this.arthropods) {
                if(type.toString().toLowerCase().contains(s.toLowerCase()) && type.toString().toLowerCase().replace(s.toLowerCase(), "").toCharArray().length <= 3) {
                    arthropods = true;
                    break;
                }
            }
            if(undead) mobType = 1;
            else if(arthropods) mobType = 2;
        }catch(Exception ignored) {}
        System.out.println("[DEBUG] EntityType: " + type.name() + " mobType: " + mobType);
        if(getLevel(item, Enchantment.DAMAGE_ALL) != 0) {
            System.out.println("[DEBUG] Enchantment Sharpness exist");
            damageEnch += (0.5 * getLevel(item, Enchantment.DAMAGE_ALL) + 0.5);
        }
        if(mobType == 1 && getLevel(item, Enchantment.DAMAGE_UNDEAD) != 0) {
            System.out.println("[DEBUG] Enchantment Smite exist");
            damageEnch += getLevel(item, Enchantment.DAMAGE_UNDEAD) * 2.5;
        }
        if(mobType == 2 && getLevel(item, Enchantment.DAMAGE_ARTHROPODS) != 0) {
            System.out.println("[DEBUG] Enchantment Bane of arthropods exist");
            damageEnch += getLevel(item, Enchantment.DAMAGE_ARTHROPODS) * 2.5;
        }
        System.out.println("[DEBUG] damageEnch: " + damageEnch);
        semiTotal += damageEnch;
        // Semitotal = If attack attribute damage exists, use Attack Attribute Damage else
        // use Item base damage. Added with Sharpness, Smite, and Bane of Arthropods enchantment
        // calculated (from MC wiki)
        //
        // The only things that are missing are the following:
        // - Damaged's armor - Damaged's protection and other protection type armor
        double percentage = calculatePercentageArmor1_8(cause, damaged);
        // 1.8 MC Wiki: Each armor point counts for 4% and each protection level also counts for 4%
        // All of the percentages will be added up and removed from the semiTotal
        System.out.println("[DEBUG] calculatePercentageArmor1_8: " + percentage);
        double total = percentage == 0 ? semiTotal : semiTotal - (semiTotal * percentage / 100);
        System.out.println("[DEBUG] Total (without resistance): " + total);
        if(resistance != 0) total -= total * (resistance * 20) / 100;
        System.out.println("[DEBUG] Total (witho resistance): " + total + ", Resistance: " + resistance);
        return total;
    }

    public int getLevel(ItemStack item, Enchantment ench) {
        if(!item.containsEnchantment(ench)) return 0;

        try {
            return item.getItemMeta().getEnchantLevel(ench);
        }catch(Exception ignored) {}
        return 0;
    }

    /**
     * Damage the damagedEntity realistically from attacker with realistic/close to accurate
     * damage calculation (including armor) and knockback (not accurate through the client POV)
     * @param damagedEntityU The entity that you want to damage. The 'U' at the end is for field names only
     * @param attackerU The entity that is supposed to damage the damagedEntity
     */
    public void damageRealistically(Entity damagedEntityU, Entity attackerU) {
        if(!(attackerU instanceof LivingEntity)) return;
        if(!(damagedEntityU instanceof LivingEntity)) return;
        LivingEntity attacker, damagedEntity;
        attacker = (LivingEntity) attackerU; damagedEntity = (LivingEntity) damagedEntityU;
        double dmg = calculateDamage1_8(DamageCause.ENTITY_ATTACK, attacker, damagedEntity);
        double healthSave = damagedEntity.getHealth();
        damagedEntity.damage(0.5);
        damagedEntity.setHealth(healthSave - dmg);
        applyKnockback(damagedEntity, attacker);
    }

    // https://www.spigotmc.org/threads/simulating-knockback.393555/
    public void applyKnockback(LivingEntity entity, Entity attacker) {
        Random r = new Random();
        double dist = (attacker instanceof Player && ((Player)attacker).isSprinting()) ? 1.5 : 1;
        dist += r.nextDouble() * 0.4 - 0.2;
        if(attacker instanceof LivingEntity) dist += 3 * getLevel(((LivingEntity)attacker).getEquipment().getItemInHand(), Enchantment.KNOCKBACK);
        double mag = distanceToMagnitude(dist);
        Location loc = attacker.getLocation();
        if(attacker instanceof Projectile)
            loc.setDirection(attacker.getVelocity());
        entity.setVelocity(setMag(loc.getDirection(), mag));
    }

    // https://www.spigotmc.org/threads/simulating-knockback.393555/
    private double distanceToMagnitude(double distance) {
        return ((distance + 1.5) / 5d);
    }
    // https://www.spigotmc.org/threads/simulating-knockback.393555/
    private Vector setMag(Vector vector, double mag) {
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();
        double deno = Math.sqrt(x*x + y*y + z*z);
        if(deno != 0) return vector.multiply(mag / deno);
        else return vector;
    }

    private final List<String> undead = Lists.newArrayList();
    private final List<String> arthropods = Lists.newArrayList();
    {
        undead.addAll(Arrays.asList("DROWNED", "HUSK", "PHANTOM", "SKELETON", "SKELETON_HORSE", "STRAY", "WITHER", "WITHER_SKELETON", "ZOGLIN",
                "ZOMBIE", "ZOMBIE_HORSE", "ZOMBIE_VILLAGER", "ZOMBIFIED_PIGLIN"));
        arthropods.addAll(Arrays.asList("AXOLOTL", "DOLPHIN", "SQUID", "GLOWSQUID", "GLOW_SQUID", "GUARDIAN", "ELDER_GUARDIAN", "TURTLE", "COD", "SALMON", "PUFFERFISH", "TROPICAL_FISH", "PUFFER_FISH", "TROPICALFISH"
        ));
    }
    public String getShovelLevel(ItemStack spade) {
        if(!(spade.getType().name().toLowerCase().contains("spade") || spade.getType().name().toLowerCase().contains("shovel"))) return null; // Not a shovel
        return spade.getType().name().toUpperCase().replace("_SHOVEL", "").replace("_SPADE", "");
    }

    @Nullable
    public NPC getSelectedNPC(Player p) {
        if(this.getServer().getPluginManager().getPlugin("Citizens") == null) return null;
        if(!this.getServer().getPluginManager().getPlugin("Citizens").isEnabled()) return null;
        try {
            return ((Citizens)this.getServer().getPluginManager().getPlugin("Citizens")).getNPCSelector().getSelected(p);
        }catch(Exception e1) {
            return null;
        }
    }

    /**
     * Returns whether the chat is on (true) or not (false)
     */
    protected boolean chat = true;
    private static Main instance;
    public InventoryEvents getInventoryEvents() {
        return inv;
    }

    public FileConfiguration getUUIDConfig() {
        return udcfg;
    }
    public File getUUIDFile() {
        return ud;
    }
    private boolean isValidURL(String url) {
        return new UrlValidator().isValid(startHttp(url));
    }
    public void sendActionBar(Player player, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2));
    }
    public String fixEssentialsChat(Player p, String msg) {
        String fixed = new String(msg.toCharArray().clone());
        if(p.hasPermission("essentials.chat.color")) {
            fixed = fixed.replace("&0", "" + ChatColor.BLACK);
            fixed = fixed.replace("&1", "" + ChatColor.DARK_BLUE);
            fixed = fixed.replace("&2", "" + ChatColor.DARK_GREEN);
            fixed = fixed.replace("&3", "" + ChatColor.DARK_AQUA);
            fixed = fixed.replace("&4", "" + ChatColor.DARK_RED);
            fixed = fixed.replace("&5", "" + ChatColor.DARK_PURPLE);
            fixed = fixed.replace("&6", "" + ChatColor.GOLD);
            fixed = fixed.replace("&7", "" + ChatColor.GRAY);
            fixed = fixed.replace("&8", "" + ChatColor.DARK_GRAY);
            fixed = fixed.replace("&9", "" + ChatColor.BLUE);
            fixed = fixed.replace("&a", "" + ChatColor.GREEN);
            fixed = fixed.replace("&b", "" + ChatColor.AQUA);
            fixed = fixed.replace("&c", "" + ChatColor.RED);
            fixed = fixed.replace("&d", "" + ChatColor.LIGHT_PURPLE);
            fixed = fixed.replace("&e", "" + ChatColor.YELLOW);
            fixed = fixed.replace("&f", "" + ChatColor.WHITE);
        }else {
            if(p.hasPermission("essentials.chat.black")) fixed = fixed.replace("&0", "" + ChatColor.BLACK);
            if(p.hasPermission("essentials.chat.dark_blue")) fixed = fixed.replace("&1", "" + ChatColor.DARK_BLUE);
            if(p.hasPermission("essentials.chat.dark_green")) fixed = fixed.replace("&2", "" + ChatColor.DARK_GREEN);
            if(p.hasPermission("essentials.chat.dark_aqua")) fixed = fixed.replace("&3", "" + ChatColor.DARK_AQUA);
            if(p.hasPermission("essentials.chat.dark_red")) fixed = fixed.replace("&4", "" + ChatColor.DARK_RED);
            if(p.hasPermission("essentials.chat.dark_purple")) fixed = fixed.replace("&5", "" + ChatColor.DARK_PURPLE);
            if(p.hasPermission("essentials.chat.gold")) fixed = fixed.replace("&6", "" + ChatColor.GOLD);
            if(p.hasPermission("essentials.chat.gray")) fixed = fixed.replace("&7", "" + ChatColor.GRAY);
            if(p.hasPermission("essentials.chat.dark_gray")) fixed = fixed.replace("&8", "" + ChatColor.DARK_GRAY);
            if(p.hasPermission("essentials.chat.blue")) fixed = fixed.replace("&9", "" + ChatColor.BLUE);
            if(p.hasPermission("essentials.chat.green")) fixed = fixed.replace("&a", "" + ChatColor.GREEN);
            if(p.hasPermission("essentials.chat.aqua")) fixed = fixed.replace("&b", "" + ChatColor.AQUA);
            if(p.hasPermission("essentials.chat.red")) fixed = fixed.replace("&c", "" + ChatColor.RED);
            if(p.hasPermission("essentials.chat.light_purple")) fixed = fixed.replace("&d", "" + ChatColor.LIGHT_PURPLE);
            if(p.hasPermission("essentials.chat.yellow")) fixed = fixed.replace("&e", "" + ChatColor.YELLOW);
            if(p.hasPermission("essentials.chat.white")) fixed = fixed.replace("&f", "" + ChatColor.WHITE);
        }
        if(p.hasPermission("essentials.chat.magic")) fixed = fixed.replace("&k", "" + ChatColor.MAGIC);
        if(p.hasPermission("essentials.chat.bold")) fixed = fixed.replace("&l", "" + ChatColor.BOLD);
        if(p.hasPermission("essentials.chat.strikethrough")) fixed = fixed.replace("&m", "" + ChatColor.STRIKETHROUGH);
        if(p.hasPermission("essentials.chat.underline")) fixed = fixed.replace("&n", "" + ChatColor.UNDERLINE);
        if(p.hasPermission("essentials.chat.italic")) fixed = fixed.replace("&o", "" + ChatColor.ITALIC);
        if(p.hasPermission("essentials.chat.reset")) fixed = fixed.replace("&r", "" + ChatColor.RESET);

        //URL
        if(!p.hasPermission("essentials.chat.url")) {
            StringBuilder now = new StringBuilder();
            for(String s : fixed.split(" ")) {
                if(isValidURL(s)) s = s.replace(".", " ");
                now.append(s).append(" ");
            }
            now = new StringBuilder(now.substring(0, now.length() - 1));
            return now.toString();
        }
        return fixed;
    }
    public String getSend(String receiver, String msg) {
        return MSG_SEND_FORMAT.replace("%entity%", receiver).replace("%msg%", msg);
    }
    public String getReceive(String sender, String msg) {
        return MSG_RECEIVE_FORMAT.replace("%entity%", sender).replace("%msg%", msg);
    }
    public String getThirdPerson(String sender, String receiver, String msg) {
        return MSG_RECEIVE_FORMAT.replace("You", receiver).replace("%entity%", sender).replace("%msg%", msg);
    }
    public String getSimplified(Throwable t, Class<?>... getErrorSourceFrom) {
        final String[] str = {t.getClass().getName() + ": " + t.getMessage() + " || " + t.getLocalizedMessage()+"\n"};
        if(t.getStackTrace() != null) Arrays.stream(t.getStackTrace()).filter((e) -> Arrays.stream(getErrorSourceFrom).anyMatch((s) -> (e.getFileName() != null ? e.getFileName() : "12312j8sh12sh17").contains(s.getName()) || e.getFileName().contains(s.getCanonicalName()))).forEach((s) -> str[0] += "Error at Class " + s.getClassName() + " File " + s.getFileName() + ", Line " + s.getLineNumber() + " at method " + s.getMethodName() + ", ");
        return str[0];
    }
    public void sendSocialSpy(String msg) {
        for(UUID uuid : socialSpy) {
            try {
                Player p = Bukkit.getPlayer(uuid);
                if(p == null) continue; /*Player left/doesnt exist?*/
                p.sendMessage(ChatColor.GOLD + "[SOCIALSPY] " + ChatColor.RESET + msg);
            }catch(Exception ignored) {}
        }
        if(this.consoleSocialSpy)
            getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[CONSOLE-SOCIALSPY] " + ChatColor.RESET + msg);
    }
    public String getDisplayName(Player p) {
        User user = lp.getPlayerAdapter(Player.class).getUser(p);
        String prefix = user.getCachedData().getMetaData(user.getQueryOptions()).getPrefix();
        String suffix = user.getCachedData().getMetaData(user.getQueryOptions()).getSuffix();
        return ChatColor.translateAlternateColorCodes('&', (prefix == null ? "" : prefix) + p.getName() + (suffix == null ? "" : suffix));
    }
    private String getConsoleDisplayName(Player p) {
        User user = lp.getPlayerAdapter(Player.class).getUser(p);
        String prefix = user.getCachedData().getMetaData(user.getQueryOptions()).getPrefix();
        String suffix = user.getCachedData().getMetaData(user.getQueryOptions()).getSuffix();
        return (prefix == null ? "" : prefix) + p.getName() + (suffix == null ? "" : suffix);
    }

    public static Main getInstance() {
        return instance;
    }
    public void onEnable() {
        Main.instance = this;
        /* Hard dependencies */
        if(!(getServer().getPluginManager().isPluginEnabled("ProtocolLib") ||
        getServer().getPluginManager().isPluginEnabled("PacketWrapper") ||
        getServer().getPluginManager().isPluginEnabled("LuckPerms"))) {
            getLogger().severe("This plugin is disabling!");
            getServer().getPluginManager().disablePlugin(this);
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "ExtraCommands has been disabled due to: One of 3 required dependencies are not included");
            new InvalidPluginException("One of these 3 required dependencies are not included: ProtocolLib | PacketWrapper | LuckPerms.").printStackTrace();
            return;
        }
        /*                   */
        if(getDescription().getVersion().toLowerCase().contains("build")) {
            StringBuilder ver = new StringBuilder(getDescription().getVersion());
            String[] sp = ver.toString().split("-");
            ver = new StringBuilder();
            for(int i = 1; i < sp.length; i++) {
                ver.append(sp[i]).append("-");
            }
            ver = new StringBuilder(ver.substring(0, ver.length() - 1));
            getLogger().warning("This is a Dev Build. Expect to encounter bugs! Dev-build info: " +
            ver);
            if(ver.toString().contains("CE")) getLogger().info("This DEV build is for CustomEnchantments.");
        }
        getLogger().info("Loading ProtocolLib packet listeners...");
        protocolManager = ProtocolLibrary.getProtocolManager();
        loadPacketEvents();

        getLogger().info("Loading command and inventory events...");
        inv = new InventoryEvents(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Loading Luckperms...");
        lp = LuckPermsProvider.get();
        getLogger().info("Loading config...");
        loadConfiguration();
        getLogger().info("Loading commands...");
        getCommand("test").setExecutor(this);
        loadFakePlugins();
        getCommand("cnickother").setExecutor((commandSender, command, s, strings) -> false);
        getCommand("cnickother").setTabCompleter(this);
        // COMMANDS LOAD
        getCommand("broadcast").setExecutor(new BroadcastCommand());
        getCommand("console").setExecutor(new ConsoleCommand());
        getCommand("cosmetic").setExecutor(new CosmeticCommand());
        getCommand("demo").setExecutor(new DemoCommand());
        DoubleJumpCommand doubleJumpCMD = new DoubleJumpCommand();
        getCommand("doublejump").setExecutor(doubleJumpCMD);
        getCommand("ecreload").setExecutor(new ECReloadCommand(this));
        FreezeCommand freezeCMD = new FreezeCommand();
        getCommand("freeze").setExecutor(freezeCMD);
        FulldownCommand fulldownCMD = new FulldownCommand(this);
        getCommand("fulldown").setExecutor(fulldownCMD);
        getCommand("ignore").setExecutor(new IgnoreCommand(this));
        getCommand("list").setExecutor(new ListCommand(this));
        getCommand("msg").setExecutor(new MessageCommand(this));
        getCommand("msgmute").setExecutor(new MessageMuteCommand(this));
        getCommand("msgtoggle").setExecutor(new MessageToggleCommand(this));
        getCommand("nightvision").setExecutor(new NightvisionCommand());
        getCommand("ping").setExecutor(new PingCommand());
        getCommand("push").setExecutor(new PushCommand());
        getCommand("reply").setExecutor(new ReplyCommand(this));
        getCommand("socialspy").setExecutor(new SocialspyCommand(this));
        ToggleChatCommand toggleChatCMD = new ToggleChatCommand(this);
        getCommand("togglechat").setExecutor(toggleChatCMD);
        getCommand("vjump").setExecutor(new VJumpCommand());
        // COMMAND EVENTS
        Bukkit.getServer().getPluginManager().registerEvents(freezeCMD, this);
        Bukkit.getServer().getPluginManager().registerEvents(fulldownCMD, this);
        //
        // hf and ht automatically assigns themselves
        new HypixelFireball();
        new HypixelTNT();
        getLogger().info("Loading custom mobs...");
        try {
            CustomMob.registerEntities();
        }catch(Exception e1) {
            e1.printStackTrace();
            getLogger().warning("Failed to load custom mobs!");
        }
        getLogger().info("Loading repeating tasks...");
        // COMMAND REPEATING TASK
        doubleJumpCMD.runTaskTimer(this, 5L, 5L);
        freezeCMD.runTaskTimer(this, 60 * 20, 60 * 20);
        //
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            // Every 4 second, nicked entity actionbar.
            if(!Bukkit.getPluginManager().isPluginEnabled("EazyNick"))
                return;
            for(Player p : Bukkit.getOnlinePlayers()) {
                NickManager api = new NickManager(p);
                if(!api.isNicked()) return;
                String full = ChatColor.translateAlternateColorCodes('&', api.getChatPrefix() + api.getNickName() + api.getChatSuffix());
                sendActionBar(p, "&fYou are currently &4NICKED &fas &r" + full);
            }
        }, 0L, 40L);
        getLogger().info("Loading custom enchantments...");
        new EnchantLoader();
        getLogger().info("Loading citizen traits...");
        try {
            if(getServer().getPluginManager().getPlugin("Citizens") == null || !getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
                getLogger().warning("No citizens found. Aborting at enabling traits");
            }else {
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(NoKB.class).withName("NoKB"));
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PunchBot.class).withName("PunchBot"));
                getCommand("punchbot").setExecutor(new PunchBot.PunchBotCommand());
            }
        }catch(Exception noCitizens) {
            getLogger().warning("No citizens found. Aborting at enabling traits");
        }
        getLogger().info("Plugin enabled! [Private Plugin]");
    }
    private void loadPacketEvents() {
        // not used
    }
    public File getNormalConfigFile() {
        return this.normalCfgFile;
    }
    public FileConfiguration getNormalConfig() {
        return this.normalCfg;
    }
    /**
     *
     * @return The database file
     * @throws NullPointerException if the file hasn't been loaded yet.
     */
    public File getDatabaseFile() throws NullPointerException {
        return db;
    }
    /**
     *
     * @return The database configuration
     * @throws NullPointerException if the config hasn't been loaded yet.
     */
    public FileConfiguration getDatabaseConfig() throws NullPointerException {
        return database;
    }
    public void loadConfiguration() {
        if(!getDataFolder().exists()) if(!(getDataFolder().mkdirs())) getLogger().warning("Failed to create data folder for ExtraCommands!");
        File db = new File(getDataFolder().getAbsolutePath() + "/database.yml");
        File group = new File(getDataFolder().getAbsolutePath() + "/group_displayname.yml");
        saveResource("database.yml", false);
        saveResource("group_displayname.yml", false);
        saveResource("uuid_database.yml", false);
        saveResource("config.yml", false);
        this.db = db;
        this.database = new YamlConfiguration();
        this.group = group;
        this.groupConfig = new YamlConfiguration();
        this.normalCfgFile = new File(getDataFolder().getAbsolutePath() + "/config.yml");
        this.normalCfg = new YamlConfiguration();
        try {
            this.database.load(this.db);
            this.groupConfig.load(this.group);
            this.normalCfg.load(this.normalCfgFile);
        }catch(IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            getLogger().warning("Error while loading configurations!");
        }
        for(String s : getDatabaseConfig().getStringList("msgmute")) {
            messageMute.add(UUID.fromString(s));
        }
        for(String s : getDatabaseConfig().getStringList("msgtoggle")) {
            messageToggle.add(UUID.fromString(s));

        }
        this.ud = new File(getDataFolder().getAbsolutePath() + "/uuid_database.yml");
        this.udcfg = new YamlConfiguration();
        try {this.udcfg.load(this.ud);}catch (Exception ignored) {}
        ConfigurationSection ignorelist = getDatabaseConfig().getConfigurationSection("ignore-list");
        for(String ignorer : ignorelist.getKeys(false)) {
            try {
                ArrayList<UUID> ignored = new ArrayList<>();
                for(String ignoredUUID : ignorelist.getStringList(ignorer)) {
                    ignored.add(UUID.fromString(ignoredUUID));
                }
                this.ignorelist.put(UUID.fromString(ignorer), ignored);
            }catch(Exception ignored) {}
        }
        this.ignoreexempt = getDatabaseConfig().getStringList("ignore-exempt");
        this.chatFormat = ChatColor.translateAlternateColorCodes('&', getGroupConfig().getString("chat_format"));
        this.joinMsg = getNormalConfig().getString("join-msg");
        this.leaveMsg = getNormalConfig().getString("leave-msg");
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(this.leaveMsg.equalsIgnoreCase("")) return;
        try {
            String displayName = getDisplayName(e.getPlayer());
            e.setJoinMessage(ChatColor.translateAlternateColorCodes('&', this.joinMsg.replace("{entity}", displayName)));
        }catch(Exception e1) {
            e.setJoinMessage(ChatColor.translateAlternateColorCodes('&', this.joinMsg.replace("{entity}", e.getPlayer().getName())));
        }
    }
    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if(this.leaveMsg.equalsIgnoreCase("")) return;
        try {
            String displayName = getDisplayName(e.getPlayer());
            e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', this.leaveMsg.replace("{entity}", displayName)));
        }catch(Exception e1) {
            e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', this.leaveMsg.replace("{entity}", e.getPlayer().getName())));
        }
    }
    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if(this.leaveMsg.equalsIgnoreCase("")) return;
        try {
            String displayName = getDisplayName(e.getPlayer());
            e.setLeaveMessage(ChatColor.translateAlternateColorCodes('&', this.leaveMsg.replace("{entity}", displayName)));
        }catch(Exception e1) {
            e.setLeaveMessage(ChatColor.translateAlternateColorCodes('&', this.leaveMsg.replace("{entity}", e.getPlayer().getName())));
        }
    }

    public void onDisable() {
        EnchantLoader.getInstance().disable();
        getLogger().info("Disabling tasks...");
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Plugin Disabled!");
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerWorldChange(PlayerChangedWorldEvent e) {
        if(e.getPlayer().getWorld().getName().toLowerCase().contains("nether") ||
                e.getPlayer().getWorld().getName().toLowerCase().contains("end") || e.getPlayer().getWorld().getName().toLowerCase().contains("world")) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> Bukkit.dispatchCommand(getServer().getConsoleSender(), "mv tp " + e.getPlayer().getName() + " plots"), 30L);
        }
    }
    public GameMode getGamemode(String gmName) {
        return GameMode.valueOf(gmName.toUpperCase());
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGamemodeChange(PlayerGameModeChangeEvent e) {
        Player p = e.getPlayer();
        GameMode newgm = e.getNewGameMode();
        try {
            getDatabaseConfig().getConfigurationSection("gamemode").set(p.getUniqueId().toString(), newgm.toString().toLowerCase());
            getDatabaseConfig().save(getDatabaseFile());
        }catch (IOException | NullPointerException e1) {
            System.out.println("Unable to save gamemode config");
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        FileConfiguration udcfgclone = this.udcfg;
        File udclone = this.ud;
        if(e.getPlayer().getWorld().getName().toLowerCase().contains("nether") ||
                e.getPlayer().getWorld().getName().toLowerCase().contains("end") || e.getPlayer().getWorld().getName().toLowerCase().contains("world")) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> Bukkit.dispatchCommand(getServer().getConsoleSender(), "mv tp " + e.getPlayer().getName() + " plots"), 20L);
        }
        try {
            udcfgclone.set(e.getPlayer().getUniqueId().toString(), e.getPlayer().getName());
            udcfgclone.save(udclone);
        }catch(Exception e1) {
            System.out.println("Failed to set username!");
        }
        Player p = e.getPlayer();
        if(!getDatabaseConfig().getConfigurationSection("gamemode").contains(p.getUniqueId().toString())) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                p.setGameMode(getGamemode(getDatabaseConfig().getString("default-gamemode")));
                try {
                    getDatabaseConfig().getConfigurationSection("gamemode").set(p.getUniqueId().toString(), getGamemode(getDatabaseConfig().getString("default-gamemode")).toString().toLowerCase());
                    getDatabaseConfig().save(getDatabaseFile());
                }catch (IOException | NullPointerException e1) {
                    System.out.println("Unable to save gamemode config");
                }
            }, 5L);
        }else {
            GameMode change = getGamemode(getDatabaseConfig().getConfigurationSection("gamemode").getString(p.getUniqueId().toString()));
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> p.setGameMode(change), 5L);
        }
    }
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent e) {
        lastMessage.remove(e.getPlayer().getUniqueId());
    }
    private void loadFakePlugins() {
//        fakePlugins.add("EssentialsX-POSModified");
//        fakePlugins.add("CreativePlots-1.12");
//        fakePlugins.add("AdministrationTools");
//        fakePlugins.add("WorldEdit-POSM");
//        fakePlugins.add("PaperPacket");
//        fakePlugins.add("ExtraCommands");
//        fakePlugins.add("HypixelNick");
//        fakePlugins.add("MultiverseCore-POSM");
//        fakePlugins.add("PaperOnSpigot");
//        fakePlugins.add("ViaVersion-PM");
//        fakePlugins.add("LogErrorHider");
//        fakePlugins.add("ProtocolLib-POSC");
//        fakePlugins.add(ChatColor.RED + "PacketWrapper");
//        fakePlugins.add("Events-API");
    }
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String[] args = e.getMessage().split(" ");
        if(args[0].contains(":")) {
            if(!e.getPlayer().hasPermission("e.usesyntax")) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "Using commands through the plugin is disabled for security and private commands. Please use the command straight and not use ':'");
            }else e.getPlayer().sendMessage(ChatColor.GREEN + "Bypassed syntax with ':' using your permission of e.usesyntax");
        }
        Player p = e.getPlayer();
//        if(args[0].equalsIgnoreCase("/pl") || args[0].equalsIgnoreCase("/plugins") || args[0].equalsIgnoreCase("/info")) {
//            if(!p.hasPermission("e.seeplugins")) {
//                e.setCancelled(true);
//                if(args[0].equalsIgnoreCase("/info")) {
//                    p.sendMessage(PREFIX + "This command has been disabled!");
//                    return;
//                }
//                StringBuilder str = new StringBuilder(ChatColor.RED + "Plugins [" + UUID.randomUUID() + "] (" + fakePlugins.size() + "): " + ChatColor.GREEN);
//                for(int i = 0; i < fakePlugins.size(); i++) {
//                    str.append(fakePlugins.get(i));
//                    if(i != fakePlugins.size() - 1) str.append(ChatColor.WHITE).append(", ").append(ChatColor.GREEN);
//                }
//                p.sendMessage(str.toString());
//                return;
//            }else p.sendMessage(ChatColor.GREEN + "Bypassed fake plugins using your permission of 'e.seeplugins'");
//        }
//        if(args[0].equalsIgnoreCase("/cnick") || args[0].equalsIgnoreCase("/customnick")) {
//            e.setCancelled(true);
//            if(!p.hasPermission("e.cnick")) {
//                p.sendMessage(ChatColor.RED + "No permission! (e.cnick)");
//                return;
//            }
//            NickManager api = new NickManager(p);
//            api.unnickPlayer();
//            api.updatePlayer();
//            NickInfo ni = new NickInfo(this, p, null, false);
//            ni.new NickAnvilGUI(p);
//        }else if(args[0].equalsIgnoreCase("/cnickother") || args[0].equalsIgnoreCase("/customnickother") || args[0].equalsIgnoreCase("/cnickothers") || args[0].equalsIgnoreCase("/customnickothers")) {
//            e.setCancelled(true);
//            if(!p.hasPermission("e.cnickothers")) {
//                p.sendMessage(ChatColor.RED + "No permission! (e.cnickothers)");
//                return;
//            }
//            if(args.length < 2) {
//                p.sendMessage(PREFIX + "Usage: /cnickother <entity>");
//                return;
//            }
//            Player player;
//            try {
//                player = Bukkit.getPlayer(args[1]);
//            }catch(Exception e1) {
//                p.sendMessage(ChatColor.RED + "Player not found.");
//                return;
//            }
//            if(player == null) {
//                p.sendMessage(ChatColor.RED + "Player not found.");
//                return;
//            }
//            NickInfo ni = new NickInfo(this, player, p, true);
//            ni.new NickAnvilGUI(p);
//        }
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(!(sender instanceof Player)) return null;
        Player p = (Player) sender;
        if(command.getName().equalsIgnoreCase("msg") || command.getName().equalsIgnoreCase("cnickother")) {
            if(command.getName().equalsIgnoreCase("cnickother") && !p.hasPermission("e.cnickothers")) return null;
            List<String> tabComplete = new ArrayList<>();
            if(args.length == 0) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    tabComplete.add(player.getName());
                }
                return tabComplete;
            }else return null;
        }
        return null;
    }

    private net.minecraft.server.v1_8_R3.World toNMS(World world) {
        return ((CraftWorld) world).getHandle();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /test COMMAND

        if(command.getName().equalsIgnoreCase("test")) {
            if(!(sender instanceof Player)) {
                System.out.println("This command is only for players!");
                return true;
            }
            Player p = (Player) sender;
            if(!p.hasPermission("e.test")) {p.sendMessage(ChatColor.RED + "No permission. (e.test)");
            return true;}
            p.sendMessage("args: 1 <command string>\n2 [spawn custom mob]\n3 <check string>\n4 [remove all AngryVillager]\n5 [test sphere]");
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; i++) builder.append(args[i]).append(" ");
            builder = new StringBuilder(builder.substring(0,builder.length()-1));
            if(args[0].equalsIgnoreCase("1")) {
                EnchantCommand cmd = ConfigEnchantLoader.getInstance().getCommand(builder.toString());
                cmd.reset();
                cmd.input(p);
                System.out.println(Arrays.toString(cmd.neededInputs()));
                System.out.println(cmd.getClass());
                cmd.run();
                cmd.reset();
            }else if(args[0].equalsIgnoreCase("2")) {
                ((CraftWorld) p.getWorld()).getHandle().addEntity(new AngryVillager(((CraftWorld) p.getWorld()).getHandle(),p.getLocation()));
            }else if(args[0].equalsIgnoreCase("3")) {
                Check cmd = ConfigEnchantLoader.getInstance().getCheck(builder.toString());
                cmd.reset();
                cmd.input(p);
                System.out.println(Arrays.toString(cmd.neededInputs()));
                System.out.println(cmd.getClass());
                p.sendMessage(cmd.check()+"");
                cmd.reset();
            }else if(args[0].equalsIgnoreCase("4")) {
                for(Entity entity : p.getWorld().getEntities()) {
                    if(((CraftEntity) entity).getHandle() instanceof AngryVillager) {
                        entity.remove();
                    }
                }
            }else if(args[0].equalsIgnoreCase("5")) {

            }
        }else {
            System.out.println("This command only works for players! Please use the essentials version if there is!");
        }
        return true;
    }

    private String startHttp(String url) {
        if(url.toLowerCase().startsWith("http:") || url.toLowerCase().startsWith("https:") ||
                url.toLowerCase().startsWith("ftp:")) return url;
        return "http://" + url;
    }

    @EventHandler
    public void onPlayerFoodBarChange(FoodLevelChangeEvent e) {
        if(!(e.getEntity() instanceof Player)) return;
        e.setCancelled(true);
        ((Player)e.getEntity()).setFoodLevel(20);
    }
    /**
     *
     * @return The group file
     * @throws NullPointerException if the file hasn't been loaded yet.
     */
    public File getGroupFile() throws NullPointerException {
        return group;
    }
    /**
     *
     * @return The group configuration
     * @throws NullPointerException if the config hasn't been loaded yet.
     */
    public FileConfiguration getGroupConfig() throws NullPointerException {
        return groupConfig;
    }
    @EventHandler(priority = EventPriority.LOW)
    public void ignore(AsyncPlayerChatEvent e) {
        // TEMPORARY
        if(true) return; // Disable Chat Format
        if(e.isCancelled()) return;
        e.setCancelled(true);
        String consoleMessage = this.chatFormat.replace("{msg}", e.getMessage()).replace("{displayname}", this.getConsoleDisplayName(e.getPlayer()));
        String message = this.fixEssentialsChat(e.getPlayer(), this.chatFormat.replace("{msg}", e.getMessage()).replace("{displayname}", this.getDisplayName(e.getPlayer())));
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(!ignorelist.containsKey(p.getUniqueId())) {
                p.sendMessage(message);
                Bukkit.getConsoleSender().sendMessage(consoleMessage);
            }else {
                if(!ignorelist.get(p.getUniqueId()).contains(e.getPlayer().getUniqueId())) {
                    p.sendMessage(message);
                    Bukkit.getConsoleSender().sendMessage(consoleMessage);
                }
            }
        }
    }
}