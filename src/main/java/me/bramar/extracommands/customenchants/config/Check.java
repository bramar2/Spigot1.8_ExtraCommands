package me.bramar.extracommands.customenchants.config;

import me.bramar.extracommands.customenchants.objects.ObjectWithType;
import me.bramar.extracommands.customenchants.objects.EntityDamaged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

public abstract class Check {
    protected Object[] inputted = new Object[100];
    private int inputCount=0;
    protected final String[] eachLine;
    protected final boolean isDamaged;
    private final Class<?>[] needed;
    private final Class<?>[][] alternativeNeeded;
    private final Function[] alternativeConversion;
    public Check(String str) {
        eachLine = str.split(":");
        eachLine[0] = eachLine[0].replace(" ", "");
        if(!(eachLine[0].equalsIgnoreCase(name()) || (allowDamaged() && eachLine[0].equalsIgnoreCase("DAMAGED_"+name())))) {
            throw new IllegalArgumentException("wrong id");
        }
        isDamaged = eachLine[0].equalsIgnoreCase("DAMAGED_"+name());
        this.needed = neededInputs();
        int index = needsIndex(Player.class);
        int leIndex = needsIndex(LivingEntity.class);
        this.alternativeNeeded = alternativeInput();
        this.alternativeConversion = alternativeConversion();
        if(isDamaged && leIndex != -1) {
            // Conversion (Damaged entity)
            needed[leIndex] = EntityDamaged.class;
        }else if(index != -1) {
            alternativeNeeded[index] = new Class<?>[] {
                    BlockBreakEvent.class,
                    BlockPlaceEvent.class,
                    EntityEvent.class,
                    PlayerEvent.class
            };
            alternativeConversion[index] = (t) -> {
                if(t instanceof BlockBreakEvent) return ((BlockBreakEvent) t).getPlayer();
                if(t instanceof BlockPlaceEvent) return ((BlockPlaceEvent) t).getPlayer();
                if(t instanceof EntityEvent) {
                    Entity entity = ((EntityEvent) t).getEntity();
                    if(entity instanceof Player) return entity; // Casting not needed
                }
                if(t instanceof PlayerEvent) return ((PlayerEvent) t).getPlayer();
                return t;
            };
        }
        index = needsIndex(EntityDamaged.class);
        if(index != -1) {
            alternativeNeeded[index] = new Class<?>[] {EntityDamageEvent.class};
            alternativeConversion[index] = (t) -> (t instanceof EntityDamageEvent) ? ((EntityDamageEvent) t).getEntity() :
                    null;
        }
    }
    private int needsIndex(Class<?> clazz) {
        int count = 0;
        for(Class<?> clazz2 : needed) {
            if(clazz2 == null) break;
            if(clazz2 == clazz || clazz2.isInstance(clazz)) return count;
            count++;
        }
        return -1;
    }
    public abstract String name();
    public abstract Class<?>[] neededInputs();
    public Class<?>[][] alternativeInput() {
        return new Class<?>[10][10];
    }
    public Function[] alternativeConversion() {
        return new Function[10];
    }
    public abstract boolean check();
    public boolean allowDamaged() { return true; }
    public <T> T getInput(Class<T> clazz) {
        try {
            for(Object obj : inputted) {
                if(instanceOf(obj, clazz, false)) return (T) obj;
            }
        }catch(Exception ignored) {}
        return null;
    }
    public <T> boolean instanceOf(Object obj, Class<T> clazz, boolean isClass) {
        if(clazz.isInstance(obj)) return true;
        try {
            T t = (T) obj;
            if(t != null) return true;
        }catch(Exception ignored) {}
        Class<?> last = clazz;
        int amount = 0;
        do {
            if(((isClass) ? obj : obj.getClass()) == last) return true;
            if(Arrays.asList(last.getInterfaces()).contains(clazz)) return true;
            last = last.getSuperclass();
            amount++;
            if(amount >= 100) break; // No loop
        }while(last != null && last != Object.class);
        return false;
    }
    private void input0(Object obj) {
        if(isDamaged && obj instanceof EntityDamaged) obj = ((EntityDamaged) obj).entity;
        if(obj instanceof ObjectWithType) {
            final ObjectWithType finalObj = (ObjectWithType) obj;
            if(Arrays.stream(objectWithTypes()).noneMatch((str) -> str.equalsIgnoreCase(finalObj.type))) return;
            obj = finalObj.obj;
        }
        for(int i = 0; i < Math.min(alternativeNeeded.length, alternativeConversion.length); i++) {
            try {
                Class<?>[] classes = alternativeNeeded[i];
                Function func = alternativeConversion[i];
                boolean breakOut=false;
                for(Class<?> clazz : classes) {
                    if(instanceOf(obj, clazz, false)) {
                        Object newObject = func.apply(obj);
                        if(newObject != null) {
                            obj = newObject;
                            breakOut=true;
                            break;
                        }
                    }
                }
                if(breakOut) break;
            }catch(Exception ignored) {}
        }
        inputted[inputCount] = obj;
        inputCount++;
    }
    public void input(Object... obj) {
        for(Object o : obj) input0(o);
    }
    public void input(Collection collection) {
        for(Object obj : collection) input0(obj);
    }
    protected double getNumber(String str) {
        try {
            return Double.parseDouble(str);
        }catch(Exception e1) {
            return Integer.parseInt(str);
        }
    }
    public void reset() {
        inputted = new Object[100];
        inputCount=0;
    }
    public String[] objectWithTypes() { return new String[0]; }
    public final boolean needs(Class<?> clazz) {
        if(getInput(clazz) != null) return true;
        for(Object obj : inputted) {
            if(instanceOf(obj, clazz, false)) return false; // Already inputted
        }
        for(Class<?> clazz2 : needed) {
            if(instanceOf(clazz2, clazz, true)) return true;
        }
        for(Class<?>[] classes : alternativeNeeded) {
            for(Class<?> clazz2 : classes) {
                if(instanceOf(clazz2, clazz, true)) return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String className = getClass().getName();
        return className.substring(className.lastIndexOf(".") + 1) + "{" +
                "line=" + elString() +
                ", isDamaged=" + isDamaged +
                '}';
    }
    private final String elString() {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(eachLine).forEach((s) -> builder.append(s).append(":"));
        return builder.substring(0, builder.length() - 1);
    }
}
