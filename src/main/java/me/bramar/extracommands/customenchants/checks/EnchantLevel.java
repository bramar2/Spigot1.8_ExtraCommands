package me.bramar.extracommands.customenchants.checks;

import me.bramar.extracommands.customenchants.config.Check;
import me.bramar.extracommands.customenchants.objects.ObjectWithType;
import me.bramar.extracommands.customenchants.objects.Operator;

// ENCHANT_LEVEL:=:3

// check enchant level
public class EnchantLevel extends Check {
    public EnchantLevel(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "LVL";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {ObjectWithType.class};
    }

    @Override
    public String[] objectWithTypes() {
        return isDamaged ? new String[] {"damaged enchant lvl"} : new String[] {"enchant lvl"};
    }

    @Override
    public boolean check() {
        return Operator.getOperator(eachLine[1]).test(getInput(Integer.class), (int) getNumber(eachLine[2]));
    }
}
