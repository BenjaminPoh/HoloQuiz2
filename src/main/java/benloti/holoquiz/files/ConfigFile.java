package benloti.holoquiz.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public class ConfigFile {
    private final int interval;
    private final int leaderboardSize;
    private final int leaderboardMinReq;
    private final boolean leaderboardOptimisation;
    private final boolean easterEggsEnabled;
    private final String gameMode;
    private final boolean cheatsDetectorEnabled;
    private final int minTimeRequired;
    private final boolean countAsCorrect;
    private final List<String> cheatingCommands;
    private final boolean enableOnStart;

    public ConfigFile(JavaPlugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            Bukkit.getLogger().log(Level.SEVERE, "Yabe peko");
        }

        FileConfiguration configs = YamlConfiguration.loadConfiguration(configFile);
        this.interval = configs.getInt("Interval");
        this.leaderboardSize = configs.getInt("LeaderboardSize");
        this.leaderboardMinReq = configs.getInt("LeaderboardMinQuestionsNeeded");
        this.leaderboardOptimisation = configs.getBoolean("LeaderboardOptimisation");
        this.easterEggsEnabled = configs.getBoolean("EasterEggs");
        this.gameMode = configs.getString("GameMode");
        this.enableOnStart = configs.getBoolean("EnableOnStart");
        ConfigurationSection cheatSection= configs.getConfigurationSection("Cheats");
        this.cheatsDetectorEnabled = cheatSection.getBoolean("CheatingChecker");
        this.minTimeRequired = (int) (cheatSection.getDouble("CheatingTimer") * 1000);
        this.countAsCorrect = cheatSection.getBoolean("CountAsCorrect");
        this.cheatingCommands = cheatSection.getStringList("CommandToPerform");
    }

    public int getInterval() {
        return interval;
    }

    public int getLeaderboardSize() {
        return leaderboardSize;
    }

    public int getLeaderboardMinReq() {
        return leaderboardMinReq;
    }

    public boolean isEasterEggsEnabled() {
        return easterEggsEnabled;
    }

    public String getGameMode() {
        return gameMode;
    }

    public boolean isCheatsDetectorEnabled() {
        return cheatsDetectorEnabled;
    }

    public int getMinTimeRequired() {
        return minTimeRequired;
    }

    public boolean isCountAsCorrect() {
        return countAsCorrect;
    }

    public List<String> getCheatingCommands() {
        return cheatingCommands;
    }

    public boolean isEnableOnStart() {
        return enableOnStart;
    }

    public boolean isLeaderboardOptimisation() {
        return leaderboardOptimisation;
    }
}
