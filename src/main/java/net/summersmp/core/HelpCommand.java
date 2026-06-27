package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements CommandExecutor {
    private final SummerSMPCore plugin;
    public HelpCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(Component.text("Summer SMP 2 Commands", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text("/rtp", NamedTextColor.AQUA).append(Component.text(" — Random teleport into the world", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/spawn", NamedTextColor.AQUA).append(Component.text(" — Teleport back to hub", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/world", NamedTextColor.AQUA).append(Component.text(" — Teleport to 0,0", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/tpa <player>", NamedTextColor.AQUA).append(Component.text(" — Request to teleport to someone", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/tpaccept", NamedTextColor.AQUA).append(Component.text(" — Accept a teleport request", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/tpdeny", NamedTextColor.AQUA).append(Component.text(" — Deny a teleport request", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/rules", NamedTextColor.AQUA).append(Component.text(" — View server rules", NamedTextColor.GRAY)));
        if (!plugin.getDragonManager().isCommandDisabled()) {
            sender.sendMessage(Component.text("/dragon", NamedTextColor.AQUA).append(Component.text(" — Time until the dragon fight", NamedTextColor.GRAY)));
        }
        String discord = plugin.getConfig().getString("discord-link", "https://discord.gg/pfC7kjSG9");
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Need more help? Join our Discord for support:", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(discord, NamedTextColor.AQUA));
        return true;
    }
}
