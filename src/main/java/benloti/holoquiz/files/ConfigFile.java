package benloti.holoquiz.files;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class ConfigFile {
    private final int interval;
    private final int leaderboardSize;
    private final int leaderboardMinReq;
    private final boolean easterEggsEnabled;
    private final String gameMode;
    private final boolean cheatsDetectorEnabled;
    private final int minTimeRequired;
    private final boolean countAsCorrect;
    private final List<String> cheatingCommands;
    private final boolean enableOnStart;
    private final String pluginPrefix;
    private final int mathDifficulty;
    private final String mathDistribution;
    private final boolean mathDivisorLimit;
    private final int mathOperationLimit;
    private final boolean mathChaosMode;
    private final String mathQuestionColour;

    public ConfigFile(JavaPlugin plugin, String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);

        FileConfiguration configs = YamlConfiguration.loadConfiguration(configFile);
        this.interval = configs.getInt("Interval");
        this.leaderboardSize = configs.getInt("LeaderboardSize");
        this.leaderboardMinReq = configs.getInt("LeaderboardMinQuestionsNeeded");
        this.easterEggsEnabled = configs.getBoolean("EasterEggs");
        this.gameMode = configs.getString("GameMode");
        this.enableOnStart = configs.getBoolean("EnableOnStart");
        ConfigurationSection cheatSection= configs.getConfigurationSection("Cheats");
        this.cheatsDetectorEnabled = cheatSection.getBoolean("CheatingChecker");
        this.minTimeRequired = (int) (cheatSection.getDouble("CheatingTimer") * 1000);
        this.countAsCorrect = cheatSection.getBoolean("CountAsCorrect");
        this.cheatingCommands = cheatSection.getStringList("CommandToPerform");
        ConfigurationSection mathSection = configs.getConfigurationSection("QuickMath");
        this.mathDifficulty = mathSection.getInt("MathDifficulty");
        this.mathDistribution = mathSection.getString("Distribution");
        this.mathDivisorLimit = mathSection.getBoolean("DivisorLimit");
        this.mathOperationLimit = mathSection.getInt("OperationsLimit");
        this.mathChaosMode = mathSection.getBoolean("ChaosMode");
        this.mathQuestionColour = mathSection.getString("QuestionColour");
        this.pluginPrefix = configs.getString("PluginPrefix");
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

    public String getPluginPrefix() {
        return pluginPrefix;
    }

    public int getMathDifficulty() {
        return mathDifficulty;
    }

    public String getMathDistribution() {
        return mathDistribution;
    }

    public boolean isMathDivisorLimit() {
        return mathDivisorLimit;
    }

    public int getMathOperationLimit() {
        return mathOperationLimit;
    }

    public boolean isMathChaosMode() {
        return mathChaosMode;
    }

    public String getMathQuestionColour() {
        return mathQuestionColour;
    }
}
