package benloti.holoquiz.files;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class ConfigFile {
    private final int interval;
    private final int intervalCheck;
    private final int leaderboardSize;
    private final int leaderboardMinReq;
    private final boolean easterEggsEnabled;
    private final String gameMode;
    private final boolean cheatsDetectorEnabled;
    private final int minTimeRequired;
    private final int QuestionCooldownLength;
    private final boolean countAsCorrect;
    private final List<String> cheatingCommands;
    private final boolean enableOnStart;
    private final String pluginPrefix;
    private final int mathRange;
    private final String mathDistribution;
    private final boolean mathDivisorLimit;
    private final int mathOperationLimit;
    private final boolean mathChaosMode;
    private final String mathQuestionColour;
    private final String mathDifficulty;
    private final boolean dailyContest;
    private final boolean weeklyContest;
    private final boolean monthlyContest;
    private final int dailyMin;
    private final int weeklyMin;
    private final int monthlyMin;
    private final String weeklyResetDay;
    private final String timezoneOffset;
    private final boolean isMultipleContestPositionAllowed;

    public ConfigFile(JavaPlugin plugin, String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        FileConfiguration configs = YamlConfiguration.loadConfiguration(configFile);
        this.interval = configs.getInt("Interval");
        this.intervalCheck = configs.getInt("IntervalCheck");
        this.leaderboardSize = configs.getInt("LeaderboardSize");
        this.leaderboardMinReq = configs.getInt("LeaderboardMinQuestionsNeeded");
        this.easterEggsEnabled = configs.getBoolean("EasterEggs");
        this.gameMode = configs.getString("GameMode");
        this.enableOnStart = configs.getBoolean("EnableOnStart");
        this.QuestionCooldownLength = configs.getInt("QuestionCooldown");
        ConfigurationSection cheatSection= configs.getConfigurationSection("Cheats");
        this.cheatsDetectorEnabled = cheatSection.getBoolean("CheatingChecker");
        this.minTimeRequired = (int) (cheatSection.getDouble("CheatingTimer") * 1000);
        this.countAsCorrect = cheatSection.getBoolean("CountAsCorrect");
        this.cheatingCommands = cheatSection.getStringList("CommandToPerform");
        ConfigurationSection mathSection = configs.getConfigurationSection("QuickMath");
        this.mathRange = mathSection.getInt("MathRange");
        this.mathDistribution = mathSection.getString("Distribution");
        this.mathDivisorLimit = mathSection.getBoolean("DivisorLimit");
        this.mathOperationLimit = mathSection.getInt("OperationsLimit");
        this.mathChaosMode = mathSection.getBoolean("ChaosMode");
        this.mathQuestionColour = mathSection.getString("QuestionColour");
        this.mathDifficulty = mathSection.getString("MathDifficulty");
        ConfigurationSection contestSection = configs.getConfigurationSection("Contests");
        this.dailyContest = contestSection.getBoolean("Daily");
        this.weeklyContest = contestSection.getBoolean("Weekly");
        this.monthlyContest = contestSection.getBoolean("Monthly");
        this.weeklyResetDay = contestSection.getString("WeeklyResetDay");
        this.dailyMin = contestSection.getInt("DailyMin");
        this.weeklyMin = contestSection.getInt("WeeklyMin");
        this.monthlyMin = contestSection.getInt("MonthlyMin");
        this.timezoneOffset = contestSection.getString("TimeZone", "GMT+0");
        this.isMultipleContestPositionAllowed = contestSection.getBoolean("RepeatWinning");
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

    public int getMathRange() {
        return mathRange;
    }

    public String getMathDifficulty() {
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

    public boolean isDailyContest() {
        return dailyContest;
    }

    public boolean isWeeklyContest() {
        return weeklyContest;
    }

    public boolean isMonthlyContest() {
        return monthlyContest;
    }

    public String getWeeklyResetDay() {
        return weeklyResetDay;
    }

    public int getDailyMin() {
        return dailyMin;
    }

    public int getWeeklyMin() {
        return weeklyMin;
    }

    public int getMonthlyMin() {
        return monthlyMin;
    }

    public String getTimezoneOffset() {
        return timezoneOffset;
    }

    public boolean isMultipleContestPositionAllowed() {
        return isMultipleContestPositionAllowed;
    }

    public int getIntervalCheck() {
        return intervalCheck;
    }

    public int getQuestionCooldownLength() {
        return QuestionCooldownLength;
    }
}
