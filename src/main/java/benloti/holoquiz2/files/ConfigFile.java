package benloti.holoquiz2.files;

import benloti.holoquiz2.structs.Question;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public class ConfigFile {
    private final JavaPlugin plugin;
    private final int interval;
    private final int leaderboardSize;
    private final int leaderboardMinReq;

    public ConfigFile(JavaPlugin plugin) {
        this.plugin = plugin;
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            Bukkit.getLogger().log(Level.SEVERE, "Yabe peko");
        }

        FileConfiguration information = YamlConfiguration.loadConfiguration(configFile);
        this.interval = information.getInt("Interval");
        this.leaderboardSize = information.getInt("LeaderboardSize");
        this.leaderboardMinReq = information.getInt("LeaderboardMinQuestionsNeeded");
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


}
