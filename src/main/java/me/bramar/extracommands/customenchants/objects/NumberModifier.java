package me.bramar.extracommands.customenchants.objects;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

public enum NumberModifier {
    ADD("+", Double::sum),
    SUBTRACT("-", (a1, a2) -> a1 - a2),
    DIVIDE("/", (a1, a2) -> a1 / a2),
    MULTIPLY("*", (a1, a2) -> a1 * a2),
    RANK("^", Math::pow),
    EQUALS("=", (a1, a2) -> a2),
    MAX("max", (a1, a2) -> a1 > a2 ? a1 : a2),
    MIN("min", (a1, a2) -> a1 > a2 ? a2 : a1),
    FLOOR("f", (a1, a2) -> Math.floor(a1)),
    CEILING("c", (a1, a2) -> Math.ceil(a1)),
    ROUND("o", (a1, a2) -> (double) Math.round(a1)),
    DECIMALS(".", (obj1, obj2) -> {
        StringBuilder dfStr = new StringBuilder("#.");
        int round = (int) Math.round(obj2);
        for(int i = 0; i < round; i++)
            dfStr.append("#");
        DecimalFormat df = new DecimalFormat(dfStr.toString());
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(obj1));
    });
    private final TwoArgs<Double,Double,Double> t;
    private final String symbol;
    NumberModifier(String symbol, TwoArgs<Double,Double,Double> t) {
        this.t = t;
        this.symbol = symbol;
    }
    public static NumberModifier getModifier(String str) {
        try {
            return Objects.requireNonNull(valueOf(str.toUpperCase()));
        }catch(Exception ignored) {}
        for(NumberModifier m : values()) {
            if(m.symbol.equalsIgnoreCase(str)) return m;
        }
        return null;
    }
    public static NumberModifier getModifier(Object obj) {
        NumberModifier o = getModifier(obj+"");
        if(o != null) return o;
        for(NumberModifier m : values()) {
            if(obj == m) return m;
        }
        return null;
    }
    public double test(double args1, double args2) {
        return t.apply(args1, args2);
    }

    @FunctionalInterface
    private interface TwoArgs<A,B,C> {
        C apply(A a1, B b1);
    }
}
