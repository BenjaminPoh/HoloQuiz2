package benloti.holoquiz.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class ConfigFile {
    private final int interval;
    private final int leaderboardSize;
    private final int leaderboardMinReq;
    private final boolean easterEggsEnabled;
    private final String gameMode;

    public ConfigFile(JavaPlugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            Bukkit.getLogger().log(Level.SEVERE, "Yabe peko");
        }

        FileConfiguration information = YamlConfiguration.loadConfiguration(configFile);
        this.interval = information.getInt("Interval");
        this.leaderboardSize = information.getInt("LeaderboardSize");
        this.leaderboardMinReq = information.getInt("LeaderboardMinQuestionsNeeded");
        this.easterEggsEnabled = information.getBoolean("EasterEggs");
        this.gameMode = information.getString("GameMode");
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
}
