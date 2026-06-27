package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SupportCommand implements CommandExecutor {
    private final SummerSMPCore plugin;
    public SupportCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String discord = plugin.getConfig().getString("discord-link", "https://discord.gg/pfC7kjSG9");
        sender.sendMessage(Component.text("Need help with anything? Ask in our Discord:", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(discord, NamedTextColor.AQUA));
        return true;
    }
}
