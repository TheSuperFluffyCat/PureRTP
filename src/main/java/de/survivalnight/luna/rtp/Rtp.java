package de.survivalnight.luna.rtp;

import de.survivalnight.luna.rtp.command.RtpCommand;
import de.survivalnight.luna.rtp.gui.RtpGui;
import org.bukkit.plugin.java.JavaPlugin;

public final class Rtp extends JavaPlugin {

    private static Rtp instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        getCommand("rtp").setExecutor(new RtpCommand());
        getServer().getPluginManager().registerEvents(new RtpGui(), this);
    }

    @Override
    public void onDisable() {
    }

    public static Rtp getInstance() {
        return instance;
    }
}
