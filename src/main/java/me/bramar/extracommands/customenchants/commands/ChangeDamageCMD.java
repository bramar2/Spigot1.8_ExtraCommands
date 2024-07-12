package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import me.bramar.extracommands.customenchants.objects.NumberModifier;
import org.bukkit.event.entity.EntityDamageEvent;

public class ChangeDamageCMD extends EnchantCommand {
    public ChangeDamageCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "CHANGE_DAMAGE";
    }

    @Override
    public boolean allowDamaged() {
        return false;
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {EntityDamageEvent.class};
    }

    @Override
    public void run() {
        EntityDamageEvent event = getInput(EntityDamageEvent.class);
        double newDamage = NumberModifier.getModifier(eachLine[1]).test(event.getDamage(), getNumber(eachLine[2]));
        event.setDamage(newDamage);
    }
}
