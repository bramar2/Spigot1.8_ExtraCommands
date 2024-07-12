package me.bramar.extracommands.mobs;

import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.EntityVillager;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum CustomMob {
    ANGRY_VILLAGE("AngryVillager",
            78,
            EntityType.VILLAGER,
            EntityVillager.class,
            AngryVillager.class);
    private String name;
    private int id;
    private EntityType type;
    private Class<?> nmsClass;
    private Class<?> customClass;
    CustomMob(String name, int id, EntityType type, Class<?> nmsClass, Class<?> customClass) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.nmsClass = nmsClass;
        this.customClass = customClass;
    }
    public static void registerEntities() { for (CustomMob ce : values()) ce.register(); }

    private void register() {
        try {

            List<Map<?, ?>> dataMap = new ArrayList<>();
            for (Field f : EntityTypes.class.getDeclaredFields()){
                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())){
                    f.setAccessible(true);
                    dataMap.add((Map<?, ?>) f.get(null));
                }
            }

            if (dataMap.get(2).containsKey(id)){
                dataMap.get(0).remove(name);
                dataMap.get(2).remove(id);
            }

            Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(null, customClass, name, id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

    public EntityType getEntityType() {
        return type;
    }

    public Class<?> getCustomClass() {
        return customClass;
    }

    private static Object getPrivateStatic(final Class<?> clazz, final String f) {
        try {
            Field field = clazz.getDeclaredField(f);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
