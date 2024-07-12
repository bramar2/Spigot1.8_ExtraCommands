package me.bramar.extracommands.customenchants.commands;

import me.bramar.extracommands.customenchants.config.EnchantCommand;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlaySoundCMD extends EnchantCommand {
    public PlaySoundCMD(String str) {
        super(str);
    }

    @Override
    public String name() {
        return "PLAY_SOUND";
    }

    @Override
    public Class<?>[] neededInputs() {
        return new Class[] {Player.class};
    }
    @Override
    public void run() {
        Player p = getInput(Player.class);
        Sound sound = Sound.valueOf(eachLine[1].toUpperCase());
        float volume = (float) getNumber(eachLine[2]);
        float pitch = (float) getNumber(eachLine[3]);
        p.playSound(p.getLocation(), sound, volume, pitch);
    }
}
