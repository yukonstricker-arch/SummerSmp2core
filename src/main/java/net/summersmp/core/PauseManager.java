package net.summersmp.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PauseManager implements Listener {
    private final SummerSMPCore plugin;
    private boolean paused = false;

    public PauseManager(SummerSMPCore plugin) { this.plugin = plugin; }
    public boolean isPaused() { return paused; }
    public void setPaused(boolean v) { paused = v; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
        if (!paused) return;
        if (event.getPlayer().hasPermission("summersmp.pause")) return;
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Server is currently under maintenance. Please try again later.");
    }
}
