package me.bramar.extracommands.customenchants.checks;

import me.bramar.extracommands.customenchants.config.Check;
import me.bramar.extracommands.customenchants.objects.EntityDamaged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// ENTITY_TYPE:*
// ENTITY_TYPE:PIG
// ENTITY_TYPE:!=:PIG
public class EntityTypeCheck extends Check {
    List<EntityType> types = new ArrayList<>();
    boolean reverse = false;

    public EntityTypeCheck(String str) {
        super(str);
        boolean all = false;
        for(int i = 1; i < eachLine.length; i++) {
            String s = eachLine[i];
            if(s.equalsIgnoreCase("*") || s.equalsIgnoreCase("all")) {
                all = true;
                break;
            }
            if(i == 1 && s.equals("!="))
                reverse = true;
            else
                try {
                    types.add(EntityType.valueOf(s.toUpperCase()));
                }catch(Exception ignored) {}
        }
        if(all) {
            types.clear();
            Collections.addAll(types, EntityType.values());
        }
    }

    @Override
    public String name() {
        return "ENTITY_TYPE";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {EntityDamaged.class};
    }

    @Override
    public boolean allowDamaged() {
        return false;
    }

    @Override
    public boolean check() {
        try {
            return types.contains(getInput(EntityDamaged.class).entity.getType());
        }catch(Exception ignored) {}
        return types.contains(getInput(Entity.class).getType());
    }
}
