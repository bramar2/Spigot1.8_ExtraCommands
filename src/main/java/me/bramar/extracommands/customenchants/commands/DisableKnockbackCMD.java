package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.EnchantLoader;
import me.bramar.extracommands.customenchants.config.ConfigEnchantLoader;
import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class DisableKnockbackCMD extends EnchantCommand {
    public DisableKnockbackCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "DISABLE_KNOCKBACK";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }

    @Override
    public void run() {
        UUID uuid = getInput(LivingEntity.class).getUniqueId();
        ConfigEnchantLoader.getInstance().listener.noKnockback.remove(uuid);
        ConfigEnchantLoader.getInstance().listener.noKnockback.put(uuid,
                Main.getInstance().immutableEntry(EnchantLoader.getInstance().getCurrentTick(),
                        (int) getNumber(eachLine[1])));
    }
}
