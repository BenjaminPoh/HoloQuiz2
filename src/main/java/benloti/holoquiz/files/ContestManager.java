package benloti.holoquiz.files;

import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.games.RewardsHandler;
import benloti.holoquiz.structs.ContestInfo;

import benloti.holoquiz.structs.ContestRewardTier;
import benloti.holoquiz.structs.ContestWinner;
import benloti.holoquiz.structs.PlayerData;
import org.bukkit.Bukkit;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;

public class ContestManager {
    private final DatabaseManager databaseManager;
    private final RewardsHandler rewardsHandler;
    private final ZoneId zoneId;

    private final boolean dailyEnabled;
    private final boolean weeklyEnabled;
    private final String weeklyResetTime;
    private final boolean monthlyEnabled;
    private final boolean isMultipleWinsAllowed;

    private ContestInfo dailyContest = null;
    private ContestInfo weeklyContest = null;
    private ContestInfo monthlyContest = null;

    public ContestManager(DatabaseManager databaseManager, ConfigFile configFile,
                          ExternalFiles externalFiles, GameManager gameManager) {
        this.databaseManager = databaseManager;
        this.rewardsHandler = gameManager.getRewardsHandler();
        this.dailyEnabled = configFile.isDailyContest();
        this.weeklyEnabled = configFile.isWeeklyContest();
        this.monthlyEnabled = configFile.isMonthlyContest();
        this.weeklyResetTime = configFile.getWeeklyResetDay();
        this.isMultipleWinsAllowed = configFile.isMultipleContestPositionAllowed();
        String timeZone = configFile.getTimezoneOffset();
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeZone);
        } catch (Exception e) {
            Bukkit.getLogger().info("[HoloQuiz] Your TimeZone isn't valid! Defaulting to +0.");
            zoneId = ZoneId.of("GMT+0");
        }
        this.zoneId = zoneId;

        initialiseContestInfo(externalFiles, configFile);
    }

    public void checkEndedContests() {
        LocalDate todayDate = LocalDate.now(zoneId);
        long currTime = todayDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
        long dailyContestEndTime = dailyContest.getEndTime();
        if(dailyEnabled && currTime > dailyContestEndTime) {
            long dailyContestNewEndTime = dailyContestEndTime + 86400000;
            Map<String, ArrayList<PlayerData>> dailyContestWinners = databaseManager.executeContestEndedTasks
                    (dailyContest, dailyContestNewEndTime, isMultipleWinsAllowed);
            ArrayList<ContestWinner> contestWinners = parseContestWinners(dailyContestWinners, dailyContest);
            rewardsHandler.giveContestRewards(contestWinners, dailyContest);
            dailyContest.updateContestTimes(dailyContestEndTime ,dailyContestNewEndTime);
        }

        long weeklyContestEndTime = weeklyContest.getEndTime();
        if(weeklyEnabled && currTime > weeklyContestEndTime) {
            long weeklyContestNewEndTime = weeklyContestEndTime + 86400000 * 7;
            Map<String, ArrayList<PlayerData>> weeklyContestWinners = databaseManager.executeContestEndedTasks
                    (weeklyContest, weeklyContestNewEndTime, isMultipleWinsAllowed);
            ArrayList<ContestWinner> contestWinners = parseContestWinners(weeklyContestWinners, weeklyContest);
            rewardsHandler.giveContestRewards(contestWinners, weeklyContest);
            weeklyContest.updateContestTimes(weeklyContestEndTime, weeklyContestNewEndTime);
        }
        long monthlyContestEndTime = monthlyContest.getEndTime();
        if(monthlyEnabled && currTime > monthlyContestEndTime) {
            long monthlyContestNewEndTime = 0; //TODO
        }

    }

    private void initialiseContestInfo(ExternalFiles externalFiles, ConfigFile configFile) {
        ArrayList<ContestInfo> enabledContestList = new ArrayList<>();
        LocalDate todayDate = LocalDate.now(zoneId);
        long startTimestamp = todayDate.atStartOfDay(zoneId).toInstant().toEpochMilli();

        //Creates contests as though they don't exist.
        // All contest start time is assumed to be 00:00 of the day it is set to be enabled.
        if(dailyEnabled) {
            LocalDate endDate = todayDate.plusDays(1);
            long endTimestamp = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
            ArrayList<ContestRewardTier> topAnswerRewards = externalFiles.getContestRewardByCategory("DailyMost");
            ArrayList<ContestRewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory("DailyBestAvg");
            ArrayList<ContestRewardTier> fastestRewards = externalFiles.getContestRewardByCategory("DailyFastest");
            int dailyMin = configFile.getDailyMin();
            ContestInfo dailyContestInfo = new ContestInfo(startTimestamp, endTimestamp, "D", dailyMin,
                    topAnswerRewards, bestAverageRewards, fastestRewards);
            enabledContestList.add(dailyContestInfo);
        }
        if(weeklyEnabled) {
            LocalDate endDate = todayDate.plusWeeks(1);
            long endTimestamp = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
            ArrayList<ContestRewardTier> topAnswerRewards = externalFiles.getContestRewardByCategory("WeeklyMost");
            ArrayList<ContestRewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory("WeeklyBestAvg");
            ArrayList<ContestRewardTier> fastestRewards = externalFiles.getContestRewardByCategory("WeeklyFastest");
            int weeklyMin = configFile.getWeeklyMin();
            ContestInfo weeklyContestInfo = new ContestInfo(startTimestamp, endTimestamp, "W", weeklyMin,
                    topAnswerRewards, bestAverageRewards, fastestRewards);
            enabledContestList.add(weeklyContestInfo);
        }
        if(monthlyEnabled) {
            LocalDate endDate = todayDate.plusMonths(1);
            long endTimestamp = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
            ArrayList<ContestRewardTier> topAnswerRewards = externalFiles.getContestRewardByCategory("MonthlyMost");
            ArrayList<ContestRewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory("MonthlyBestAvg");
            ArrayList<ContestRewardTier> fastestRewards = externalFiles.getContestRewardByCategory("MonthlyFastest");
            int monthlyMin = configFile.getMonthlyMin();
            ContestInfo monthlyContestInfo = new ContestInfo(startTimestamp, endTimestamp, "M", monthlyMin,
                    topAnswerRewards, bestAverageRewards, fastestRewards);
            enabledContestList.add(monthlyContestInfo);
        }

        ArrayList<ContestInfo> runningContests = databaseManager.updateOngoingContestInformation(enabledContestList);
        for (ContestInfo contestInfo : runningContests) {
            String code = contestInfo.getType();
            switch (code) {
            case "D":
                this.dailyContest = contestInfo;
                dailyContest.loadDates(zoneId);
                break;
            case "W":
                this.weeklyContest = contestInfo;
                weeklyContest.loadDates(zoneId);
                break;
            case "M":
                this.monthlyContest = contestInfo;
                monthlyContest.loadDates(zoneId);
                break;
            default:
                Bukkit.getLogger().info("[HolQuiz] Error! If you saw this, you messed with the database didn't you?");
                break;
            }
        }

        checkEndedContests();
    }

    private ArrayList<ContestWinner> parseContestWinners(
            Map<String, ArrayList<PlayerData>> contestWinnersData, ContestInfo contestInfo) {
        ArrayList<ContestWinner> contestWinners = new ArrayList<>();
        for(String contestCode : contestWinnersData.keySet()) {
            ArrayList<PlayerData> contestCategoryWinnersData = contestWinnersData.get(contestCode);
            ArrayList<ContestRewardTier> contestRewardTiers = contestInfo.getRewardByCategory(contestCode);
            int rewardTierIndex = -1;
            int rewardTierReps = 0;
            int position = 1;
            for(PlayerData winnerData : contestCategoryWinnersData) {
                if(rewardTierReps == 0) {
                    rewardTierIndex += 1;
                    rewardTierReps = contestRewardTiers.get(rewardTierIndex).getReps();
                }
                ContestWinner winner = new ContestWinner(contestRewardTiers.get(rewardTierIndex), winnerData, position);
                contestWinners.add(winner);
                rewardTierReps -= 1;
                position += 1;
            }
        }
        return contestWinners;
    }

}
