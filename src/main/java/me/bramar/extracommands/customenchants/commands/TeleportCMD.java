package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class TeleportCMD extends EnchantCommand {
    public TeleportCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "TELEPORT";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {LivingEntity.class};
    }
    @Override
    public void run() {
        LivingEntity p = getInput(LivingEntity.class);
        Location loc = p.getLocation().clone();
        double x, y, z;
        if(eachLine[2].startsWith("~")) x = loc.getX() + getNumber(eachLine[2].substring(1));
        else x = getNumber(eachLine[2]);
        if(eachLine[3].startsWith("~")) y = loc.getY() + getNumber(eachLine[3].substring(1));
        else y = getNumber(eachLine[3]);
        if(eachLine[4].startsWith("~")) z = loc.getZ() + getNumber(eachLine[4].substring(1));
        else z = getNumber(eachLine[4]);
        p.teleport(new Location(p.getWorld(),x,y,z,loc.getYaw(),loc.getPitch()));
    }
}
