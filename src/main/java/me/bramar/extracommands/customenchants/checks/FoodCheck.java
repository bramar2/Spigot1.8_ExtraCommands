package me.bramar.extracommands.customenchants.checks;

import me.bramar.extracommands.customenchants.config.Check;
import me.bramar.extracommands.customenchants.objects.Operator;
import org.bukkit.entity.Player;

// FOOD:>:2
public class FoodCheck extends Check {
    public FoodCheck(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "FOOD";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Player.class};
    }

    @Override
    public boolean check() {
        return Operator.getOperator(eachLine[1]).test(getInput(Player.class).getFoodLevel(), getNumber(eachLine[2]));
    }
}
