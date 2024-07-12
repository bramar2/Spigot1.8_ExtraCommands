package me.bramar.extracommands.customenchants.config;

import me.bramar.extracommands.customenchants.checks.EnchantLevel;
import me.bramar.extracommands.customenchants.objects.ObjectWithType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Ability {
    private final List<Check> checks;
    private final List<EnchantCommand> commands;
    private final Map<UUID, Long> cooldown = new HashMap<>();
    private final String event;
    private int ticks;
    private int cooldownTime;
    private final List<EnchantLevel> lvl;
    private final boolean additive;
    public Ability(List<Check> checks, List<EnchantCommand> commands, String events, List<EnchantLevel> lvl, boolean additive) {
        this.lvl = lvl;
        this.checks = checks;
        this.commands = commands;
        this.event = events;
        this.additive = additive;
    }

    public boolean isAdditive() {
        return additive;
    }

    public boolean testLevel(int lvl) {
        ObjectWithType<Integer> owt = new ObjectWithType<>("enchant lvl", lvl);
        for(EnchantLevel l : this.lvl) {
            l.reset();
            l.input(owt);
            if(!l.check()) return false;
            l.reset();
        }
        return true;
    }

    public int getCooldownTime() {
        return cooldownTime;
    }

    public void setCooldownTime(int cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public boolean hasCooldown() {
        try {
            return cooldownTime > 0;
        }catch(Exception ignored) {}
        return false;
    }
    private void updateCooldown() {
        new HashMap<>(cooldown).forEach((uuid, time) -> {
            if(Math.floor(((double) System.currentTimeMillis() - (double) time) / 20d) >= cooldownTime) cooldown.remove(uuid);
        });
    }
    public boolean isOnCooldown(UUID uuid) {
        if(uuid == null) return false;
        updateCooldown();
        return hasCooldown() && cooldown.containsKey(uuid);
    }
    public void setOnCooldown(UUID uuid) {
        if(!hasCooldown() || isOnCooldown(uuid)) return;
        updateCooldown();
        cooldown.put(uuid, System.currentTimeMillis());
    }
    public void setTicks(int ticks) {
        this.ticks = ticks;
    }
    public boolean hasTicks() {
        try {
            return ticks > 0;
        }catch(Exception ignored) {}
        return false;
    }
    public int getTicks() {
        return ticks;
    }

    public List<Check> getChecks() {
        return checks;
    }

    public List<EnchantCommand> getCommands() {
        return commands;
    }

    public String getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "Ability{" +
                "checks=" + checks +
                ", commands=" + commands +
                ", event='" + event + '\'' +
                ", ticks=" + ticks +
                ", cooldownTime=" + cooldownTime +
                ", lvl=" + lvl +
                ", additive=" + additive +
                '}';
    }
}
