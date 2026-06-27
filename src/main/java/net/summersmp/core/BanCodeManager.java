package net.summersmp.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class BanCodeManager {
    private final SummerSMPCore plugin;
    private final File file;
    private final FileConfiguration data;
    private final Random random = new Random();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public BanCodeManager(SummerSMPCore plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        this.file = new File(plugin.getDataFolder(), "bancodes.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public String record(String player, String reason, String durationText, String staff) {
        String code = newCode();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm z").format(new Date());
        data.set(code + ".player", player);
        data.set(code + ".reason", reason);
        data.set(code + ".duration", durationText);
        data.set(code + ".staff", staff);
        data.set(code + ".date", now);
        save();
        return code;
    }

    public String lookup(String code) {
        String key = code.toUpperCase();
        if (!data.contains(key + ".player")) return null;
        return "Code " + key + "\nPlayer:   " + data.getString(key + ".player")
                + "\nReason:   " + data.getString(key + ".reason")
                + "\nDuration: " + data.getString(key + ".duration")
                + "\nBanned by: " + data.getString(key + ".staff")
                + "\nDate:     " + data.getString(key + ".date");
    }

    private String newCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5; i++) sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
            code = sb.toString();
        } while (data.contains(code + ".player"));
        return code;
    }

    private void save() {
        try { data.save(file); } catch (IOException e) { plugin.getLogger().warning("Could not save bancodes.yml"); }
    }
}
