package net.summersmp.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class MaceLimitListener implements Listener {
    private final SummerSMPCore plugin;
    public MaceLimitListener(SummerSMPCore plugin) { this.plugin = plugin; }

    private boolean isCounted(ItemStack s) { if (s == null) return false; ItemMeta m = s.getItemMeta(); return m != null && m.getPersistentDataContainer().has(plugin.countedKey, PersistentDataType.BYTE); }
    private ItemStack tagCounted(ItemStack s) { if (s == null) return null; ItemMeta m = s.getItemMeta(); if (m != null) { m.getPersistentDataContainer().set(plugin.countedKey, PersistentDataType.BYTE, (byte) 1); s.setItemMeta(m); } return s; }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(PlayerAttemptPickupItemEvent e) {
        Item item = e.getItem(); ItemStack stack = item.getItemStack();
        if (stack.getType() != Material.HEAVY_CORE || isCounted(stack)) return;
        if (plugin.getHeavyCoresFound() >= plugin.getMaceLimit()) {
            e.setCancelled(true); item.remove();
            e.getPlayer().sendMessage(ChatColor.RED + "This server is at its limit of " + plugin.getMaceLimit() + " Maces, so this Heavy Core crumbles to dust.");
            return;
        }
        plugin.incrementHeavyCores(); item.setItemStack(tagCounted(stack));
        announce(ChatColor.GOLD + "" + ChatColor.BOLD + "A Heavy Core has been claimed! " + ChatColor.YELLOW + "(" + plugin.getHeavyCoresFound() + "/" + plugin.getMaceLimit() + ")");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(PrepareItemCraftEvent e) { if (e.getRecipe() != null && e.getInventory().getResult() != null && e.getInventory().getResult().getType() == Material.MACE) e.getInventory().setResult(tagCounted(e.getInventory().getResult())); }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) { if (e.getBlockPlaced().getType() == Material.HEAVY_CORE && isCounted(e.getItemInHand())) plugin.addCountedBlock(e.getBlockPlaced()); }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent e) {
        if (e.getBlockState().getType() != Material.HEAVY_CORE || !plugin.isCountedBlock(e.getBlock())) return;
        for (Item d : e.getItems()) d.setItemStack(tagCounted(d.getItemStack()));
        plugin.removeCountedBlock(e.getBlock());
    }

    private void destroyOne(String how) { plugin.decrementHeavyCores(); announce(ChatColor.RED + "A Mace was destroyed " + how + "! " + ChatColor.GRAY + "(" + plugin.getHeavyCoresFound() + "/" + plugin.getMaceLimit() + " remain)"); }

    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Item item) || !isCounted(item.getItemStack())) return;
        if (item.getPersistentDataContainer().has(plugin.decrementedKey, PersistentDataType.BYTE)) return;
        if (e.getFinalDamage() >= item.getHealth()) { item.getPersistentDataContainer().set(plugin.decrementedKey, PersistentDataType.BYTE, (byte) 1); destroyOne("in the world"); }
    }

    @EventHandler(ignoreCancelled = true) public void onDespawn(ItemDespawnEvent e) { if (isCounted(e.getEntity().getItemStack())) destroyOne("(despawned)"); }
    @EventHandler(ignoreCancelled = true) public void onBreak(PlayerItemBreakEvent e) { if (e.getBrokenItem().getType() == Material.MACE && isCounted(e.getBrokenItem())) destroyOne("(worn out)"); }
    @EventHandler(ignoreCancelled = true) public void onExplode(EntityExplodeEvent e) { handleExploded(e.blockList()); }
    @EventHandler(ignoreCancelled = true) public void onBlockExplode(BlockExplodeEvent e) { handleExploded(e.blockList()); }

    private void handleExploded(List<org.bukkit.block.Block> blocks) { for (var b : blocks) if (b.getType() == Material.HEAVY_CORE && plugin.isCountedBlock(b)) { plugin.removeCountedBlock(b); destroyOne("in an explosion"); } }
    private void announce(String msg) { if (plugin.announceClaims()) Bukkit.broadcastMessage(msg); }
}
