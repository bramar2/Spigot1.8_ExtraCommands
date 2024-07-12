package me.bramar.extracommands.events;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantLoader;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PreRegisteringEnchant extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final EnchantLoader loader;
    private static boolean ran = false;
    public PreRegisteringEnchant(EnchantLoader loader) {
        if(ran) throw new UnsupportedOperationException("this event is one-time use, and initialization is not allowed more than once");
        this.loader = loader;
        ran = true;
    }
    public void registerEnchantment(CustomEnchantment ench) {
        loader.registerEnchant(ench);
    }
    public void registerEnchantment(Iterable<? extends CustomEnchantment> ench) {
        for(CustomEnchantment en : ench)
            registerEnchantment(en);
    }
    public static HandlerList getHandlerList() { return handlers; }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
