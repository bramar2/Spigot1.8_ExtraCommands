package me.bramar.extracommands.customenchants.checks;

import me.bramar.extracommands.customenchants.config.Check;
import me.bramar.extracommands.customenchants.objects.Operator;
import org.bukkit.entity.Player;

public class ExpCheck extends Check {

    public ExpCheck(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "EXP";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[]{Player.class};
    }

    @Override
    public boolean check() {
        return Operator.getOperator(eachLine[1]).test(getInput(Player.class).getExp(), getNumber(eachLine[2]));
    }
}
