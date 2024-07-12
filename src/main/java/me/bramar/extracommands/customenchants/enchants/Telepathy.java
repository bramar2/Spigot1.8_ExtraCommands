package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import me.bramar.extracommands.events.BlockDropItemEvent;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

public class Telepathy extends CustomEnchantment {
    public Telepathy() {
        super(3541, EnchantmentTarget.TOOLS, 1, 1, "Telepathy", "&cTelepathy", "&cTeleports dropped items to entity and makes the entity instantly picks it up");
    }

    @Override
    public int getMultiplier() {
        return 5;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return Collections.singletonList(EventType.BLOCK_DROP_ITEM);
    }

    @Override
    public void onEvent(EventStore e) {
        try {
            BlockDropItemEvent event = e.cast();
            event.runWhenFinished((item) -> {
                item.setPickupDelay(3);
                item.teleport(e.getPlayer().getLocation());
                item.setVelocity(new Vector(0d,0.1,0d));
            });
        }catch(Exception ignored) {}
    }
}
