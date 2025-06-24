package benloti.holoquiz.files;

import benloti.holoquiz.structs.MinSDCheatDetector;
import benloti.holoquiz.structs.MinTimeCheatDetector;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.ZoneId;
import java.util.List;

public class ConfigFile {
    private static final String WARNING_SRTS_EMPTY_WHITELIST = "[HoloQuiz] Warning: SRTS uses an empty Whitelist - No one can claim reward items!" ;

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
    private final boolean dailyContest;
    private final boolean weeklyContest;
    private final boolean monthlyContest;
    private final int dailyMin;
    private final int weeklyMin;
    private final int monthlyMin;
    private final int weeklyResetDay;

    public ConfigFile(JavaPlugin plugin, String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        FileConfiguration configs = YamlConfiguration.loadConfiguration(configFile);

        this.pluginPrefix = configs.getString("PluginPrefix");
        this.collectRewardOnJoin = configs.getBoolean("CollectRewardsOnJoin");

        this.interval = configs.getInt("Interval");
        this.intervalCheck = configs.getInt("IntervalCheck");
        this.correctAnswerMessageLoc = parseCorrectAnswerMsgLoc(configs.getString("CorrectAnswerMessageLoc"));
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
        this.minTimeCheatDetector = new MinTimeCheatDetector(isEnabled_MT, limit_MT, countAsCorrect_MT, cheatingCommands_MT);
        ConfigurationSection consistencySection = cheatSection.getConfigurationSection("ConsistencyChecker");
        boolean isEnabled_SD = consistencySection.getBoolean("Checker");
        int numOfAnswers_SD = consistencySection.getInt("NumberOfAnswers");
        double limit_SD = consistencySection.getDouble("AcceptableSD");
        boolean countAsCorrect_SD = consistencySection.getBoolean("CountAsCorrect");
        List<String> cheatingCommands_SD = consistencySection.getStringList("CommandToPerform");
        this.minSDCheatDetector = new MinSDCheatDetector(isEnabled_SD, numOfAnswers_SD, limit_SD, countAsCorrect_SD, cheatingCommands_SD);

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
        this.weeklyResetDay = parseStartDay(contestSection.getString("WeeklyResetDay", "Monday"));
        this.dailyMin = contestSection.getInt("DailyAvgMin");
        this.weeklyMin = contestSection.getInt("WeeklyAvgMin");
        this.monthlyMin = contestSection.getInt("MonthlyAvgMin");
        this.timezoneOffset = parseTimeZone(contestSection.getString("TimeZone", "GMT+0"));

        this.SRTS_useWhitelist = configs.getBoolean("SRTS_useWhitelist");
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

    public boolean isDailyContest() {
        return dailyContest;
    }

    public boolean isWeeklyContest() {
        return weeklyContest;
    }

    public boolean isMonthlyContest() {
        return monthlyContest;
    }

    public int getWeeklyResetDay() {
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

    private ZoneId parseTimeZone(String timeZone) {
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeZone);
        } catch (Exception e) {
            Bukkit.getLogger().info("[HoloQuiz] Your TimeZone isn't valid! Defaulting to +0.");
            zoneId = ZoneId.of("GMT+0");
        }
        return zoneId;
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

    private int parseStartDay(String day) {
        if(day.equals("Monday")) {
            return 1;
        }
        if(day.equals("Tuesday")) {
            return 2;
        }
        if(day.equals("Wednesday")) {
            return 3;
        }
        if(day.equals("Thursday")) {
            return 4;
        }
        if(day.equals("Friday")) {
            return 5;
        }
        if(day.equals("Saturday")) {
            return 6;
        }
        if(day.equals("Sunday")) {
            return 7;
        }
        Bukkit.getLogger().info("[HoloQuiz] Error: What kind of day is " + day + "?");
        return 1;
    }

    private int parseCorrectAnswerMsgLoc(String toko) {
        if(toko.equals("TitleMsg")) {
            return 0;
        }
        if(toko.equals("ActionBar")) {
            return 1;
        }
        if(toko.equals("Disabled")) {
            return -1;
        }
        Bukkit.getLogger().info("[HoloQuiz] Error: CorrectAnswerMsgLoc: " + toko + " is invalid");
        return -1;
    }

    public int getCorrectAnswerMessageLoc() {
        return correctAnswerMessageLoc;
    }
}
