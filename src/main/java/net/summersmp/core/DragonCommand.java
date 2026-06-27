package net.summersmp.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DragonCommand implements CommandExecutor {
    private final SummerSMPCore plugin;
    public DragonCommand(SummerSMPCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        DragonManager dm = plugin.getDragonManager();
        if (dm.isCommandDisabled()) {
            sender.sendMessage(Component.text("The dragon fight has already happened!", NamedTextColor.GRAY));
            return true;
        }
        if (dm.isEndOpened()) {
            sender.sendMessage(Component.text("The End is NOW OPEN!", NamedTextColor.LIGHT_PURPLE));
            return true;
        }
        String countdown = dm.getCountdown();
        if (countdown == null) {
            sender.sendMessage(Component.text("The End is NOW OPEN!", NamedTextColor.LIGHT_PURPLE));
        } else {
            sender.sendMessage(Component.text("Dragon fight in " + countdown + "!", NamedTextColor.LIGHT_PURPLE));
        }
        return true;
    }
}
