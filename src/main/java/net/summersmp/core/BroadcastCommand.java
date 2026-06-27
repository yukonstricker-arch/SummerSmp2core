package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BroadcastCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("summersmp.broadcast")) { sender.sendMessage("You don't have permission."); return true; }
        if (args.length == 0) { sender.sendMessage("Usage: /broadcast <message>"); return true; }
        String msg = String.join(" ", args);
        Bukkit.broadcast(Component.text("BROADCAST ", NamedTextColor.GOLD, TextDecoration.BOLD).append(Component.text("» " + msg, NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false)));
        return true;
    }
}
