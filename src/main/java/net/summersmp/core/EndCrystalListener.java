package net.summersmp.core;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class EndCrystalListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) { if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType() == Material.END_CRYSTAL) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true)
    public void onPlace(EntityPlaceEvent e) { if (e.getEntityType() == EntityType.END_CRYSTAL) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true)
    public void onCraft(PrepareItemCraftEvent e) { Recipe r = e.getRecipe(); if (r != null && r.getResult().getType() == Material.END_CRYSTAL) e.getInventory().setResult(null); }
}
