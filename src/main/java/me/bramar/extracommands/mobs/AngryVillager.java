package me.bramar.extracommands.mobs;

import me.bramar.extracommands.Main;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class AngryVillager extends EntityVillager {
    public AngryVillager(World world, Location loc) {
        super(world);
        List gB = (List) getPrivateField("b", PathfinderGoalSelector.class, goalSelector);
        List tC = (List) getPrivateField("c", PathfinderGoalSelector.class, targetSelector);
        List gC = (List) getPrivateField("c", PathfinderGoalSelector.class, goalSelector);
        List tB = (List) getPrivateField("b", PathfinderGoalSelector.class, targetSelector);
        gB.clear();
        gC.clear();
        tB.clear();
        tC.clear();
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1d, false));
        this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
        this.goalSelector.a(3, new PathfinderGoal() {
            private final Random r = new Random();
            @Override
            public boolean a() {
                return getGoalTarget() != null;
            }
            @Override
            public boolean b() {
                try {
                    LivingEntity entity = (LivingEntity) getGoalTarget().getBukkitEntity();
                    if(a() && entity != null && r.nextDouble() <= 0.075) { // This should mathematically run every 1.1111~ minute
                        getWorld().worldData.setWeatherDuration(200); // 10 seconds
                        LightningStrike lightningStrike = entity.getWorld().strikeLightningEffect(entity.getLocation());
                        entity.damage(5d, lightningStrike);
                    }
                }catch(Exception ignored) {}
                return true;
            }
        });
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        setCustomNameVisible(true);
        setCustomName(new ChatComponentText("Angry Villager").setChatModifier(new ChatModifier().setColor(EnumChatFormat.RED)).getText());
    }
    @Override
    protected void getRareDrop() {
        // Nether star drop
        this.a(Items.NETHER_STAR, 1);
    }


    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        boolean o = super.damageEntity(damagesource, f);
        setHealth(512f);
        try {
            if(damagesource.getEntity() instanceof LivingEntity) {
                setInvisible(true);
                Location loc = damagesource.getEntity().getBukkitEntity().getLocation();
                loc.getWorld().playSound(loc, Sound.ENDERDRAGON_GROWL, 1f,1f);
                schedule(() -> setInvisible(false), 200);
                loc.getWorld().strikeLightning(loc);
                schedule(() -> loc.getWorld().strikeLightning(loc), 5);
                schedule(() -> ((LivingEntity) damagesource.getEntity().getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 1, true, true)), 5);
                schedule(() -> loc.getWorld().strikeLightning(loc), 10);
                schedule(() -> ((LivingEntity) damagesource.getEntity().getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 1, true, true)), 5);
                schedule(() -> loc.getWorld().strikeLightning(loc), 15);
                schedule(() -> ((LivingEntity) damagesource.getEntity().getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 40, 0, true, true)), 5);
            }
        }catch(Exception ignored) {}
        return o;
    }
    private void schedule(Runnable run, long delay) {
        Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), run, delay);
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        AttributeMapBase a = this.getAttributeMap();
        tryCatch(NullPointerException.class, () -> this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(35.0d), (err) -> {
            this.getAttributeMap().b(GenericAttributes.FOLLOW_RANGE); // Register as an attribute
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(35.0d);
        });
        tryCatch(NullPointerException.class, () -> this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.8d), (err) -> {
            this.getAttributeMap().b(GenericAttributes.MOVEMENT_SPEED); // Register as an attribute
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.8d);
        });
        tryCatch(NullPointerException.class, () -> this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(7d), (err) -> {
            this.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE); // Register as an attribute
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(7d);
        });
        tryCatch(NullPointerException.class, () -> this.getAttributeInstance(GenericAttributes.maxHealth).setValue(512), (err) -> {
            this.getAttributeMap().b(GenericAttributes.maxHealth); // Register as an attribute
            this.getAttributeInstance(GenericAttributes.maxHealth).setValue(512);
        });
        setHealth(512);
    }
    private <T> void tryCatch(Class<T> clazz, Runnable code, Consumer<T> consumer) {
        try {
            code.run();
        }catch(Throwable t) {
            if(clazz.isInstance(t)) consumer.accept((T) t);
            else {
                try {
                    T casted = (T) t;
                    consumer.accept(casted);
                }catch(Exception ignored) {}
            }
        }
    }
    public static Object getPrivateField(String fieldName, Class clazz, Object object)
    {
        Field field;
        Object o = null;

        try
        {
            field = clazz.getDeclaredField(fieldName);

            field.setAccessible(true);

            o = field.get(object);
        }
        catch(NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return o;
    }
}
