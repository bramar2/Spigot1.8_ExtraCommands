package me.bramar.extracommands.customenchants.checks;

import me.bramar.extracommands.customenchants.config.Check;
import me.bramar.extracommands.customenchants.objects.Operator;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LocationCheck extends Check {

    public LocationCheck(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "LOCATION";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Player.class};
    }

    @Override
    public boolean check() {
        Operator o = Operator.getOperator(eachLine[1]);
        Location loc = getInput(Player.class).getLocation().clone();
        // ~ system
        double x, y, z;
        if(eachLine[2].startsWith("~")) {
            if(!eachLine[2].substring(1).isEmpty()) x = loc.getX() + getNumber(eachLine[2].substring(1));
            else x = 0;
        }
        else x = getNumber(eachLine[2]);
        if(eachLine[3].startsWith("~")) {
            if(!eachLine[3].substring(1).isEmpty()) y = loc.getY() + getNumber(eachLine[3].substring(1));
            else y = 0;
        }
        else y = getNumber(eachLine[3]);
        if(eachLine[4].startsWith("~")) {
            if(!eachLine[4].substring(1).isEmpty()) z = loc.getZ() + getNumber(eachLine[4].substring(1));
            else z = 0;
        }
        else z = getNumber(eachLine[4]);
        return o.test(loc.getX(), x) &&
                o.test(loc.getY(), y) &&
                o.test(loc.getZ(), z);
    }
}
