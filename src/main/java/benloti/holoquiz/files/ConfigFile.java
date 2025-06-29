package benloti.holoquiz.files;

import benloti.holoquiz.games.RewardsHandler;
import benloti.holoquiz.structs.ContestInfo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.ZoneId;
import java.util.List;

public class ConfigFile {
    private static final String ERROR_MISSING_CONFIG = "[HoloQuiz] ERROR: Missing Config Key %s!" ;
    private static final String WARNING_SRTS_EMPTY_WHITELIST = "[HoloQuiz] Warning: SRTS uses an empty Whitelist - No one can claim reward items!" ;
    private static final String WARNING_CONTEST_INVALID_MIN = "[HoloQuiz] Warning: Minimum Requirement for %s cannot be lower than 1!";
    private static final String WARNING_INVALID_CONFIG = "[HoloQuiz] Warning: The Value %s for %s is invalid!" ;
    private static final String EASTER_EGG_EXTRA_SASS = " What sort of day is %s anyway?";

    private final String pluginPrefix;
    private final boolean collectRewardOnJoin;
    private final boolean easterEggsEnabled;
    private final boolean enableOnStart;

    private final int leaderboardSize;
    private final int leaderboardMinReq;

    private final boolean SRTS_useWhitelist;
    private final List<String> SRTS_WorldList; //Expect list to be small.

    private final String gameMode;
    private final int interval;
    private final int intervalCheck;
    private final int revealAnswerDelay;
    private final int QuestionCooldownLength;
    private final RewardsHandler.MinTimeCheatDetector minTimeCheatDetector;
    private final RewardsHandler.MinSDCheatDetector minSDCheatDetector;
    private final int correctAnswerMessageLoc; // -1: Disabled. 0:TitleMsg. 1:ActionBar

    private final int mathRange;
    private final String mathDistribution;
    private final boolean mathDivisorLimit;
    private final int mathOperationLimit;
    private final boolean mathChaosMode;
    private final String mathQuestionColour;
    private final String mathDifficulty;

    private final ZoneId timezoneOffset;
    private final int weeklyResetDay;
    private final ContestInfo dailyContest;
    private final ContestInfo weeklyContest;
    private final ContestInfo monthlyContest;

