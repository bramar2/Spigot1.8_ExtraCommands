package me.bramar.extracommands.customenchants.checks;

import me.bramar.extracommands.customenchants.config.Check;

import java.util.Random;

public class Percentage extends Check {

    public Percentage(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "PERCENTAGE";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[0];
    }

    @Override
    public boolean check() {
        return new Random().nextDouble() * 100 <= getNumber(eachLine[1]);
    }
}
