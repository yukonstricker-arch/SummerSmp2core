package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PauseCommand implements CommandExecutor {
    private final SummerSMPCore plugin;
    public PauseCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("summersmp.pause")) { sender.sendMessage("You don't have permission."); return true; }
        if (cmd.getName().equalsIgnoreCase("pause")) {
            plugin.getPauseManager().setPaused(true);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("summersmp.pause")) {
                    p.kickPlayer("Server is currently under maintenance. Please try again later.");
                }
            }
            String staffName = sender instanceof Player ? sender.getName() : "Console";
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Component.text("Server has been paused for maintenance by " + staffName + ".", NamedTextColor.YELLOW));
            }
            sender.sendMessage("Server is now in maintenance mode. Non-staff cannot join.");
        } else {
            plugin.getPauseManager().setPaused(false);
            sender.sendMessage("Maintenance mode ended. Players can now join.");
        }
        return true;
    }
}
