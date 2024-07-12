package me.bramar.extracommands.customenchants.enchants;

import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantmentTarget;
import me.bramar.extracommands.customenchants.EventStore;
import me.bramar.extracommands.customenchants.EventType;
import me.bramar.extracommands.events.BlockDropItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Autosmelt extends CustomEnchantment {

    public Autosmelt() {
        super(3511,EnchantmentTarget.TOOLS_NO_HOE, 1, 1, "Autosmelt", "&eAutosmelt", "&eSmelts all drops from broken blocks", Enchantment.SILK_TOUCH, Enchantment.LOOT_BONUS_BLOCKS);
    }

    @Override
    public int getMultiplier() {
        return 6;
    }

    @Override
    public List<EventType> listeningEventsTo() {
        return Collections.singletonList(EventType.BLOCK_DROP_ITEM);
    }

    @Override
    public void onEvent(EventStore e) {
        try {
            BlockDropItemEvent event = e.cast();
            event.getItemList().forEach((item) -> {
                // Smelt item
                Iterator<Recipe> iter = Bukkit.recipeIterator();
                while (iter.hasNext()) {
                    Recipe recipe = iter.next();
                    if (recipe instanceof FurnaceRecipe) {
                        FurnaceRecipe fr = ((FurnaceRecipe) recipe);
                        if (fr.getInput().getType() == item.getItemStack().getType()) {
                            ItemStack clone = item.getItemStack().clone();
                            ItemStack result = fr.getResult().clone();
                            clone.setType(result.getType());
                            if(result.getAmount() <= 0) continue;
                            clone.setDurability(result.getDurability());
                            if(clone.getAmount() * result.getAmount() > 127) {
                                /* max limit of item */
                                long amount = (long) clone.getAmount() * result.getAmount();
                                clone.setAmount(127);
                                amount -= 127;
                                while(amount > 127) {
                                    ItemStack newItem = clone.clone();
                                    newItem.setAmount(127);
                                    amount -= 127;
                                    event.spawnItem(newItem);
                                }
                                if(amount > 0)
                                    clone.setAmount((int) amount); // guaranteed no numberformatexception: negative always continues, and this maxes out at 126
                            }else
                                clone.setAmount(clone.getAmount() * result.getAmount());
                            item.setItemStack(clone);
                            break;
                        }
                    }
                }
            });
        }catch(Exception ignored) {}
    }
}
