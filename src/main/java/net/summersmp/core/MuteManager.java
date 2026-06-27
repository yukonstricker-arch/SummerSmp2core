package net.summersmp.core;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MuteManager implements Listener {
    private final SummerSMPCore plugin;
    private final Map<UUID, Long> mutedUntil = new ConcurrentHashMap<>();

    public MuteManager(SummerSMPCore plugin) { this.plugin = plugin; }

    public void mute(UUID id, long durationMillis) {
        mutedUntil.put(id, System.currentTimeMillis() + durationMillis);
    }

    public void unmute(UUID id) { mutedUntil.remove(id); }

    public boolean isMuted(UUID id) {
        Long until = mutedUntil.get(id);
        if (until == null) return false;
        if (until <= System.currentTimeMillis()) { mutedUntil.remove(id); return false; }
        return true;
    }

    public long remainingSeconds(UUID id) {
        Long until = mutedUntil.get(id);
        if (until == null) return 0;
        long ms = until - System.currentTimeMillis();
        return ms <= 0 ? 0 : (ms + 999) / 1000;
    }

    public String remainingFormatted(UUID id) {
        long secs = remainingSeconds(id);
        if (secs <= 0) return "0 seconds";
        long h = secs / 3600; long m = (secs % 3600) / 60; long s = secs % 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        if (!isMuted(id)) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text(
                "You are muted. You cannot type or speak for another " + remainingFormatted(id) + ".",
                NamedTextColor.RED));
    }
}
