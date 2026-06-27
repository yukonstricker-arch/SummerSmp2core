package net.summersmp.core;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BanCodeCommand implements CommandExecutor {
    private final SummerSMPCore plugin;
    public BanCodeCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("summersmp.ban")) { sender.sendMessage(ChatColor.RED + "You don't have permission."); return true; }
        if (args.length < 1) { sender.sendMessage(ChatColor.RED + "Usage: /bancode <code>"); return true; }
        String result = plugin.getBanCodeManager().lookup(args[0]);
        if (result == null) { sender.sendMessage(ChatColor.RED + "No ban found with code \"" + args[0].toUpperCase() + "\"."); return true; }
        sender.sendMessage(ChatColor.GOLD + "=== Ban Lookup ===");
        for (String line : result.split("\n")) sender.sendMessage(ChatColor.GRAY + line);
        return true;
    }
}
