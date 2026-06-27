package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class SpawnCommand implements CommandExecutor, Listener {
    private final SummerSMPCore plugin;
    private final Map<UUID, BukkitRunnable> warmups = new HashMap<>();

    public SpawnCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players can use this."); return true; }
        if (plugin.getCombatManager() != null && plugin.getCombatManager().isTagged(player)) {
            player.sendMessage(Component.text("You can't go to spawn while in combat!", NamedTextColor.RED)); return true;
        }
        if (warmups.containsKey(player.getUniqueId())) { player.sendMessage(Component.text("You're already teleporting!", NamedTextColor.RED)); return true; }

        String worldName = plugin.getConfig().getString("spawn-world", "hub");
        World hub = plugin.getServer().getWorld(worldName);
        if (hub == null) {
            for (World w : plugin.getServer().getWorlds()) { if (w.getName().equalsIgnoreCase(worldName)) { hub = w; break; } }
        }
        if (hub == null) {
            String loaded = plugin.getServer().getWorlds().stream().map(World::getName).collect(Collectors.joining(", "));
            player.sendMessage(Component.text("Spawn world '" + worldName + "' isn't loaded.", NamedTextColor.RED));
            player.sendMessage(Component.text("Loaded worlds: " + loaded, NamedTextColor.GRAY));
            return true;
        }
        final World target = hub;
        UUID id = player.getUniqueId();
        BukkitRunnable warmup = new BukkitRunnable() {
            int remaining = 5;
            @Override public void run() {
                if (!player.isOnline()) { cancel(); warmups.remove(id); return; }
                if (remaining <= 0) { cancel(); warmups.remove(id); player.teleport(target.getSpawnLocation()); player.sendMessage(Component.text("Welcome back to spawn!", NamedTextColor.GREEN)); return; }
                player.sendActionBar(Component.text("Teleporting in " + remaining + "s...", NamedTextColor.YELLOW));
                remaining--;
            }
        };
        warmups.put(id, warmup);
        warmup.runTaskTimer(plugin, 0L, 20L);
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        BukkitRunnable w = warmups.remove(p.getUniqueId());
        if (w != null) { w.cancel(); p.sendActionBar(Component.text("")); p.sendMessage(Component.text("Teleport cancelled — you took damage.", NamedTextColor.RED)); }
    }
}
