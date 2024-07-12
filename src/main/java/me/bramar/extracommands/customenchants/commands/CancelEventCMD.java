package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.event.Cancellable;

public class CancelEventCMD extends EnchantCommand {
    public CancelEventCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "CANCEL";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Cancellable.class};
    }

    @Override
    public void run() {
        getInput(Cancellable.class).setCancelled(true);
    }
}
