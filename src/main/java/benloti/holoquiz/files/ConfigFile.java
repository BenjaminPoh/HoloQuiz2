package benloti.holoquiz.files;

import benloti.holoquiz.games.MinSDCheatDetector;
import benloti.holoquiz.games.MinTimeCheatDetector;
import benloti.holoquiz.structs.ContestInfo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ConfigFile {

    private static final String WARNING_SRTS_EMPTY_WHITELIST = "[HoloQuiz] Warning: SRTS uses an empty Whitelist - No one can claim reward items!" ;
    private static final String WARNING_CONTEST_INVALID_MIN = "[HoloQuiz] Warning: Minimum Requirement for %s cannot be lower than 1!";
    private static final String WARNING_INVALID_CONFIG = "[HoloQuiz] Warning: The Value %s for %s is invalid!" ;
    private static final String EASTER_EGG_EXTRA_SASS = " What sort of day is %s anyway?";
    public static final String ERROR_CUSTOM_CONTEST_FAILED_TO_LOAD = "[HoloQuiz] Error: Failed to load contest of name %s";

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
    private final MinTimeCheatDetector minTimeCheatDetector;
    private final MinSDCheatDetector minSDCheatDetector;
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
    private final ArrayList<ContestInfo> customContests;

    public ConfigFile(JavaPlugin plugin, ConfigLoader configLoader, String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        FileConfiguration configs = YamlConfiguration.loadConfiguration(configFile);

        this.pluginPrefix = configLoader.getString(configs,"PluginPrefix", "&7[&bHoloQuiz&7] ");
        this.collectRewardOnJoin = configLoader.getBoolean(configs, "CollectRewardsOnJoin", true);
        this.easterEggsEnabled = configLoader.getBoolean(configs, "EasterEggs", false);
        this.enableOnStart = configLoader.getBoolean(configs, "EnableOnStart", false);

        this.gameMode = parseGameMode(configs, configLoader);
        this.interval = configLoader.getInt(configs, "Interval", 300);
        this.intervalCheck = configLoader.getInt(configs, "IntervalCheck", 10);
        this.revealAnswerDelay = configLoader.getInt(configs, "RevealAnswerDelay", 0);
        this.leaderboardSize = configLoader.getInt(configs, "LeaderboardSize", 10);
        this.leaderboardMinReq = configLoader.getInt(configs, "LeaderboardMinQuestionsNeeded", 0);
        this.correctAnswerMessageLoc = parseCorrectAnswerMsgLoc(configs, configLoader);
        this.QuestionCooldownLength = configLoader.getInt(configs, "QuestionCooldown", 5);

        ConfigurationSection cheatSection = configs.getConfigurationSection("Cheats");
        ConfigurationSection minTimeSection = cheatSection.getConfigurationSection("MinTimeChecker");
        boolean isEnabled_MT = configLoader.getBoolean(minTimeSection,"Checker", true);
        int limit_MT = (int) (configLoader.getDouble(minTimeSection,"CheatingTimer", 0.5) * 1000);
        boolean countAsCorrect_MT = configLoader.getBoolean(minTimeSection,"CountAsCorrect", false);
        List<String> cheatingCommands_MT = configLoader.getStringList(minTimeSection,"CommandToPerform");
        this.minTimeCheatDetector = new MinTimeCheatDetector(isEnabled_MT, limit_MT, countAsCorrect_MT, cheatingCommands_MT);
        ConfigurationSection consistencySection = cheatSection.getConfigurationSection("ConsistencyChecker");
        boolean isEnabled_SD = configLoader.getBoolean(consistencySection, "Checker", true);
        int numOfAnswers_SD = configLoader.getInt(consistencySection, "NumberOfAnswers", 5);
        double limit_SD = configLoader.getDouble(consistencySection, "AcceptableSD", 0.1);
        boolean countAsCorrect_SD = configLoader.getBoolean(consistencySection, "CountAsCorrect", true);
        List<String> cheatingCommands_SD = configLoader.getStringList(consistencySection, "CommandToPerform");
        this.minSDCheatDetector = new MinSDCheatDetector(isEnabled_SD, numOfAnswers_SD, limit_SD, countAsCorrect_SD, cheatingCommands_SD);

        ConfigurationSection mathSection = configs.getConfigurationSection("QuickMath");
        this.mathRange = configLoader.getInt(mathSection, "MathRange", 20);
        this.mathDistribution = configLoader.getString(mathSection, "Distribution", "Default");
        this.mathDivisorLimit = configLoader.getBoolean(mathSection, "DivisorLimit", true);
        this.mathOperationLimit = configLoader.getInt(mathSection, "OperationsLimit", 4);
        this.mathChaosMode = configLoader.getBoolean(mathSection, "ChaosMode", false);
        this.mathQuestionColour = configLoader.getString(mathSection, "QuestionColour", "&6");
        this.mathDifficulty = configLoader.getString(mathSection, "MathDifficulty", "Normal");

        ConfigurationSection contestSection = configs.getConfigurationSection("Contests");
        this.weeklyResetDay = parseStartDay(contestSection, configLoader);
        this.timezoneOffset = parseTimeZone(contestSection, configLoader);
        this.dailyContest = parseContestConfig(configLoader, contestSection, "Daily", 0);
        this.weeklyContest = parseContestConfig(configLoader, contestSection, "Weekly",  1);
        this.monthlyContest = parseContestConfig(configLoader, contestSection, "Monthly",  2);
        this.customContests = parseCustomContestConfig(configLoader, contestSection);

        this.SRTS_useWhitelist = parseSRTSListType(configs, configLoader);
        this.SRTS_WorldList = configLoader.getStringList(configs, "SRTS_WorldList");
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

    public ArrayList<ContestInfo> getCustomContests() {
        return customContests;
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

    public MinTimeCheatDetector getMinTimeCheatDetector() {
        return minTimeCheatDetector;
    }

    public MinSDCheatDetector getMinSDCheatDetector() {
        return minSDCheatDetector;
    }

    public boolean isCollectRewardOnJoin() {
        return collectRewardOnJoin;
    }

    public int getCorrectAnswerMessageLoc() {
        return correctAnswerMessageLoc;
    }

    private String parseGameMode(FileConfiguration configs, ConfigLoader configLoader) {
        String gameMode = configLoader.getString(configs, "GameMode", "Trivia");
        if(!gameMode.equals("Trivia") && !gameMode.equals("Math")) {
            String logMessage = String.format(WARNING_INVALID_CONFIG, gameMode, "GameMode");
            Bukkit.getLogger().info(logMessage);
            return "Trivia";
        }
        return gameMode;
    }

    private int parseCorrectAnswerMsgLoc(FileConfiguration configs, ConfigLoader configLoader) {
        String location = configLoader.getString(configs, "CorrectAnswerMessageLoc", "TitleMsg");
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

    private ZoneId parseTimeZone(ConfigurationSection section, ConfigLoader configLoader) {
        String timeZone = configLoader.getString(section, "TimeZone", "GMT+0");
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

    private int parseStartDay(ConfigurationSection section, ConfigLoader configLoader) {
        String day = configLoader.getString(section, "WeeklyResetDay", "Monday");
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

    private boolean parseSRTSListType(FileConfiguration configs, ConfigLoader configLoader) {
        String listType = configLoader.getString(configs, "SRTS_listType", "Whitelist");
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

    private ArrayList<ContestInfo> parseCustomContestConfig(ConfigLoader configLoader, ConfigurationSection contestSection) {
        ConfigurationSection customContestSection = configLoader.getSection(contestSection, "Custom");
        ArrayList<ContestInfo> customContests = new ArrayList<>();
        if(customContestSection == null) {
            return customContests;
        }
        for(String subKey : customContestSection.getKeys(false)) {
            customContests.add(parseCustomContest(configLoader, customContestSection, subKey));
        }
        return customContests;
    }

    private ContestInfo parseContestConfig(ConfigLoader configLoader, ConfigurationSection contestSection, String key, int code) {
        ConfigurationSection section = configLoader.getSection(contestSection, key);
        if(section == null) {
            return new ContestInfo(code, false, false, false, false, 0, 0);
        }

        boolean mostEnabled = configLoader.getBoolean(section, "Top", false);
        boolean fastestEnabled = configLoader.getBoolean(section, "Fastest", false);
        boolean bestAvgEnabled = configLoader.getBoolean(section, "BestAvg", false);
        boolean bestXEnabled = configLoader.getBoolean(section, "BestX", false);
        int bestAvgMinReq = configLoader.getInt(section, "BestAvgMinReq", 1);
        int bestXMinReq = configLoader.getInt(section,"BestXMinReq", 1);
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

    private ContestInfo parseCustomContest(ConfigLoader configLoader, ConfigurationSection contestSection, String key) {
        ConfigurationSection section = configLoader.getSection(contestSection, key);
        if(section == null) {
            return new ContestInfo(false, false, false, false, 0, 0, 0, 0, key, "");
        }

        boolean mostEnabled = configLoader.getBoolean(section, "Top", false);
        boolean fastestEnabled = configLoader.getBoolean(section, "Fastest", false);
        boolean bestAvgEnabled = configLoader.getBoolean(section, "BestAvg", false);
        boolean bestXEnabled = configLoader.getBoolean(section, "BestX", false);
        int bestAvgMinReq = configLoader.getInt(section, "BestAvgMinReq", 1);
        int bestXMinReq = configLoader.getInt(section,"BestXMinReq", 1);
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
        long startTimestamp = configLoader.getLong(section, "StartTimestamp", 0);
        long endTimestamp = configLoader.getLong(section, "EndTimestamp", 0);
        String rewardCategory = configLoader.getString(section, "RewardCategory", "");
        if(startTimestamp == 0 || endTimestamp == 0 || endTimestamp <= startTimestamp || rewardCategory.isEmpty()) {
            Bukkit.getLogger().info(String.format(ERROR_CUSTOM_CONTEST_FAILED_TO_LOAD,key));
            return new ContestInfo(false, false, false, false, 0, 0, 0, 0, key, "");
        }
        return new ContestInfo(mostEnabled, fastestEnabled, bestAvgEnabled, bestXEnabled, bestAvgMinReq, bestXMinReq,
                startTimestamp, endTimestamp, key, rewardCategory);
    }

}
