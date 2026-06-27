package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CombatManager extends BukkitRunnable implements Listener {
    private final SummerSMPCore plugin;
    private final int tagSeconds;
    private final Set<String> blockedCommands = new HashSet<>();
    private final Map<UUID, Long> combatUntil = new HashMap<>();

    public CombatManager(SummerSMPCore plugin) {
        this.plugin = plugin;
        this.tagSeconds = plugin.getConfig().getInt("combat.tag-seconds", 20);
        for (String c : plugin.getConfig().getStringList("combat.blocked-commands"))
            blockedCommands.add(c.toLowerCase(Locale.ROOT).replace("/", ""));
    }

    public boolean isTagged(Player p) { Long u = combatUntil.get(p.getUniqueId()); return u != null && u > System.currentTimeMillis(); }
    public int secondsLeft(Player p) { Long u = combatUntil.get(p.getUniqueId()); if (u == null) return 0; long ms = u - System.currentTimeMillis(); return ms <= 0 ? 0 : (int) Math.ceil(ms / 1000.0); }

    private void tag(Player p) {
        boolean was = isTagged(p);
        combatUntil.put(p.getUniqueId(), System.currentTimeMillis() + tagSeconds * 1000L);
        if (!was) p.sendMessage(Component.text("You are now in combat! Don't log out for " + tagSeconds + " seconds.", NamedTextColor.RED));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        Player attacker = null;
        if (e.getDamager() instanceof Player p) attacker = p;
        else if (e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) attacker = p;
        if (attacker == null || attacker.equals(victim)) return;
        tag(victim); tag(attacker);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (isTagged(e.getPlayer())) {
            combatUntil.remove(e.getPlayer().getUniqueId());
            e.getPlayer().setHealth(0.0);
            Bukkit.broadcast(Component.text(e.getPlayer().getName() + " logged out in combat and died!", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) { combatUntil.remove(e.getEntity().getUniqueId()); }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!isTagged(e.getPlayer())) return;
        String raw = e.getMessage().startsWith("/") ? e.getMessage().substring(1) : e.getMessage();
        String label = raw.split(" ")[0].toLowerCase(Locale.ROOT);
        if (label.contains(":")) label = label.substring(label.indexOf(':') + 1);
        if (blockedCommands.contains(label)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Component.text("You can't use that command while in combat! (" + secondsLeft(e.getPlayer()) + "s left)", NamedTextColor.RED));
        }
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        combatUntil.entrySet().removeIf(en -> en.getValue() <= now);
        for (Player p : Bukkit.getOnlinePlayers()) {
            int left = secondsLeft(p);
            if (left > 0) p.sendActionBar(Component.text("Combat: " + left + "s", NamedTextColor.RED));
        }
    }
}
