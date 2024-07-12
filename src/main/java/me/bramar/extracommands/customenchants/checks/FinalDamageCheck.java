package me.bramar.extracommands.customenchants.checks;

import me.bramar.extracommands.customenchants.config.Check;
import me.bramar.extracommands.customenchants.objects.Operator;
import me.bramar.extracommands.customenchants.objects.EntityDamage;

public class FinalDamageCheck extends Check {

    public FinalDamageCheck(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "DAMAGE";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[]{EntityDamage.class};
    }


    @Override
    public boolean check() {
        EntityDamage ed = getInput(EntityDamage.class);
        return ed.isFinal && Operator.getOperator(eachLine[1]).test(ed.damage, getNumber(eachLine[2]));
    }
}
