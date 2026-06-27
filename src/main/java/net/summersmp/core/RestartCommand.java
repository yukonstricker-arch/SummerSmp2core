package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class RestartCommand implements CommandExecutor {
    private final SummerSMPCore plugin;
    public RestartCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("summersmp.restart")) { sender.sendMessage("You don't have permission."); return true; }
        new BukkitRunnable() {
            int count = 5;
            @Override public void run() {
                if (count <= 0) {
                    cancel();
                    Bukkit.broadcast(Component.text("Server restarting now!", NamedTextColor.RED, TextDecoration.BOLD));
                    Bukkit.getServer().shutdown();
                    return;
                }
                Bukkit.broadcast(Component.text("Server restarting in " + count + "...", NamedTextColor.RED));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        return true;
    }
}
