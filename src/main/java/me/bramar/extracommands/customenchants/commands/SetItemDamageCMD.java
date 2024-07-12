package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import me.bramar.extracommands.customenchants.objects.NumberModifier;
import me.bramar.extracommands.customenchants.objects.Slots;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

public class SetItemDamageCMD extends EnchantCommand {
    public SetItemDamageCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "SET_ITEM_DMG";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }

    private static boolean isUnbreakable(ItemStack i) {
        try {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
            if(nmsStack.getTag().getBoolean("Unbreakable")) return true;
            if(nmsStack.getTag().getByte("Unbreakable") >= 1) return true;
        }catch(Exception ignored) {}
        return false;
    }
    @Override
    public void run() {
        LivingEntity p = getInput(LivingEntity.class);
        Slots s = Slots.getSlot(eachLine[1]);
        NumberModifier n = NumberModifier.getModifier(eachLine[2]);
        short durability = (short) getNumber(eachLine[3]);
        try {
            ItemStack i = s.get(p);
            assert !isUnbreakable(i);
            short newDurability = (short) n.test(i.getDurability(), durability);
            // Bukkit API
            if(newDurability > i.getDurability() && p instanceof Player) { // It damages not repairs
                Player player = (Player) p;
                int damage = newDurability - i.getDurability();
                PlayerItemDamageEvent e1 = new PlayerItemDamageEvent(player, i, damage);
                Bukkit.getPluginManager().callEvent(e1);
                if(e1.isCancelled()) return;
                if(newDurability >= i.getType().getMaxDurability()) {
                    i.setDurability(newDurability);
                    PlayerItemBreakEvent e2 = new PlayerItemBreakEvent(player,i);
                    Bukkit.getPluginManager().callEvent(e2);
                    player.getInventory().clear(s.getSlot(player));
                    player.playSound(p.getLocation(), Sound.ITEM_BREAK, 1f, 1f);
                    return;
                }
            }
            //
            i.setDurability(newDurability);
        }catch(Exception ignored) {}
    }
}
