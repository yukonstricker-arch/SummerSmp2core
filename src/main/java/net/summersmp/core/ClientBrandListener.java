package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class ClientBrandListener implements Listener {
    private final SummerSMPCore plugin;
    private final Map<UUID, Integer> detections = new HashMap<>();

    public ClientBrandListener(SummerSMPCore plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.getConfig().getBoolean("hacked-client-detection.enabled", true)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> check(e.getPlayer()), 60L);
    }

    private void check(Player p) {
        if (!p.isOnline()) return;
        String brand = p.getClientBrandName();
        if (brand == null) return;
        String lower = brand.toLowerCase(Locale.ROOT);
        boolean hit = false;
        for (String f : plugin.getConfig().getStringList("hacked-client-detection.flagged-brands"))
            if (!f.isEmpty() && lower.contains(f.toLowerCase(Locale.ROOT))) { hit = true; break; }
        if (!hit) return;
        p.sendMessage(Component.text("Hacked clients are NOT allowed on Summer SMP 2.", NamedTextColor.RED, TextDecoration.BOLD));
        p.sendMessage(Component.text("Please remove them before joining again.", NamedTextColor.RED));
        Bukkit.getOnlinePlayers().stream().filter(s -> s.hasPermission("summersmp.ban"))
            .forEach(s -> s.sendMessage(Component.text("[Alert] " + p.getName() + " joined with flagged client: " + brand, NamedTextColor.YELLOW)));
        int count = detections.merge(p.getUniqueId(), 1, Integer::sum);
        if (plugin.getConfig().getBoolean("hacked-client-detection.ban-on-repeat", true) && count >= 2) {
            String dur = plugin.getConfig().getString("hacked-client-detection.ban-duration", "7d");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + p.getName() + " " + dur + " Hacked client detected");
        }
    }
}
