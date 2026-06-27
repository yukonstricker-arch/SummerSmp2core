package net.summersmp.core;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class SummerSMPCore extends JavaPlugin {

    private int maceLimit;
    private int heavyCoresFound;
    private final Set<String> countedBlocks = new HashSet<>();
    private File dataFile;
    private FileConfiguration dataConfig;
    public NamespacedKey countedKey;
    public NamespacedKey decrementedKey;
    private CombatManager combatManager;
    private BanCodeManager banCodeManager;
    private MuteManager muteManager;
    private PauseManager pauseManager;
    private DragonManager dragonManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadSettings();
        countedKey = new NamespacedKey(this, "counted_mace");
        decrementedKey = new NamespacedKey(this, "decremented_mace");
        loadData();
        banCodeManager = new BanCodeManager(this);
        muteManager = new MuteManager(this);
        pauseManager = new PauseManager(this);
        dragonManager = new DragonManager(this);

        try { Bukkit.removeRecipe(NamespacedKey.minecraft("end_crystal")); }
        catch (Throwable t) { getLogger().warning("Could not remove end crystal recipe: " + t.getMessage()); }

        // Listeners
        getServer().getPluginManager().registerEvents(new EndCrystalListener(), this);
        getServer().getPluginManager().registerEvents(new MaceLimitListener(this), this);
        getServer().getPluginManager().registerEvents(new BigEnderChestListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(this), this);
        combatManager = new CombatManager(this);
        getServer().getPluginManager().registerEvents(combatManager, this);
        combatManager.runTaskTimer(this, 20L, 20L);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ClientBrandListener(this), this);
        getServer().getPluginManager().registerEvents(muteManager, this);
        getServer().getPluginManager().registerEvents(pauseManager, this);

        RtpCommand rtpCommand = new RtpCommand(this);
        reg("rtp", rtpCommand); getServer().getPluginManager().registerEvents(rtpCommand, this);
        SpawnCommand spawnCommand = new SpawnCommand(this);
        reg("spawn", spawnCommand); getServer().getPluginManager().registerEvents(spawnCommand, this);
        WorldCommand worldCommand = new WorldCommand(this);
        reg("world", worldCommand); getServer().getPluginManager().registerEvents(worldCommand, this);
        TpaCommand tpaCommand = new TpaCommand(this);
        reg("tpa", tpaCommand); reg("tpahere", tpaCommand); reg("tpaccept", tpaCommand); reg("tpdeny", tpaCommand);
        getServer().getPluginManager().registerEvents(tpaCommand, this);

        BanCommand banCommand = new BanCommand(this);
        reg("ban", banCommand); reg("unban", banCommand);
        reg("bancode", new BanCodeCommand(this));
        reg("maces", new MaceCommand(this));
        reg("rules", new RuleCommand(this));
        reg("help", new HelpCommand(this));
        reg("support", new SupportCommand(this));
        reg("broadcast", new BroadcastCommand());
        reg("kick", new KickCommand());
        MuteCommand muteCmd = new MuteCommand(this);
        reg("mute", muteCmd); reg("unmute", muteCmd);
        PauseCommand pauseCmd = new PauseCommand(this);
        reg("pause", pauseCmd); reg("unpause", pauseCmd);
        reg("restart", new RestartCommand(this));
        reg("dragon", new DragonCommand(this));

        dragonManager.start();
        getLogger().info("SummerSMPCore v2.0 enabled. Mace limit: " + maceLimit + " | counted: " + heavyCoresFound);
    }

    private void reg(String name, org.bukkit.command.CommandExecutor exec) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(exec);
            if (exec instanceof org.bukkit.command.TabCompleter)
                getCommand(name).setTabCompleter((org.bukkit.command.TabCompleter) exec);
        } else {
            getLogger().warning("Command '" + name + "' missing from plugin.yml!");
        }
    }

    public CombatManager getCombatManager() { return combatManager; }
    public BanCodeManager getBanCodeManager() { return banCodeManager; }
    public MuteManager getMuteManager() { return muteManager; }
    public PauseManager getPauseManager() { return pauseManager; }
    public DragonManager getDragonManager() { return dragonManager; }

    public void reloadSettings() { reloadConfig(); maceLimit = getConfig().getInt("mace-limit", 3); }

    private void loadData() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) { getDataFolder().mkdirs(); try { dataFile.createNewFile(); } catch (IOException e) { getLogger().warning("Could not create data.yml"); } }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        heavyCoresFound = dataConfig.getInt("heavy-cores-found", 0);
        countedBlocks.clear(); countedBlocks.addAll(dataConfig.getStringList("counted-blocks"));
    }

    private void saveData() {
        if (dataConfig == null || dataFile == null) return;
        dataConfig.set("heavy-cores-found", heavyCoresFound);
        dataConfig.set("counted-blocks", new java.util.ArrayList<>(countedBlocks));
        try { dataConfig.save(dataFile); } catch (IOException e) { getLogger().warning("Could not save data.yml"); }
    }

    public int getMaceLimit() { return maceLimit; }
    public void setMaceLimit(int v) { maceLimit = Math.max(0, v); getConfig().set("mace-limit", maceLimit); saveConfig(); }
    public boolean announceClaims() { return getConfig().getBoolean("announce-heavy-core-claims", true); }
    public String defaultBanReason() { return getConfig().getString("default-ban-reason", "Banned by staff"); }
    public int getHeavyCoresFound() { return heavyCoresFound; }
    public void setHeavyCoresFound(int v) { heavyCoresFound = Math.max(0, v); saveData(); }
    public void incrementHeavyCores() { setHeavyCoresFound(heavyCoresFound + 1); }
    public void decrementHeavyCores() { setHeavyCoresFound(heavyCoresFound - 1); }

    private String key(Block b) { return b.getWorld().getName() + ";" + b.getX() + ";" + b.getY() + ";" + b.getZ(); }
    public boolean isCountedBlock(Block b) { return countedBlocks.contains(key(b)); }
    public void addCountedBlock(Block b) { countedBlocks.add(key(b)); saveData(); }
    public void removeCountedBlock(Block b) { countedBlocks.remove(key(b)); saveData(); }
}
