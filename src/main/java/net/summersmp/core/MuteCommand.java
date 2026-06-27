package net.summersmp.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.*;

public class MuteCommand implements CommandExecutor, TabCompleter {
    private final SummerSMPCore plugin;
    public MuteCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("summersmp.mute")) { sender.sendMessage(ChatColor.RED + "You don't have permission."); return true; }
        if (cmd.getName().equalsIgnoreCase("unmute")) return handleUnmute(sender, args);
        return handleMute(sender, args);
    }

    private boolean handleMute(CommandSender sender, String[] args) {
        if (args.length < 1) { sender.sendMessage(ChatColor.RED + "Usage: /mute <player> [duration]"); return true; }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) { sender.sendMessage(ChatColor.RED + "Player not found."); return true; }
        long durationMs = 5 * 3600_000L; // default 5 hours
        String durationText = "5 hours";
        if (args.length >= 2) {
            Long parsed = parseCompact(args[1]);
            if (parsed != null) { durationMs = parsed; durationText = args[1]; }
        }
        plugin.getMuteManager().mute(target.getUniqueId(), durationMs);
        target.sendMessage(ChatColor.RED + "You have been muted for " + durationText + ". You cannot type or speak.");
        // Also mute in Simple Voice Chat if available
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "voicechat mute " + target.getName());
        sender.sendMessage(ChatColor.GREEN + "Muted " + target.getName() + " for " + durationText + ".");
        return true;
    }

    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (args.length < 1) { sender.sendMessage(ChatColor.RED + "Usage: /unmute <player>"); return true; }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target != null) {
            plugin.getMuteManager().unmute(target.getUniqueId());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "voicechat unmute " + target.getName());
            target.sendMessage(ChatColor.GREEN + "You have been unmuted.");
        }
        sender.sendMessage(ChatColor.GREEN + "Unmuted " + args[0] + ".");
        return true;
    }

    private Long parseCompact(String t) { Matcher m = Pattern.compile("^(\\d+)\\s*([a-zA-Z]+)$").matcher(t.trim()); if (!m.matches()) return null; long a = Long.parseLong(m.group(1)); long p = unitToMs(m.group(2)); return p <= 0 ? null : a * p; }
    private long unitToMs(String u) { u = u.toLowerCase(Locale.ROOT); if (u.startsWith("sec") || u.equals("s")) return 1000L; if (u.startsWith("min") || u.equals("m")) return 60_000L; if (u.startsWith("hour") || u.equals("h")) return 3_600_000L; if (u.startsWith("day") || u.equals("d")) return 86_400_000L; return -1; }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) { String p = args[0].toLowerCase(); for (Player pl : Bukkit.getOnlinePlayers()) if (pl.getName().toLowerCase().startsWith(p)) out.add(pl.getName()); }
        if (args.length == 2 && cmd.getName().equalsIgnoreCase("mute")) out.addAll(Arrays.asList("5h", "1h", "30m", "1d"));
        return out;
    }
}