    public ConfigFile(JavaPlugin plugin, String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        FileConfiguration configs = YamlConfiguration.loadConfiguration(configFile);

        this.pluginPrefix = configs.getString("PluginPrefix");
        this.collectRewardOnJoin = configs.getBoolean("CollectRewardsOnJoin");

        this.interval = configs.getInt("Interval");
        this.intervalCheck = configs.getInt("IntervalCheck");
        this.correctAnswerMessageLoc = parseCorrectAnswerMsgLoc(configs);
        this.revealAnswerDelay = configs.getInt("RevealAnswerDelay");
        this.leaderboardSize = configs.getInt("LeaderboardSize");
        this.leaderboardMinReq = configs.getInt("LeaderboardMinQuestionsNeeded");
        this.easterEggsEnabled = configs.getBoolean("EasterEggs");
        this.gameMode = configs.getString("GameMode");
        this.enableOnStart = configs.getBoolean("EnableOnStart");
        this.QuestionCooldownLength = configs.getInt("QuestionCooldown");

        ConfigurationSection cheatSection = configs.getConfigurationSection("Cheats");
        ConfigurationSection minTimeSection = cheatSection.getConfigurationSection("MinTimeChecker");
        boolean isEnabled_MT = minTimeSection.getBoolean("Checker");
        int limit_MT = (int) (minTimeSection.getDouble("CheatingTimer") * 1000);
        boolean countAsCorrect_MT = minTimeSection.getBoolean("CountAsCorrect");
        List<String> cheatingCommands_MT = minTimeSection.getStringList("CommandToPerform");
        this.minTimeCheatDetector = new RewardsHandler.MinTimeCheatDetector(isEnabled_MT, limit_MT, countAsCorrect_MT, cheatingCommands_MT);
        ConfigurationSection consistencySection = cheatSection.getConfigurationSection("ConsistencyChecker");
        boolean isEnabled_SD = consistencySection.getBoolean("Checker");
        int numOfAnswers_SD = consistencySection.getInt("NumberOfAnswers");
        double limit_SD = consistencySection.getDouble("AcceptableSD");
        boolean countAsCorrect_SD = consistencySection.getBoolean("CountAsCorrect");
        List<String> cheatingCommands_SD = consistencySection.getStringList("CommandToPerform");
        this.minSDCheatDetector = new RewardsHandler.MinSDCheatDetector(isEnabled_SD, numOfAnswers_SD, limit_SD, countAsCorrect_SD, cheatingCommands_SD);

        ConfigurationSection mathSection = configs.getConfigurationSection("QuickMath");
        this.mathRange = mathSection.getInt("MathRange");
        this.mathDistribution = mathSection.getString("Distribution");
        this.mathDivisorLimit = mathSection.getBoolean("DivisorLimit");
        this.mathOperationLimit = mathSection.getInt("OperationsLimit");
        this.mathChaosMode = mathSection.getBoolean("ChaosMode");
        this.mathQuestionColour = mathSection.getString("QuestionColour");
        this.mathDifficulty = mathSection.getString("MathDifficulty");

        ConfigurationSection contestSection = configs.getConfigurationSection("Contests");
        this.weeklyResetDay = parseStartDay(contestSection);
        this.timezoneOffset = parseTimeZone(contestSection);
        this.dailyContest = parseContestConfig(contestSection, "Daily", 0);
        this.weeklyContest = parseContestConfig(contestSection, "Weekly",  1);
        this.monthlyContest = parseContestConfig(contestSection, "Monthly",  2);

        this.SRTS_useWhitelist = parseSRTSListType(configs);
        this.SRTS_WorldList = configs.getStringList("SRTS_WorldList");
        if(this.SRTS_useWhitelist && this.SRTS_WorldList.isEmpty()) {
            Bukkit.getLogger().info(WARNING_SRTS_EMPTY_WHITELIST);
        }
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

    public ContestInfo getDailyContestConfig() {
        return dailyContest;
    }

    public ContestInfo getWeeklyContestConfig()  {
        return weeklyContest;
    }

    public ContestInfo getMonthlyContestConfig()  {
        return monthlyContest;
    }

    public int getWeeklyResetDay() {
        return weeklyResetDay;
    }

    public ZoneId getTimezoneOffset() {
        return timezoneOffset;
    }

    public int getIntervalCheck() {
        return intervalCheck;
    }

    public int getQuestionCooldownLength() {
        return QuestionCooldownLength;
    }

    public int getRevealAnswerDelay() {
        return revealAnswerDelay;
    }

    public boolean isSRTS_useWhitelist() {
        return SRTS_useWhitelist;
    }

    public List<String> getSRTS_WorldList() {
        return SRTS_WorldList;
    }

    public RewardsHandler.MinTimeCheatDetector getMinTimeCheatDetector() {
        return minTimeCheatDetector;
    }

    public RewardsHandler.MinSDCheatDetector getMinSDCheatDetector() {
        return minSDCheatDetector;
    }

    public boolean isCollectRewardOnJoin() {
        return collectRewardOnJoin;
    }

    public int getCorrectAnswerMessageLoc() {
        return correctAnswerMessageLoc;
    }

    private int parseCorrectAnswerMsgLoc(FileConfiguration configs) {
        String location = configs.getString("CorrectAnswerMessageLoc", "TitleMsg");
        switch (location) {
            case "TitleMsg":
                return 0;
            case "ActionBar":
                return 1;
            case "Disabled":
                return -1;
        }
        String logMessage = String.format(WARNING_INVALID_CONFIG, location, "CorrectAnswerMsgLoc");
        Bukkit.getLogger().info(logMessage);
        return 0;
    }

    private ZoneId parseTimeZone(ConfigurationSection section) {
        String timeZone = section.getString("TimeZone", "GMT+0");
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeZone);
        } catch (Exception e) {
            String logMessage = String.format(WARNING_INVALID_CONFIG, timeZone, "TimeZone");
            Bukkit.getLogger().info(logMessage);
            zoneId = ZoneId.of("GMT+0");
        }
        return zoneId;
    }

    private int parseStartDay(ConfigurationSection section) {
        String day = section.getString("WeeklyResetDay", "Monday");
        switch (day) {
            case "Monday":
                return 1;
            case "Tuesday":
                return 2;
            case "Wednesday":
                return 3;
            case "Thursday":
                return 4;
            case "Friday":
                return 5;
            case "Saturday":
                return 6;
            case "Sunday":
                return 7;
        }
        String logMessage = String.format(WARNING_INVALID_CONFIG, day, "WeeklyResetDay") + String.format(EASTER_EGG_EXTRA_SASS, day);
        Bukkit.getLogger().info(logMessage);
        return 1;
    }

    private ContestInfo parseContestConfig(ConfigurationSection contestSection, String key, int code) {
        ConfigurationSection section = contestSection.getConfigurationSection(key);
        if(section == null) {
            String logMessage = String.format(ERROR_MISSING_CONFIG, key);
            Bukkit.getLogger().info(logMessage);
            return new ContestInfo(code, false, false, false, false, 0, 0);
        }

        boolean mostEnabled = section.getBoolean("Top", false);
        boolean fastestEnabled = section.getBoolean("Fastest", false);
        boolean bestAvgEnabled = section.getBoolean("BestAvg", false);
        boolean bestXEnabled = section.getBoolean("BestX", false);
        int bestAvgMinReq = section.getInt("BestAvgMinReq", 1);
        int bestXMinReq = section.getInt("BestXMinReq", 1);
        if(bestAvgMinReq < 1 && bestAvgEnabled) {
            String logMessage = String.format(WARNING_CONTEST_INVALID_MIN, "BestAvgMinReq");
            Bukkit.getLogger().info(logMessage);
            bestAvgEnabled = false;
        }
        if(bestXMinReq < 1 && bestXEnabled) {
            String logMessage = String.format(WARNING_CONTEST_INVALID_MIN, "BestXMinReq");
            Bukkit.getLogger().info(logMessage);
            bestXEnabled = false;
        }
        return new ContestInfo(code, mostEnabled, fastestEnabled, bestAvgEnabled, bestXEnabled, bestAvgMinReq, bestXMinReq);
    }

    private boolean parseSRTSListType(FileConfiguration configs) {
        String listType = configs.getString("SRTS_listType", "Whitelist");
        switch (listType) {
            case "Whitelist":
                return true;
            case "Blacklist":
                return false;
        }
        String logMessage = String.format(WARNING_INVALID_CONFIG, listType, "SRTS_listType");
        Bukkit.getLogger().info(logMessage);
        return false;
    }
}
