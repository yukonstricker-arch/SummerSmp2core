package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class DragonManager {
    private final SummerSMPCore plugin;
    private final ZonedDateTime fightTime;
    private final ZonedDateTime commandRemoveTime;
    private boolean endOpened = false;
    private boolean commandDisabled = false;
    private final Set<String> firedMilestones = new HashSet<>();

    public DragonManager(SummerSMPCore plugin) {
        this.plugin = plugin;
        String tz = plugin.getConfig().getString("dragon.timezone", "America/New_York");
        int year = plugin.getConfig().getInt("dragon.year", 2026);
        int month = plugin.getConfig().getInt("dragon.month", 7);
        int day = plugin.getConfig().getInt("dragon.day", 17);
        int hour = plugin.getConfig().getInt("dragon.hour", 15);
        this.fightTime = ZonedDateTime.of(year, month, day, hour, 0, 0, 0, ZoneId.of(tz));
        this.commandRemoveTime = fightTime.plusDays(3);
    }

    public boolean isCommandDisabled() { return commandDisabled; }
    public boolean isEndOpened() { return endOpened; }

    public String getCountdown() {
        Duration d = Duration.between(ZonedDateTime.now(fightTime.getZone()), fightTime);
        if (d.isNegative() || d.isZero()) return null;
        long days = d.toDays();
        long hours = d.toHours() % 24;
        long mins = d.toMinutes() % 60;
        long secs = d.getSeconds() % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" day").append(days != 1 ? "s" : "").append(", ");
        if (hours > 0) sb.append(hours).append(" hour").append(hours != 1 ? "s" : "").append(", ");
        if (mins > 0) sb.append(mins).append(" minute").append(mins != 1 ? "s" : "").append(", ");
        sb.append(secs).append(" second").append(secs != 1 ? "s" : "");
        return sb.toString();
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ZonedDateTime now = ZonedDateTime.now(fightTime.getZone());
                Duration d = Duration.between(now, fightTime);

                if (now.isAfter(commandRemoveTime) && !commandDisabled) {
                    commandDisabled = true;
                    plugin.getLogger().info("Dragon fight is over. /dragon command disabled.");
                }

                if (d.isNegative() || d.isZero()) {
                    if (!endOpened) {
                        endOpened = true;
                        broadcast(Component.text("THE END IS NOW OPEN! Go fight the dragon!", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import world_the_end end");
                    }
                    return;
                }

                long totalMins = d.toMinutes();
                long totalHours = d.toHours();
                long totalDays = d.toDays();

                checkMilestone("1d", totalDays <= 0 && totalHours <= 24, "Dragon fight in 1 DAY!");
                checkMilestone("10h", totalHours <= 10 && totalHours > 0, "Dragon fight in 10 HOURS!");
                checkMilestone("1h", totalHours <= 1 && totalMins > 10, "Dragon fight in 1 HOUR!");
                checkMilestone("10m", totalMins <= 10 && totalMins > 0, "Dragon fight in 10 MINUTES!");
            }
        }.runTaskTimer(plugin, 200L, 1200L);
    }

    private void checkMilestone(String key, boolean condition, String msg) {
        if (condition && !firedMilestones.contains(key)) {
            firedMilestones.add(key);
            broadcast(Component.text(msg, NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
        }
    }

    private void broadcast(Component msg) {
        Bukkit.broadcast(msg);
    }
}
