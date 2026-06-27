package net.summersmp.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class KickCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("summersmp.kick")) { sender.sendMessage(ChatColor.RED + "You don't have permission."); return true; }
        if (args.length < 1) { sender.sendMessage(ChatColor.RED + "Usage: /kick <player> [reason]"); return true; }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) { sender.sendMessage(ChatColor.RED + "Player not found."); return true; }
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Kicked by staff";
        target.kickPlayer(ChatColor.RED + "" + ChatColor.BOLD + "You have been kicked.\n\n" + ChatColor.GRAY + "Reason: " + ChatColor.WHITE + reason);
        sender.sendMessage(ChatColor.GREEN + "Kicked " + target.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) { String p = args[0].toLowerCase(); for (Player pl : Bukkit.getOnlinePlayers()) if (pl.getName().toLowerCase().startsWith(p)) out.add(pl.getName()); }
        return out;
    }
}
