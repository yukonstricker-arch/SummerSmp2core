package net.summersmp.core;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Random;

public class RespawnListener implements Listener {
    private final SummerSMPCore plugin;
    private final Random random = new Random();

    public RespawnListener(SummerSMPCore plugin) { this.plugin = plugin; }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (e.isBedSpawn() || e.isAnchorSpawn()) return;
        String wName = plugin.getConfig().getString("rtp.world", "world");
        World world = plugin.getServer().getWorld(wName);
        if (world == null) world = plugin.getServer().getWorlds().get(0);
        Location safe = findSafe(world);
        if (safe != null) e.setRespawnLocation(safe);
    }

    private Location findSafe(World w) {
        int r = plugin.getConfig().getInt("rtp.radius", 5000), min = plugin.getConfig().getInt("rtp.min-radius", 100);
        for (int i = 0; i < 24; i++) {
            int x = rc(r, min), z = rc(r, min);
            if (!w.getWorldBorder().isInside(new Location(w, x, 64, z))) continue;
            int y = w.getHighestBlockYAt(x, z); Block g = w.getBlockAt(x, y, z);
            if (safe(g) && w.getBlockAt(x, y + 1, z).getType().isAir()) return new Location(w, x + 0.5, y + 1, z + 0.5);
        }
        return null;
    }

    private int rc(int r, int min) { int s = Math.max(1, r - min); int v = min + random.nextInt(s); return random.nextBoolean() ? v : -v; }
    private boolean safe(Block b) { Material t = b.getType(); return t.isSolid() && t != Material.LAVA && t != Material.WATER && t != Material.FIRE && t != Material.MAGMA_BLOCK && t != Material.CACTUS && t != Material.POWDER_SNOW; }
}
