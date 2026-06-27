package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.*;

public class MovementListener implements Listener {
    private final boolean disableElytra;
    private final Set<Material> blocked = new HashSet<>();

    public MovementListener(SummerSMPCore plugin) {
        this.disableElytra = plugin.getConfig().getBoolean("disable-elytra", true);
        for (String name : plugin.getConfig().getStringList("disabled-movement-items")) { Material m = Material.matchMaterial(name.toUpperCase(Locale.ROOT)); if (m != null) blocked.add(m); }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent e) { if (disableElytra && e.getEntity() instanceof Player && e.isGliding()) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) { if (blocked.contains(e.getItem().getType())) { e.setCancelled(true); e.getPlayer().sendMessage(Component.text("That item is disabled on this server.", NamedTextColor.RED)); } }
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) { if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && blocked.contains(e.getItem().getType())) { e.setCancelled(true); e.getPlayer().sendMessage(Component.text("That item is disabled on this server.", NamedTextColor.RED)); } }
}
