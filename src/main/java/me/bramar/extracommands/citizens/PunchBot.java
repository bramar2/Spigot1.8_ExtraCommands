package me.bramar.extracommands.citizens;

import java.util.List;

import net.citizensnpcs.api.event.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.bramar.extracommands.Main;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;

public class PunchBot extends Trait {
    @Persist(value = "reach")
    double reach = 5;
    @Persist(value = "attackmobs")
    boolean attackMobs = false;
    @Persist(value = "speed")
    int speedModifier = 1;
    @Persist(value = "health")
    double health = 20;
    @Persist(value = "strength")
    int strength = 0;
    @Persist(value = "antikb")
    boolean antikb = false;
    @Persist(value = "hptoheal")
    int hptoheal = 2;
    @Persist(value = "healdelay")
    int healdelay = 2;
    @Persist(value = "infinitehealth")
    boolean infinitehealth = false;
    @Persist(value = "jump")
    int jump = 0;
    @Persist(value = "aggresive")
    boolean aggresive = false;
    @Persist(value = "spawnpoint")
    Location spawnpoint = null;
    @Persist(value = "teleport_stuck")
    boolean teleportStuck = false;

    private boolean theFirstTime = true;
    private boolean isSpawned = false;

    //	Location oldLocation;
    public PunchBot() {
        super("PunchBot");
        if(npc != null) if(npc.isSpawned()) this.onSpawn();
    }
    private int healTick = 0;
    private int infhealthTick = 0;
    // Modify the damage given from the NPC, because no matter what item the NPC is holding. The damage is the same
    // as a fist.
    @EventHandler
    public void onNPCAttack(NPCDamageEntityEvent e) {
        if(e.getNPC() != this.getNPC()) return;
        if(!(npc.getEntity() instanceof LivingEntity)) return;
        LivingEntity living = (LivingEntity)npc.getEntity();
        double damage = Main.getInstance().calculateDamage1_8(e.getCause(), living, (LivingEntity)e.getDamaged());
        damage += (3 * strength);
        e.setDamage((int) Math.round(damage));
    }
    public void antiKB(NPC npc) {
        Vector lastTickSave = lastTick == null ? (npc != null ? (npc.isSpawned() ? npc.getEntity().getVelocity() : new Vector(0, 0, 0)) : new Vector(0, 0, 0)) : lastTick;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            if(npc != null) if(npc.isSpawned()) npc.getEntity().setVelocity(lastTickSave);
        }, 2L);
    }

    @EventHandler
    public void npcDamage1(NPCDamageByEntityEvent e) {
        if(e.getNPC() != this.npc) return;
        if(antikb) antiKB(e.getNPC());
        if(e.getNPC().isSpawned()) {
            Entity entity = e.getDamager();
            if(entity instanceof Player) {
                if(((Player)entity).getGameMode() == GameMode.CREATIVE || ((Player)entity).getGameMode() == GameMode.SPECTATOR) return;
                e.getNPC().getNavigator().setTarget(entity, true);
            }else if(attackMobs && entity instanceof LivingEntity) {
                e.getNPC().getNavigator().setTarget(entity, true);
            }
        }
        if(infinitehealth) e.setDamage(0);
    }

    @EventHandler
    public void npcDespawn(NPCDespawnEvent e) {
        isSpawned = false;
    }
    @EventHandler
    public void npcDamage2(NPCDamageByBlockEvent e) {
        if(e.getNPC() != this.npc) return;
        if(antikb) antiKB(e.getNPC());
        if(infinitehealth) e.setDamage(0);
    }
    @EventHandler
    public void npcDamage3(NPCDamageEvent e) {
        if(e.getNPC() != this.npc) return;
        if(antikb) antiKB(e.getNPC());
        if(infinitehealth) e.setDamage(0);
    }

    private Vector lastTick;
    @Override
    public void onSpawn() {
        if(spawnpoint != null && !isSpawned) npc.getEntity().teleport(spawnpoint);
        isSpawned = true;
        if(npc.getEntity() instanceof LivingEntity) ((LivingEntity) npc.getEntity()).resetMaxHealth();
        if(npc.getEntity() instanceof LivingEntity) ((LivingEntity) npc.getEntity()).setMaxHealth(health > 2048 ? 2048 : (health < 1 ? 1 : health));
        if(npc.getEntity() instanceof LivingEntity) ((LivingEntity) npc.getEntity()).setHealth(health > 2048 ? 2048 : (health < 1 ? 1 : health));
        npc.getNavigator().getLocalParameters().attackRange(reach).attackDelayTicks(2);
        npc.getNavigator().getLocalParameters().speedModifier(speedModifier);
        if(npc != null) if(npc.isSpawned()) lastTick = npc.getEntity().getVelocity();
        if(teleportStuck) npc.getNavigator().getLocalParameters().stuckAction(TeleportStuckAction.INSTANCE);
        else // Empty stuckaction (does nothing)
            npc.getNavigator().getLocalParameters().stuckAction((arg0, arg1) -> false);
    }
    @Override
    public void load(DataKey key) {
        reach = key.getDouble("reach", 5);
        attackMobs = key.getBoolean("attackmobs", false);
        speedModifier = key.getInt("speed", 1);
        health = key.getDouble("health", 20);
        strength = key.getInt("strength", 0);
        antikb = key.getBoolean("antikb", false);
        hptoheal = key.getInt("hptoheal", 2);
        healdelay = key.getInt("healdelay", 2);
        infinitehealth = key.getBoolean("infinitehealth", false);
        jump = key.getInt("jump", 0);
        aggresive = key.getBoolean("aggresive", true);
        try {
            spawnpoint = (Location) key.getRaw("spawnpoint");
        }catch(Exception e1) {
            spawnpoint = null; // null means it spawns in the original position.
        }
        teleportStuck = key.getBoolean("teleport_stuck", false);
    }
    public net.minecraft.server.v1_8_R3.Entity getNMS() {
        if(!npc.isSpawned()) return null;
        return ((CraftEntity)npc.getEntity()).getHandle();
    }

    @Override
    public void save(DataKey key) {
        key.setDouble("reach", reach);
        key.setBoolean("attackmobs", attackMobs);
        key.setInt("speed", speedModifier);
        key.setDouble("health", health);
        key.setBoolean("antikb", antikb);
        key.setInt("hptoheal", hptoheal);
        key.setInt("healdelay", healdelay);
        key.setBoolean("infinitehealth", infinitehealth);
        key.setInt("jump", jump);
        key.setBoolean("aggresive", aggresive);
        key.setRaw("spawnpoint", spawnpoint);
        key.setBoolean("teleport_stuck", teleportStuck);
    }
    @Override
    public void run() {
        if(theFirstTime && this.getNPC().isSpawned()) {
            theFirstTime = false;
            this.onSpawn();
        }
        if(npc.getNavigator().getEntityTarget() != null) {
            if(npc.getNavigator().getEntityTarget() instanceof Player) {
                Player p = (Player) npc.getNavigator().getEntityTarget();
                if(npc.isSpawned()) npc.getNavigator().setTarget(npc.getEntity().getLocation());
                if(p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) npc.getNavigator().setTarget(null, true);
            }
        }
        if(npc.isSpawned()) {
            healTick();
            infiniteHealth();
            jump();
            aggresive();
        }
    }
    public void healTick() {
        int healdelaytick = this.healdelay * 20;
        healTick++;
        if(healTick >= healdelaytick) {
            healTick = 0;
            if(npc.getEntity() instanceof LivingEntity) {
                LivingEntity entity = (LivingEntity) npc.getEntity();
                entity.setHealth(Math.min((entity.getHealth() + hptoheal), entity.getMaxHealth()));
            }
        }
    }
    public void infiniteHealth() {
        if(infinitehealth) {
            infhealthTick++;
            if(infhealthTick > 60) {
                infhealthTick = 0;
                if(npc.getEntity() instanceof LivingEntity) ((LivingEntity)npc.getEntity()).setHealth(((LivingEntity)npc.getEntity()).getMaxHealth());
            }
        }else if(infhealthTick != 0) infhealthTick = 0;
    }
    public void jump() {
        if(npc.getEntity() instanceof LivingEntity && jump != 0) {
            ((LivingEntity)npc.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10, jump, false, false), true);
        }
    }
    public void aggresive() {
        if(!aggresive) return;
        if(!(npc.getEntity() instanceof LivingEntity)) return;
        if(npc.getNavigator().getEntityTarget() != null) return;
        List<Entity> list = this.getNPC().getEntity().getNearbyEntities(30, 20, 30);
        for(Entity entity : list) {
            if((attackMobs && entity instanceof LivingEntity) || entity instanceof Player) {
                if(entity instanceof Player) {
                    if(((Player)entity).getGameMode() == GameMode.CREATIVE || ((Player)entity).getGameMode() == GameMode.SPECTATOR) continue;
                }
                npc.getNavigator().setTarget(entity, true);
                break;
            }
        }
    }
    public static class PunchBotCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if(sender instanceof Player) {
                Player p = (Player) sender;
                if(!p.hasPermission("punchbot.*")) {
                    p.sendMessage(ChatColor.RED + "No permission! (punchbot.*)");
                    return true;
                }
                if(args.length == 0) {
                    p.sendMessage(ChatColor.RED + "Type /punchbot help for more commands.");
                    return true;
                }
                if(args[0].equalsIgnoreCase("help")) {
                    p.sendMessage(ChatColor.YELLOW + "------------------- Punchbot Plugin Commands -----------------\n"
                            + "Every single command here is accessed by one permission: punchbot.*\n"
                            + "To equip the NPC with armor, use citizen's command: /npc equip\n"
                            + "To make the NPC respawn, use citizen's default respawn system: /npc respawn <delay>\n"
                            + "- /punchbot reach <double>: Sets the reach of the NPC. Default: 5 (e.g /punchbot reach 5 | /punchbot reach 3.2)\n"
                            + "- /punchbot attackmobs: Toggles whether the NPC attack mobs/living entities. Default: false\n"
                            + "- /punchbot speed <modifier>: Sets speed modifier. Default: 1\n"
                            + "- /punchbot health <1 - 100000>: Sets NPC health. Default: 20\n"
                            + "- /punchbot strength <level>: Sets extra strength damage. Extra (3 HP x level) damage, Default: 0\n"
                            + "- /punchbot antikb: Toggles whether the NPC takes KB or not. Default: false\n"
                            + "- /punchbot heal <hearts_to_heal> <heal_delay>: Makes the NPC heals {hearts_to_heal} HP every {heal_delay} seconds. Default: 1 heart [2 HP] every 2 sec\n"
                            + "- /punchbot infinitehealth: Sets the damage to 0 every time the NPC gets damaged!\n"
                            + "- /punchbot nofall: Toggles whether the NPC takes fall damage.\n"
                            + "- /punchbot jump <jump_boost_amplifier>: Sets the jump boost amplifier extra height. Uses the normal jump boost amplifier effect [1 - 255]. Default: 0\n"
                            + "- /punchbot aggresive: Toggles whether the NPC kills players if they fought first or not. Default: true\n"
                            + "- /punchbot spawnpoint [unset]: Sets the NPC spawnpoint. The NPC will be teleported to the spawnpoint after spawning (this includes /npc move IF the /npc respawn value is -1). Use the optional 'unset' to remove the spawnpoint location.\n"
                            + "- /punchbot teleportstuck: Toggles whether the NPC teleports to the Target (the entity/mob the npc is currently fighting) if the NPC is stuck. Defualt: false");
                }else if(args[0].equalsIgnoreCase("reach")) {
                    try {
                        if(args.length == 1) {
                            p.sendMessage("Usage: /punchbot reach <double>: Sets the reach it can PVP.");
                            return true;
                        }
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        double reach = Double.parseDouble(args[1]);
                        npc.getTrait(PunchBot.class).reach = reach;
                        if(npc.isSpawned()) npc.getTrait(PunchBot.class).onSpawn();
                        p.sendMessage(ChatColor.GREEN + "Successfully set reach to " + ChatColor.DARK_GREEN + reach);
                    }catch(NumberFormatException e1) {
                        p.sendMessage(ChatColor.RED + "An error occured. Most likely because you didn't enter a correct double value.");
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else if(args[0].equalsIgnoreCase("attackmobs")) {
                    try {
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        npc.getTrait(PunchBot.class).attackMobs = !npc.getTrait(PunchBot.class).attackMobs;
                        if(npc.isSpawned()) npc.getTrait(PunchBot.class).onSpawn();
                        if(npc.getTrait(PunchBot.class).attackMobs) p.sendMessage(ChatColor.GREEN + "The NPC now attack mobs.");
                        else p.sendMessage(ChatColor.GREEN + "The NPC now no longer attack mobs.");
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else if(args[0].equalsIgnoreCase("speed")) {
                    try {
                        if(args.length == 1) {
                            p.sendMessage("Usage: /punchbot speed <modifier>: Sets speed modifier.");
                            return true;
                        }
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        int speed = Integer.parseInt(args[1]);
                        npc.getTrait(PunchBot.class).speedModifier = speed;
                        if(npc.isSpawned()) npc.getTrait(PunchBot.class).onSpawn();
                        p.sendMessage(ChatColor.GREEN + "Successfully set speed to " + ChatColor.DARK_GREEN + speed);
                    }catch(NumberFormatException e1) {
                        p.sendMessage(ChatColor.RED + "An error occured. Most likely because you didn't enter a correct modifier value.");
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else if(args[0].equalsIgnoreCase("health")) {
                    try {
                        if(args.length == 1) {
                            p.sendMessage("Usage: /punchbot health <1 - 100000>: Sets NPC health.");
                            return true;
                        }
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        int health = Integer.parseInt(args[1]);
                        npc.getTrait(PunchBot.class).health = health;
                        if(npc.isSpawned()) npc.getTrait(PunchBot.class).onSpawn();
                        p.sendMessage(ChatColor.GREEN + "Successfully set health to " + ChatColor.DARK_GREEN + health);
                    }catch(NumberFormatException e1) {
                        p.sendMessage(ChatColor.RED + "An error occured. Most likely because you didn't enter a correct double value.");
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else if(args[0].equalsIgnoreCase("strength")) {
                    try {
                        if(args.length == 1) {
                            p.sendMessage("Usage: /punchbot strength <level>: Sets extra strength damage. Extra (3 HP x level) damage, Default: 0");
                            return true;
                        }
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        int strength = Integer.parseInt(args[1]);
                        npc.getTrait(PunchBot.class).strength = strength;
                        p.sendMessage(ChatColor.GREEN + "Successfully set strength to " + ChatColor.DARK_GREEN + strength);
                    }catch(NumberFormatException e1) {
                        p.sendMessage(ChatColor.RED + "An error occured. Most likely because you didn't enter a correct modifier value.");
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else if(args[0].equalsIgnoreCase("antikb")) {
                    try {
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        PunchBot trait = npc.getTrait(PunchBot.class);
                        trait.antikb = !trait.antikb;
                        if(trait.antikb) p.sendMessage(ChatColor.GREEN + "Successfully enabled antikb for the NPC!");
                        else p.sendMessage(ChatColor.GREEN + "Successfully disabled antikb for the NPC!");
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else if(args[0].equalsIgnoreCase("heal")) {
                    try {
                        if(args.length < 3) {
                            p.sendMessage("Usage: /punchbot heal <hearts_to_heal> <heal_delay>: Makes the NPC heals {hearts_to_heal} HP every {heal_delay} seconds. Default: 1 heart [2 HP] every 2 sec");
                            return true;
                        }
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        PunchBot trait = npc.getTrait(PunchBot.class);
                        trait.hptoheal = Integer.parseInt(args[1]);
                        trait.healdelay = Integer.parseInt(args[2]);
                        trait.healTick = 0;
                        try {
                            p.sendMessage(ChatColor.GREEN + "Successfully set heal to " + ChatColor.BOLD + trait.hptoheal + ChatColor.GREEN + " HP [" + (trait.hptoheal / 2.0D) + " hearts] every " + ChatColor.BOLD + trait.healdelay + ChatColor.GREEN + " second" + (trait.healdelay < 2 ? "" : "s") + "!");
                        }catch(Exception e1) {
                            p.sendMessage(ChatColor.GREEN + "Successfully set heal to " + ChatColor.BOLD + trait.hptoheal + ChatColor.GREEN + " HP every " + ChatColor.BOLD + trait.healdelay + ChatColor.GREEN + " second" + (trait.healdelay < 2 ? "" : "s") + "!");
                        }
                    }catch(NumberFormatException e1) {
                        p.sendMessage(ChatColor.RED + "An error occured. Most likely because you didn't enter a correct integer value.");
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else if(args[0].equalsIgnoreCase("infinitehealth")) {
                    try {
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        PunchBot trait = npc.getTrait(PunchBot.class);
                        trait.infinitehealth = !trait.infinitehealth;
                        if(trait.infinitehealth) p.sendMessage(ChatColor.GREEN + "Successfully made the NPC invincible/has infinite health!");
                        else p.sendMessage(ChatColor.GREEN + "Successfully made the NPC have finite health!");
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else if(args[0].equalsIgnoreCase("jump")) {
                    try {
                        if(args.length == 1) {
                            p.sendMessage("Usage: /punchbot jump <jump_boost_amplifier>: Sets the jump boost amplifier extra height. Uses the normal jump boost amplifier effect [1 - 255]. Default: 0");
                            return true;
                        }
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        int jump = Integer.parseInt(args[1]);
                        if(jump < 0 || jump > 255) throw new IllegalArgumentException("Only allows integer from 0 - 255");
                        npc.getTrait(PunchBot.class).jump = jump;
                        p.sendMessage(ChatColor.GREEN + "Successfully set jump boost level to " + ChatColor.DARK_GREEN + jump);
                    }catch(NumberFormatException e1) {
                        p.sendMessage(ChatColor.RED + "An error occured. Most likely because you didn't enter a correct modifier value.");
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else if(args[0].equalsIgnoreCase("aggresive")) {
                    try {
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        PunchBot trait = npc.getTrait(PunchBot.class);
                        trait.aggresive = !trait.aggresive;
                        if(trait.aggresive) p.sendMessage(ChatColor.GREEN + "The NPC is now aggresive!");
                        else p.sendMessage(ChatColor.GREEN + "The NPC is now neutral!");
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else if(args[0].equalsIgnoreCase("spawnpoint")) {
                    try {
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        PunchBot trait = npc.getTrait(PunchBot.class);
                        if(args.length >= 2) if(args[1].equalsIgnoreCase("unset")) {
                            trait.spawnpoint = null;
                            p.sendMessage(ChatColor.GREEN + "The NPC now spawns at its current location or at /npc move. (unset)");
                            return true;
                        }
                        trait.spawnpoint = p.getLocation();
                        p.sendMessage(ChatColor.GREEN + "The NPC now spawns at Location X=" + trait.spawnpoint.getBlockX() + " Y=" + trait.spawnpoint.getBlockY() + " Z=" + trait.spawnpoint.getBlockZ());
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else if(args[0].equalsIgnoreCase("teleportstuck")) {
                    try {
                        NPC npc = Main.getInstance().getSelectedNPC(p);
                        if(npc == null) {
                            p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                        }else {
                            if(!npc.hasTrait(PunchBot.class)) {
                                p.sendMessage(ChatColor.RED + "You are currently not selecting an NPC");
                                return true;
                            }
                            if(!(npc.getTrait(Owner.class).getOwnerId() == p.getUniqueId())) {
                                if(!p.hasPermission("citizens.admin")) {
                                    p.sendMessage(ChatColor.RED + "You must be the owner of the NPC to modify it.");
                                    return true;
                                }
                            }
                        }
                        PunchBot trait = npc.getTrait(PunchBot.class);
                        trait.teleportStuck = !trait.teleportStuck;
                        if(trait.teleportStuck) p.sendMessage(ChatColor.GREEN + "Successfully enabled teleport stuck for the NPC!");
                        else p.sendMessage(ChatColor.GREEN + "Successfully disabled teleport stuck for the NPC!");
                        if(trait.teleportStuck) trait.npc.getNavigator().getLocalParameters().stuckAction(TeleportStuckAction.INSTANCE);
                        else // Empty stuckaction (Does nothing)
                            trait.npc.getNavigator().getLocalParameters().stuckAction((arg0, arg1) -> false);
                    }catch(Exception e1) {
                        p.sendMessage(ChatColor.DARK_RED + "ERROR: " + e1.getMessage());
                    }
                }else p.sendMessage(ChatColor.RED + "Invalid argument. Type /punchbot help for more commands.");
            }else System.out.println("This command can only be executed by a entity!");
            return true;
        }
    }
}
