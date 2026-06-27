package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
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

public class WorldCommand implements CommandExecutor, Listener {
    private final SummerSMPCore plugin;
    private final Map<UUID, BukkitRunnable> warmups = new HashMap<>();

    public WorldCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players can use this."); return true; }
        if (plugin.getCombatManager() != null && plugin.getCombatManager().isTagged(player)) {
            player.sendMessage(Component.text("You can't teleport while in combat!", NamedTextColor.RED)); return true;
        }
        if (warmups.containsKey(player.getUniqueId())) { player.sendMessage(Component.text("You're already teleporting!", NamedTextColor.RED)); return true; }

        String wName = plugin.getConfig().getString("rtp.world", "world");
        World world = plugin.getServer().getWorld(wName);
        if (world == null) world = plugin.getServer().getWorlds().get(0);
        final World target = world;
        UUID id = player.getUniqueId();
        BukkitRunnable warmup = new BukkitRunnable() {
            int remaining = 5;
            @Override public void run() {
                if (!player.isOnline()) { cancel(); warmups.remove(id); return; }
                if (remaining <= 0) {
                    cancel(); warmups.remove(id);
                    int y = target.getHighestBlockYAt(0, 0);
                    player.teleport(new Location(target, 0.5, y + 1, 0.5));
                    player.sendMessage(Component.text("Teleported to the center of the world (0, 0).", NamedTextColor.GREEN));
                    return;
                }
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
