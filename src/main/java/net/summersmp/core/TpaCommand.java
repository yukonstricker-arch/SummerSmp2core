package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TpaCommand implements CommandExecutor, TabCompleter, Listener {
    private final SummerSMPCore plugin;
    private final long expiryMillis;
    private final Map<UUID, Request> pending = new HashMap<>();
    private final Map<UUID, Long> sendCooldown = new HashMap<>();
    private final Map<UUID, BukkitRunnable> warmups = new HashMap<>();

    public TpaCommand(SummerSMPCore plugin) {
        this.plugin = plugin;
        this.expiryMillis = plugin.getConfig().getInt("tpa.request-seconds", 60) * 1000L;
    }

    private record Request(UUID requester, boolean here, long expiry) {}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players can use this."); return true; }
        return switch (cmd.getName().toLowerCase(Locale.ROOT)) {
            case "tpa" -> request(player, args, false);
            case "tpahere" -> request(player, args, true);
            case "tpaccept" -> accept(player);
            case "tpdeny" -> deny(player);
            default -> false;
        };
    }

    private boolean request(Player player, String[] args, boolean here) {
        if (args.length < 1) { player.sendMessage(Component.text("Usage: /" + (here ? "tpahere" : "tpa") + " <player>", NamedTextColor.RED)); return true; }
        long now = System.currentTimeMillis();
        Long until = sendCooldown.get(player.getUniqueId());
        if (until != null && until > now) {
            player.sendMessage(Component.text("You can't send another teleport request for " + ((until - now + 999) / 1000) + " seconds.", NamedTextColor.RED)); return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) { player.sendMessage(Component.text("That player isn't online.", NamedTextColor.RED)); return true; }
        if (target.equals(player)) { player.sendMessage(Component.text("You can't teleport to yourself.", NamedTextColor.RED)); return true; }

        pending.put(target.getUniqueId(), new Request(player.getUniqueId(), here, now + expiryMillis));
        int cd = plugin.getConfig().getInt("tpa.send-cooldown-seconds", 15);
        sendCooldown.put(player.getUniqueId(), now + cd * 1000L);

        target.sendMessage(Component.text(player.getName() + (here ? " wants YOU to teleport to them." : " wants to teleport to you."), NamedTextColor.AQUA));
        target.sendMessage(Component.text("Type /tpaccept to allow, or /tpdeny to refuse.", NamedTextColor.GRAY));
        player.sendMessage(Component.text("Request sent to " + target.getName() + ".", NamedTextColor.GREEN));
        return true;
    }

    private boolean accept(Player player) {
        Request req = pending.get(player.getUniqueId());
        if (req == null || req.expiry() < System.currentTimeMillis()) { pending.remove(player.getUniqueId()); player.sendMessage(Component.text("You have no pending teleport requests.", NamedTextColor.RED)); return true; }
        Player requester = Bukkit.getPlayer(req.requester());
        if (requester == null || !requester.isOnline()) { pending.remove(player.getUniqueId()); player.sendMessage(Component.text("That player is no longer online.", NamedTextColor.RED)); return true; }
        Player mover = req.here() ? player : requester;
        Player dest = req.here() ? requester : player;
        if (plugin.getCombatManager() != null && plugin.getCombatManager().isTagged(mover)) { player.sendMessage(Component.text("Can't teleport — someone is in combat.", NamedTextColor.RED)); return true; }
        pending.remove(player.getUniqueId());

        requester.sendMessage(Component.text(player.getName() + " accepted! Teleporting in 5s...", NamedTextColor.GREEN));
        player.sendMessage(Component.text("Accepted! Teleporting in 5s...", NamedTextColor.GREEN));
        UUID moverId = mover.getUniqueId();
        BukkitRunnable warmup = new BukkitRunnable() {
            int remaining = 5;
            @Override public void run() {
                if (!mover.isOnline() || !dest.isOnline()) { cancel(); warmups.remove(moverId); return; }
                if (remaining <= 0) { cancel(); warmups.remove(moverId); mover.teleport(dest); mover.sendMessage(Component.text("Teleported!", NamedTextColor.GREEN)); return; }
                mover.sendActionBar(Component.text("Teleporting in " + remaining + "s...", NamedTextColor.YELLOW));
                remaining--;
            }
        };
        warmups.put(moverId, warmup);
        warmup.runTaskTimer(plugin, 0L, 20L);
        return true;
    }

    private boolean deny(Player player) {
        Request req = pending.remove(player.getUniqueId());
        if (req == null) { player.sendMessage(Component.text("You have no pending teleport requests.", NamedTextColor.RED)); return true; }
        Player requester = Bukkit.getPlayer(req.requester());
        if (requester != null) requester.sendMessage(Component.text(player.getName() + " denied your teleport request.", NamedTextColor.RED));
        player.sendMessage(Component.text("Request denied.", NamedTextColor.GRAY));
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        BukkitRunnable w = warmups.remove(p.getUniqueId());
        if (w != null) { w.cancel(); p.sendActionBar(Component.text("")); p.sendMessage(Component.text("Teleport cancelled — you took damage.", NamedTextColor.RED)); }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) { String p = args[0].toLowerCase(Locale.ROOT); for (Player pl : Bukkit.getOnlinePlayers()) if (pl.getName().toLowerCase(Locale.ROOT).startsWith(p)) out.add(pl.getName()); }
        return out;
    }
}
