package me.bramar.extracommands.customenchants.commands;

public class RepairCMD extends SetItemDamageCMD {
    // Parameter 'str' is only to make this functional (Reflection)
    public RepairCMD(String str) {
        super(str.split(":")[0] + ":HAND:=:0");
    }

    @Override
    public String name() {
        return "REPAIR";
    }

    @Override
    public boolean hasArguments() {
        return true;
    }
}
