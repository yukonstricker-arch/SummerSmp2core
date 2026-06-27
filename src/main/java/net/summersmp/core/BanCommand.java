package net.summersmp.core;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

public class BanCommand implements CommandExecutor, TabCompleter {
    private final SummerSMPCore plugin;

    public BanCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("unban")) return handleUnban(sender, args);
        return handleBan(sender, args);
    }

    private boolean handleBan(CommandSender sender, String[] args) {
        if (!sender.hasPermission("summersmp.ban")) { sender.sendMessage(ChatColor.RED + "You don't have permission."); return true; }
        if (args.length < 2) { sender.sendMessage(ChatColor.RED + "Usage: /ban <player> <duration> [reason]"); return true; }
        String targetName = args[0];
        long durationMillis; int reasonStart;
        String first = args[1].toLowerCase(Locale.ROOT);
        if (first.equals("perm") || first.equals("permanent")) { durationMillis = -1; reasonStart = 2; }
        else if (first.matches("\\d+") && args.length >= 3 && unitToMs(args[2]) > 0) { durationMillis = Long.parseLong(first) * unitToMs(args[2]); reasonStart = 3; }
        else { Long c = parseCompact(args[1]); if (c == null) { sender.sendMessage(ChatColor.RED + "Bad duration. Try: 7d, 30m, 2w, perm"); return true; } durationMillis = c; reasonStart = 2; }
        String reason = args.length > reasonStart ? String.join(" ", Arrays.copyOfRange(args, reasonStart, args.length)) : plugin.defaultBanReason();
        Date expires = durationMillis < 0 ? null : new Date(System.currentTimeMillis() + durationMillis);
        String source = sender instanceof Player ? sender.getName() : "Console";
        String durationText = expires == null ? "Permanent" : fmt(expires);
        applyBan(targetName, reason, expires, source);
        String code = plugin.getBanCodeManager().record(targetName, reason, durationText, source);
        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            online.kickPlayer(ChatColor.RED + "" + ChatColor.BOLD + "You have been banned.\n\n"
                + ChatColor.GRAY + "Reason: " + ChatColor.WHITE + reason + "\n"
                + ChatColor.GRAY + (expires == null ? "Duration: " + ChatColor.WHITE + "Permanent" : "Expires: " + ChatColor.WHITE + fmt(expires)) + "\n\n"
                + ChatColor.GRAY + "Appeal code: " + ChatColor.YELLOW + code + "\n"
                + ChatColor.GRAY + "Open a ticket in our Discord with this code.");
        }
        sender.sendMessage(ChatColor.GREEN + "Banned " + targetName + (expires == null ? " permanently." : " until " + fmt(expires) + ".") + ChatColor.GRAY + " Reason: " + reason);
        sender.sendMessage(ChatColor.GRAY + "Ban code: " + ChatColor.YELLOW + code + ChatColor.GRAY + " (/bancode " + code + ")");
        return true;
    }

    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("summersmp.unban")) { sender.sendMessage(ChatColor.RED + "You don't have permission."); return true; }
        if (args.length < 1) { sender.sendMessage(ChatColor.RED + "Usage: /unban <player>"); return true; }
        removeBan(args[0]);
        sender.sendMessage(ChatColor.GREEN + "Unbanned " + args[0] + ".");
        return true;
    }

    @SuppressWarnings({"deprecation","rawtypes","unchecked"})
    private void applyBan(String n, String r, Date e, String s) { BanList l = Bukkit.getBanList(BanList.Type.NAME); l.addBan(n, r, e, s); }
    @SuppressWarnings({"deprecation","rawtypes"})
    private void removeBan(String n) { BanList l = Bukkit.getBanList(BanList.Type.NAME); l.pardon(n); }

    private long unitToMs(String u) { u = u.toLowerCase(Locale.ROOT); if (u.startsWith("sec") || u.equals("s")) return 1000L; if (u.startsWith("min") || u.equals("m")) return 60_000L; if (u.startsWith("hour") || u.equals("h")) return 3_600_000L; if (u.startsWith("day") || u.equals("d")) return 86_400_000L; if (u.startsWith("week") || u.equals("w")) return 604_800_000L; if (u.startsWith("month") || u.equals("mo")) return 2_592_000_000L; return -1; }
    private Long parseCompact(String t) { Matcher m = Pattern.compile("^(\\d+)\\s*([a-zA-Z]+)$").matcher(t.trim()); if (!m.matches()) return null; long a = Long.parseLong(m.group(1)); long p = unitToMs(m.group(2)); return p <= 0 ? null : a * p; }
    private String fmt(Date d) { return new SimpleDateFormat("yyyy-MM-dd HH:mm z").format(d); }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) { String p = args[0].toLowerCase(Locale.ROOT); for (Player pl : Bukkit.getOnlinePlayers()) if (pl.getName().toLowerCase(Locale.ROOT).startsWith(p)) out.add(pl.getName()); }
        if (args.length == 2 && cmd.getName().equalsIgnoreCase("ban")) out.addAll(Arrays.asList("7d", "12", "30m", "perm"));
        return out;
    }
}
