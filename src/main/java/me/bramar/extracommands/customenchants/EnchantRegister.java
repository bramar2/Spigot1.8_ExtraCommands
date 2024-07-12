package me.bramar.extracommands.customenchants;

import me.bramar.extracommands.customenchants.enchants.*;
import me.bramar.extracommands.events.PreRegisteringEnchant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;

public class EnchantRegister implements Listener {
    @EventHandler
    public void onRegister(PreRegisteringEnchant e) {
        e.registerEnchantment(Arrays.asList(
                new Abiding(),
                new Aegis(),
                new Allure(),
                new Ambit(),
                new Angelic(),
                new Aqua(),
                new Aquatic(),
                new Archer(),
                new ArrowBreak(),
                new ArrowDeflect(),
                new Autosmelt(),
                new Bait(),
                new Barbarian(),
                new Beastslayer(),
                new Berserk(),
                new Blacksmith(),
                new Bleed(),
                new Blind(),
                new BlockEnchantment(),
                new BoneCrusher(),
                new Bowmaster(),
                new CarrotPlanter(),
                new Chaos(),
                new Chunky(),
                new Cleave(),
                new Confuse(),
                new Convulse(),
                new CreeperArmor(),
                new Critical(),
                new Curse(),
                new Deadshot(),
                new DeathPunch(),
                new Decapitation(),
                new Deranged(),
                new Devour(),
                new Diminish(),
                new Glowing(),
                new Telepathy(),
                new Veinminer(),
                new Drill(),
                new Explode(),
                new WholeChunk(),
                new Mending1_8()
        ));
    }
}
