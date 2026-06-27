package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RuleCommand implements CommandExecutor {
    private final SummerSMPCore plugin;
    public RuleCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(Component.text("Summer SMP 2 Rules", NamedTextColor.GOLD, TextDecoration.BOLD));
        List<String> rules = plugin.getConfig().getStringList("rules");
        if (rules.isEmpty()) sender.sendMessage(Component.text("No rules are set.", NamedTextColor.GRAY));
        else for (String line : rules) sender.sendMessage(Component.text(line, NamedTextColor.WHITE));
        sender.sendMessage(Component.text("Not sure if something's allowed? Ask staff first.", NamedTextColor.GRAY));
        return true;
    }
}
