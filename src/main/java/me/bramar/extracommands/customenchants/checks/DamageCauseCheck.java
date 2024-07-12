package me.bramar.extracommands.customenchants.checks;

import me.bramar.extracommands.customenchants.config.Check;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.List;

// DAMAGE_CAUSE:ENTITY_ATTACK
// DAMAGE_CAUSE:CONTACT:FIRE:ENTITY_ATTACK
// multiple is allowed, and if one matches, the check is passed
//
// on EntityDamageEvent
public class DamageCauseCheck extends Check {
    List<EntityDamageEvent.DamageCause> cause = new ArrayList<>();
    boolean reverse = false;

    public DamageCauseCheck(String str) {
        super(str);
        for(int i = 1; i < eachLine.length; i++) {
            String s = eachLine[i].toUpperCase();
            if(i == 1 && s.equals("!="))
                reverse = true;
            else
                try {
                    cause.add(EntityDamageEvent.DamageCause.valueOf(s));
                }catch(Exception ignored) {}
        }
    }

    @Override
    public String name() {
        return "DAMAGE_CAUSE";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[]{EntityDamageEvent.DamageCause.class};
    }

    @Override
    public boolean allowDamaged() {
        return false;
    }

    @Override
    public boolean check() {
        return reverse != cause.contains(getInput(EntityDamageEvent.DamageCause.class));
        // (reverse) ? !x : x
    }
}
