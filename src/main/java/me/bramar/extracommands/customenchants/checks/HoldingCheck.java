package me.bramar.extracommands.customenchants.checks;

import me.bramar.extracommands.customenchants.config.Check;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class HoldingCheck extends Check {
    List<Material> mats = new ArrayList<>();
    boolean reverse = false;

    public HoldingCheck(String str) {
        super(str);
        for(int i = 1; i < eachLine.length; i++) {
            String s = eachLine[i].toUpperCase();
            if(i == 1 && s.equals("!="))
                reverse = true;
            else
                try {
                    mats.add(Material.valueOf(s));
                }catch(Exception ignored) {}
        }
    }

    @Override
    public String name() {
        return "HOLDING";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }

    @Override
    public boolean check() {
        return reverse != mats.contains(getInput(LivingEntity.class).getEquipment().getItemInHand().getType());
        // (reverse) ? !x : x
    }
}
