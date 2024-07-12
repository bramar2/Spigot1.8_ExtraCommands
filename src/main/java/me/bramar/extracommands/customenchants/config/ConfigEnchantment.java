package me.bramar.extracommands.customenchants.config;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import me.bramar.extracommands.customenchants.objects.BlockDamager;
import me.bramar.extracommands.customenchants.objects.EntityDamage;
import me.bramar.extracommands.customenchants.objects.ObjectWithType;
import me.bramar.extracommands.customenchants.objects.EntityDamaged;
import me.bramar.extracommands.events.BlockDropItemEvent;
import me.bramar.extracommands.events.PlayerLandEvent;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

import java.util.*;
import java.util.stream.Collectors;

import static me.bramar.extracommands.customenchants.EventType.*;

/**
 * Object of a CustomEnchant
 * Use {@link CustomEnchantment} for CustomEnchantment
 * This is solely for the purpose of Config Enchants
 * @see ConfigEnchantLoader
 */
public final class ConfigEnchantment extends CustomEnchantment {
    private final int multiplier;
    private final List<Ability> abilities;
    private final List<EventType> listening;
    private boolean listeningTicks=false;
    private int ticks=-1;
    // Non-instantiable outside package
    ConfigEnchantment(int id, EnchantmentTarget target, int maxLevel, int startLevel,
                      String name, String displayName, String description,
                      int multiplier, List<Ability> abilities, Enchantment[] conflicts) {
        super(id,target,maxLevel,startLevel,name,displayName,description,conflicts, true);

        this.multiplier=multiplier;
        this.abilities = abilities;

        Ability tick = this.abilities.stream().filter((a) -> a != null && a.getEvent().equalsIgnoreCase("tick"))
                .findFirst().orElse(null);
        if(tick != null) {
            this.listeningTicks=true;
            this.ticks=tick.getTicks();
        }
        //
        listening = this.abilities.stream().map(
                ability -> {
                    try {
                        return EventType.valueOf(ability.getEvent().toUpperCase());
                    }catch(Exception ignored) {}
                    return null;
                }
        ).filter(Objects::nonNull).collect(Collectors.toList());
        if(!listening.contains(EventType.TICK) && listeningTicks) listening.add(EventType.TICK);
        // Redo abilities
        if(amountOfTicks() >= 1) {
            Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(usingEnchantment(player)) {
                        repeatingTask(player);
                    }
                }
            }, amountOfTicks(), amountOfTicks());
        }
        //
    }

    @Override
    public int getMultiplier() {
        return multiplier;
    }

    @Override
    public void repeatingTask(Player player) {
        if(!listeningTicks) return;
        int additive = getEnchantLevel(player, true);
        int nonAdditive = getEnchantLevel(player, false);
        for(Ability ability : abilities) {
            ObjectWithType<Integer> lvl = new ObjectWithType<>("enchant lvl", (ability.isAdditive()) ? additive : nonAdditive);
            boolean a = ability.testLevel(lvl.obj);
            boolean b = !ability.isOnCooldown(player.getUniqueId());
            if(a && b) {
                boolean breakOut = false;
                for(Check c : ability.getChecks()) {
                    c.reset();
                    c.input(player, lvl);
                    if(!c.check()) breakOut = true;
                    c.reset();
                    if(breakOut) break;
                }
                if(breakOut) continue;
                for(EnchantCommand c : ability.getCommands()) {
                    c.reset();
                    c.input(player, lvl);
                    c.run();
                    c.reset();
                }
                if(ability.hasCooldown()) ability.setOnCooldown(player.getUniqueId());
            }
        }
    }

    @Override
    public int amountOfTicks() {
        return ticks;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return listening;
    }

    @Override
    public void onEvent(EventStore e) {
        if(e.getType() == TICK) return;
        int additive;
        try {
            additive = getEnchantLevel(e.getPlayer(), true);
        }catch(Exception e1) {
            additive = 0;
        }
        int nonAdditive;
        try {
            nonAdditive = getEnchantLevel(e.getPlayer(), false);
        }catch(Exception e1) {
            nonAdditive = 0;
        }
        UUID uuid;
        try {
            uuid = e.getPlayer().getUniqueId();
        }catch(Exception e1) {
            uuid = null;
        }
        List<Object> info = new ArrayList<>();
        addInformation(info, e);
        for(Ability ability : abilities) {
            if(!ability.getEvent().equalsIgnoreCase(e.getType().name()) ||
            !ability.testLevel(ability.isAdditive() ? additive : nonAdditive) ||
            ability.isOnCooldown(uuid)) continue;
            // If the event is not the correct one,
            // and/or the ability is on cooldown,
            // and/or the level does not match, continue


            boolean correct = true;
            for(Check c : ability.getChecks()) {
                c.reset();
                c.input(info);
                boolean o = !c.check();
                c.reset();
                if(o) {
                    correct = false;
                    break;
                }
            }
            if(!correct) continue;
            for(EnchantCommand c : ability.getCommands()) {
                c.reset();
                c.input(info);
                try {
                    c.run();
                }catch(Exception e1) {e1.printStackTrace();}
                c.reset();
            }
            if(ability.hasCooldown()) ability.setOnCooldown(uuid);
        }
    }
    private void addInformation(List<Object> info, EventStore e) {
        info.add(e.getUncastedEvent());
        boolean isDamaged = false;
        Player otherDamaged = null;
        Player otherDamager = null;
        if(e.getType() == DAMAGE_BY_BLOCK) {
            isDamaged = true;
            EntityDamageByBlockEvent event = e.cast();
            info.add(new EntityDamaged(e.getPlayer()));
            info.add(new BlockDamager(event.getDamager()));
            info.add(event.getCause());
            info.add(new EntityDamage(event.getDamage(), false));
            info.add(new EntityDamage(event.getFinalDamage(), true));
        }else if(e.getType() == DAMAGE_BY_ENTITY || e.getType() == DAMAGED_BY_PROJECTILE) {
            isDamaged = true;
            EntityDamageByEntityEvent event = e.cast();
            info.add(new EntityDamaged(e.getPlayer()));
            info.add(new EntityDamage(event.getDamage(), false));
            info.add(new EntityDamage(event.getFinalDamage(), true));
            info.add(event.getCause());
        }else if(e.getType() == DAMAGED) {
            isDamaged = true;
            EntityDamageEvent event = e.cast();
            info.add(event.getCause());
            info.add(e.getPlayer());
            info.add(new EntityDamage(event.getDamage(), false));
            info.add(new EntityDamage(event.getFinalDamage(), true));
        }else if(e.getType() == BLOCK_BREAK) {
            BlockBreakEvent event = e.cast();
            info.add(event.getPlayer());
            info.add(event.getExpToDrop());
            info.add(event.getBlock());
        }else if(e.getType() == BLOCK_PLACE) {
            BlockPlaceEvent event = e.cast();
            info.add(event.getPlayer());
            info.add(new ObjectWithType<>("placed_block_against", event.getBlockAgainst()));
            info.add(new ObjectWithType<>("placed_block", event.getBlock()));
            info.add(event.getBlockReplacedState());
        }else if(e.getType() == BLOCK_DROP_EXP) {
            BlockExpEvent event = e.cast();
            info.add(event.getExpToDrop());
            info.add(event.getBlock());
        }else if(e.getType() == BLOCK_DROP_ITEM) {
            BlockDropItemEvent event = e.cast();
            info.add(event.getPlayer());
            info.add(event.getBlock());
            info.add(event.getBlockType());
            info.add(event.getItemList());
        }else if(e.getType() == BREAK_ITEM) {
            PlayerItemBreakEvent event = e.cast();
            info.add(event.getPlayer());
            info.add(event.getBrokenItem());
        }else if(e.getType() == CONSUME_ITEM) {
            PlayerItemConsumeEvent event = e.cast();
            info.add(event.getPlayer());
            info.add(event.getItem());
        }else if(e.getType() == LAND) {
            PlayerLandEvent event = e.cast();
            info.add(event.getPlayer());
            info.add(event.getVelocity());
        }else if(e.getType() == INTERACT_AT_ENTITY) {
            PlayerInteractAtEntityEvent event = e.cast();
            info.add(event.getPlayer());
            info.add(event.getClickedPosition());
            info.add(event.getRightClicked());
        }else if(e.getType() == INTERACT_ENTITY) {
            PlayerInteractEntityEvent event = e.cast();
            info.add(event.getPlayer());
            info.add(new ObjectWithType<>("interact_entity", event.getRightClicked()));
        }else if(e.getType() == INTERACT) {
            PlayerInteractEvent event = e.cast();
            info.add(event.getPlayer());
            info.add(event.getBlockFace());
            if(event.getClickedBlock() != null) info.add(event.getClickedBlock());
            info.add(event.getAction());
            info.add(event.getItem());
        }else if(e.getType() == XP_CHANGE) {
            PlayerExpChangeEvent event = e.cast();
            info.add(event.getPlayer());
            info.add(event.getAmount());
        }else if(e.getType() == PLAYER_DAMAGE_ENTITY) {
            EntityDamageByEntityEvent event = e.cast();
            info.add(e.getPlayer());
            if(event.getEntity() instanceof Player) info.add(new EntityDamaged((Player) event.getEntity()));
            info.add(new EntityDamage(event.getDamage(), false));
            info.add(new EntityDamage(event.getFinalDamage(), true));
            info.add(event.getCause());
        }
        if(e.getPlayer() != null) {
            EntityDamageByEntityEvent event = (e.getUncastedEvent() instanceof EntityDamageByEntityEvent) ? e.cast() : null;
            if(isDamaged) {
                otherDamaged = e.getPlayer();
                if(event != null && event.getDamager() instanceof Player) otherDamager = (Player) event.getDamager();
            }else {
                otherDamager = e.getPlayer();
                if(event != null && event.getEntity() instanceof Player) otherDamaged = (Player) event.getEntity();
            }
            if(otherDamaged != null) info.add(new ObjectWithType<>("damaged enchant lvl", getEnchantLevel(otherDamaged, true)));
            if(otherDamager != null) info.add(new ObjectWithType<>("enchant lvl", getEnchantLevel(otherDamager, true)));
        }
    }

    @Override
    public String toString() {
        String str = super.toString();
        return str.substring(0, str.length() - 1) +
                ", multiplier=" + multiplier +
                ", abilities=" + abilities +
                ", listening=" + listening +
                ", listeningTicks=" + listeningTicks +
                ", ticks=" + ticks +
                '}';
    }
}
