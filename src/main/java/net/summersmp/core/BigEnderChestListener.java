package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class BigEnderChestListener implements Listener {
    private final SummerSMPCore plugin;
    private final File folder;

    public BigEnderChestListener(SummerSMPCore plugin) { this.plugin = plugin; folder = new File(plugin.getDataFolder(), "enderchests"); if (!folder.exists()) folder.mkdirs(); }

    public static class Holder implements InventoryHolder { private final UUID owner; private Inventory inventory; public Holder(UUID o) { owner = o; } public UUID getOwner() { return owner; } public void setInventory(Inventory i) { inventory = i; } @Override public Inventory getInventory() { return inventory; } }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (!plugin.getConfig().getBoolean("big-ender-chest.enabled", true)) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.ENDER_CHEST) return;
        if (e.getPlayer().isSneaking() && e.getItem() != null && e.getItem().getType().isBlock()) return;
        e.setCancelled(true);
        Player p = e.getPlayer(); Holder h = new Holder(p.getUniqueId()); Inventory inv = Bukkit.createInventory(h, 54, Component.text("Ender Chest")); h.setInventory(inv); load(p.getUniqueId(), inv); p.openInventory(inv);
    }

    @EventHandler public void onClose(InventoryCloseEvent e) { if (e.getInventory().getHolder() instanceof Holder h) save(h.getOwner(), e.getInventory()); }

    private boolean restricted(ItemStack s) { if (s == null) return false; Material m = s.getType(); return m == Material.HEAVY_CORE || m == Material.MACE; }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof Holder)) return;
        int top = e.getView().getTopInventory().getSize(); int raw = e.getRawSlot();
        if (e.isShiftClick() && raw >= top && restricted(e.getCurrentItem())) { deny(e); return; }
        if (raw >= 0 && raw < top) {
            if (restricted(e.getCursor())) { deny(e); return; }
            if (e.getClick() == ClickType.NUMBER_KEY && e.getHotbarButton() >= 0 && restricted(e.getView().getBottomInventory().getItem(e.getHotbarButton()))) { deny(e); return; }
            if (e.getClick() == ClickType.SWAP_OFFHAND && e.getWhoClicked() instanceof Player p && restricted(p.getInventory().getItemInOffHand())) deny(e);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof Holder) || !restricted(e.getOldCursor())) return;
        int top = e.getView().getTopInventory().getSize();
        for (int s : e.getRawSlots()) if (s < top) { e.setCancelled(true); e.getWhoClicked().sendMessage(Component.text("You can't store Heavy Cores or Maces in an ender chest.", NamedTextColor.RED)); return; }
    }

    private void deny(InventoryClickEvent e) { e.setCancelled(true); e.getWhoClicked().sendMessage(Component.text("You can't store Heavy Cores or Maces in an ender chest.", NamedTextColor.RED)); }
    private File file(UUID id) { return new File(folder, id + ".yml"); }
    private void load(UUID id, Inventory inv) { File f = file(id); if (!f.exists()) return; FileConfiguration c = YamlConfiguration.loadConfiguration(f); for (int i = 0; i < inv.getSize(); i++) if (c.contains("slot." + i)) inv.setItem(i, c.getItemStack("slot." + i)); }
    private void save(UUID id, Inventory inv) { FileConfiguration c = new YamlConfiguration(); for (int i = 0; i < inv.getSize(); i++) { ItemStack it = inv.getItem(i); if (it != null) c.set("slot." + i, it); } try { c.save(file(id)); } catch (IOException e) { plugin.getLogger().warning("Could not save ender chest for " + id); } }
}
