package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final SummerSMPCore plugin;
    public JoinListener(SummerSMPCore plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.sendMessage(Component.text("Welcome to Summer SMP 2!", NamedTextColor.GOLD, TextDecoration.BOLD));
        p.sendMessage(Component.text("Lifesteal — kill players to steal hearts. Hit 0 and you're eliminated.", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("Commands: /rtp  /spawn  /tpa  /rules  /help", NamedTextColor.AQUA));
        if (!plugin.getDragonManager().isCommandDisabled()) {
            p.sendMessage(Component.text("Dragon fight: July 17th!", NamedTextColor.LIGHT_PURPLE));
        }
    }
}
