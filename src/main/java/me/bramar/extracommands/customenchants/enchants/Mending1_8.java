package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Mending1_8 extends CustomEnchantment {

    public Mending1_8() {
        super(3540, EnchantmentTarget.BREAKABLE, 1, 1, "Mending1_8", "&7Mending 1.8", "&7Recreates the mending enchantment in 1.8");
    }

    @Override
    public int getMultiplier() {
        return 5;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return Collections.singletonList(EventType.XP_CHANGE);
    }
    private final Random DEFAULT = new Random();
    private final HashMap<UUID, Random> randomness = new HashMap<>();
    private Random getRandom(Player p) {
        if(p == null) return DEFAULT;
        UUID uuid = p.getUniqueId();
        try {
            Random r = randomness.get(uuid);
            if(r != null) return r;
            Random newRandom = new Random();
            randomness.put(uuid, newRandom);
            return newRandom;
        }catch(Exception ignored) {}
        return DEFAULT;
    }
    @Override
    public void onEvent(EventStore e) {
        try {
            PlayerExpChangeEvent event = e.cast();
            ItemStack[] a = currentlyEnchanted(e.getPlayer());
            for(int i = 0; i < a.length; i++) {
                try {
                    if(!(a[i].getType().getMaxDurability() > 0 && a[i].getDurability() > 0)) a[i] = null;
                    continue;
                }catch(Exception ignored) {}
                a[i] = null;
            }
            Random rnd = getRandom(e.getPlayer());
            int[] modified = new int[1000];
            int[] tempNotNull = new int[1000];
            int tempNC = 0;
            for(int i = 0; i < a.length; i++) {
                if(a[i] != null) {
                    tempNotNull[tempNC] = i;
                    tempNC++;
                }
            }
            int[] notNull = new int[tempNC + 1];
            System.arraycopy(tempNotNull, 0, notNull, 0, tempNC + 1);
            int modifiedCount = 0;
            int newAmount = event.getAmount();
            for(int i = 0; i < event.getAmount(); i++) {
                if(rnd.nextInt(10) < 9) {
                    try {
                        int n = notNull[rnd.nextInt(notNull.length)];
                        ItemStack item = a[n];
                        item.setDurability((short) Math.min(item.getDurability() - 2, 0));
                        a[n] = item;
                        modified[modifiedCount] = n;
                        modifiedCount++;
                        newAmount++;
                    }catch(Exception ignored) {}
                }
            }
            event.setAmount(newAmount);

            if(Arrays.stream(modified).anyMatch((i) -> i == 0)) e.getPlayer().getInventory().setItemInHand(a[0]);
            if(Arrays.stream(modified).anyMatch((i) -> i == 1)) e.getPlayer().getInventory().setHelmet(a[1]);
            if(Arrays.stream(modified).anyMatch((i) -> i == 2)) e.getPlayer().getInventory().setChestplate(a[2]);
            if(Arrays.stream(modified).anyMatch((i) -> i == 3)) e.getPlayer().getInventory().setLeggings(a[3]);
            if(Arrays.stream(modified).anyMatch((i) -> i == 4)) e.getPlayer().getInventory().setBoots(a[4]);
        }catch(Exception ignored) {}
    }
}
