package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RtpCommand implements CommandExecutor, Listener {
    private final SummerSMPCore plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> cooldownUntil = new HashMap<>();
    private final Map<UUID, BukkitRunnable> warmups = new HashMap<>();

    public RtpCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players can use this."); return true; }
        UUID id = player.getUniqueId();
        if (plugin.getCombatManager() != null && plugin.getCombatManager().isTagged(player)) {
            player.sendMessage(Component.text("You can't random-teleport while in combat!", NamedTextColor.RED)); return true;
        }
        if (warmups.containsKey(id)) { player.sendMessage(Component.text("You're already teleporting!", NamedTextColor.RED)); return true; }
        if (!player.hasPermission("summersmp.rtp.nocooldown")) {
            long now = System.currentTimeMillis();
            Long until = cooldownUntil.get(id);
            if (until != null && until > now) {
                long secs = (until - now + 999) / 1000;
                player.sendMessage(Component.text("You can't RTP for another " + secs + " seconds.", NamedTextColor.RED)); return true;
            }
        }
        int warmupSecs = plugin.getConfig().getInt("rtp.warmup-seconds", 5);
        BukkitRunnable warmup = new BukkitRunnable() {
            int remaining = warmupSecs;
            @Override public void run() {
                if (!player.isOnline()) { cancel(); warmups.remove(id); return; }
                if (remaining <= 0) { cancel(); warmups.remove(id); doRtp(player); return; }
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

    private void doRtp(Player player) {
        String wName = plugin.getConfig().getString("rtp.world", "world");
        World world = plugin.getServer().getWorld(wName);
        if (world == null) world = plugin.getServer().getWorlds().get(0);
        int radius = plugin.getConfig().getInt("rtp.radius", 5000);
        int minRadius = plugin.getConfig().getInt("rtp.min-radius", 100);
        tryTeleport(player, world, radius, minRadius, 24);
    }

    private void tryTeleport(Player player, World world, int radius, int minRadius, int attemptsLeft) {
        if (!player.isOnline()) return;
        int x = randomCoord(radius, minRadius), z = randomCoord(radius, minRadius);
        if (!world.getWorldBorder().isInside(new Location(world, x, 64, z))) {
            if (attemptsLeft > 0) tryTeleport(player, world, radius, minRadius, attemptsLeft - 1);
            else player.sendMessage(Component.text("Couldn't find a safe spot. Try again!", NamedTextColor.RED));
            return;
        }
        world.getChunkAtAsync(x >> 4, z >> 4).thenAccept(chunk ->
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                int y = world.getHighestBlockYAt(x, z);
                Block ground = world.getBlockAt(x, y, z);
                if (isSafe(ground) && world.getBlockAt(x, y + 1, z).getType().isAir()) {
                    player.teleport(new Location(world, x + 0.5, y + 1, z + 0.5));
                    player.sendMessage(Component.text("Teleported!", NamedTextColor.GREEN));
                    if (!player.hasPermission("summersmp.rtp.nocooldown")) {
                        int cd = plugin.getConfig().getInt("rtp.cooldown-seconds", 15);
                        cooldownUntil.put(player.getUniqueId(), System.currentTimeMillis() + cd * 1000L);
                    }
                } else if (attemptsLeft > 0) tryTeleport(player, world, radius, minRadius, attemptsLeft - 1);
                else player.sendMessage(Component.text("Couldn't find a safe spot. Try again!", NamedTextColor.RED));
            }));
    }

    private int randomCoord(int r, int min) { int span = Math.max(1, r - min); int v = min + random.nextInt(span); return random.nextBoolean() ? v : -v; }
    private boolean isSafe(Block b) { Material t = b.getType(); return t.isSolid() && t != Material.LAVA && t != Material.WATER && t != Material.FIRE && t != Material.MAGMA_BLOCK && t != Material.CACTUS && t != Material.POWDER_SNOW; }
}
