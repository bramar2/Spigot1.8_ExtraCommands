package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.EnchantLoader;
import me.bramar.extracommands.customenchants.config.ConfigEnchantLoader;
import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.entity.LivingEntity;

public class InvincibleCMD extends EnchantCommand {
    public InvincibleCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "INVINCIBLE";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }

    @Override
    public void run() {
        ConfigEnchantLoader.getInstance().listener.invincible.put(getInput(LivingEntity.class).getUniqueId(),
                Main.getInstance().immutableEntry(EnchantLoader.getInstance().getCurrentTick(),
                        (int) getNumber(eachLine[1])));
    }
}
