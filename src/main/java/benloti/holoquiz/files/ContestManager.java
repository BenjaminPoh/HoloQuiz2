package benloti.holoquiz.files;

import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.games.RewardsHandler;
import benloti.holoquiz.structs.*;

import org.bukkit.Bukkit;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ContestManager {
    private static final String LOG_CURRENT_TIME =  "[HoloQuiz] The set TimeZone is %s, giving a current date of %s (%d)";
    private static final String LOG_CREATED_NEW_CONTEST = "[HoloQuiz] Scheduled new %s Contest from %s (%d) to %s (%d)";
    private static final String LOG_DELETED_CONTEST = "[HoloQuiz] Removed old %s Contest that ends on %s (%d)";
    private static final String LOG_MESSAGE_CONTEST_ENDED = "[HoloQuiz] Contest from %s (%d) to %s (%d) ended!";
    private static final String ERROR_MESSAGE_IMPOSSIBLE_CODE = "[HoloQuiz] Dev Error, Contest %s has Type Code %d";


    private final DatabaseManager databaseManager;
    private final RewardsHandler rewardsHandler;
    private final ZoneId zoneId;
    private final ExternalFiles externalFiles;

    private final ArrayList<ContestInfo> allContests;
    private final int totalEnabledSubcontests;
    private final Set<String> playersWithOpenGUI = new HashSet<>();
    private final int contestLeaderboardMaxSize;

    public ContestManager(DatabaseManager databaseManager, ConfigFile configFile,
                          ExternalFiles externalFiles, GameManager gameManager) {
        this.databaseManager = databaseManager;
        this.rewardsHandler = gameManager.getRewardsHandler();
        this.externalFiles = externalFiles;
        this.zoneId = configFile.getTimezoneOffset();

        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        long currentTimestamp = currentDateTime.toInstant().toEpochMilli();
        String logMessage = String.format(LOG_CURRENT_TIME, zoneId, currentDateTime, currentTimestamp);
        Bukkit.getLogger().info(logMessage);

        this.allContests = initialiseContestInfo(externalFiles, configFile);
        this.totalEnabledSubcontests = countEnabledContests();
        this.contestLeaderboardMaxSize = configFile.getContestLeaderboardMaxSize();
    }

    public void updateContestsStatus() {
        long currTime = ZonedDateTime.now(zoneId).toInstant().toEpochMilli();
        for (ContestInfo contest : allContests) {
            if (contest == null) {
                continue;
            }
            long endTime = contest.getEndTime();
            if (currTime < endTime) {
                continue;
            }

            //Contest ended!
            handleEndedContestTasks(contest);
            if (contest.getTypeCode() < 3) {
                contest.updateTournamentDateToNextCycle(zoneId);
                externalFiles.updateRegularContestTimestamp(contest.getTypeString(), contest.getStartTime(), contest.getEndTime());
            }
        }
    }

    public ContestProgressGUI fetchPlayerContestStatus(String playerName, String playerUUID, UserInterface userInterface) {
        if (this.playersWithOpenGUI.contains(playerName)) {
            Bukkit.getLogger().info("[HoloQuiz] BUG: A player managed to open the Contests GUI while having it open.");
        } else {
            this.playersWithOpenGUI.add(playerName);
        }
        ContestProgressGUI contestProgressGUI = new ContestProgressGUI(this, playerName, userInterface);
        for (ContestInfo contest : this.allContests) {
            if (contest == null) {
                continue;
            }
            ArrayList<ArrayList<PlayerContestStats>> allContestWinners = databaseManager.fetchContestWinners(contest);
            PlayerContestStats targetPlayerPlacement = databaseManager.fetchPlayerContestPlacement(contest, playerName, playerUUID);
            contestProgressGUI.addContestInfo(contest, allContestWinners, targetPlayerPlacement);
        }
        return contestProgressGUI;
    }

    public void updateClosedContestGUI(String playerName) {
        if (!this.playersWithOpenGUI.contains(playerName)) {
            Bukkit.getLogger().info("[HoloQuiz] BUG: A player managed to close the Contests GUI while not having it open.");
            return;
        }
        this.playersWithOpenGUI.remove(playerName);
    }

    public int getTotalEnabledSubcontests() {
        return totalEnabledSubcontests;
    }

    public int getContestLeaderboardMaxSize() {
        return contestLeaderboardMaxSize;
    }

    private ArrayList<ContestInfo> initialiseContestInfo(ExternalFiles externalFiles, ConfigFile configFile) {
        ArrayList<ContestInfo> enabledContestList = initialiseContests(externalFiles, configFile);
        initialiseEndedContests(enabledContestList, externalFiles);
        return enabledContestList;
    }

    private ArrayList<ContestInfo> initialiseContests(ExternalFiles externalFiles, ConfigFile configFile) {
        //Fetch Contest info from Config
        ContestInfo dailyContest = configFile.getDailyContestConfig();
        ContestInfo weeklyContest = configFile.getWeeklyContestConfig();
        ContestInfo monthlyContest = configFile.getMonthlyContestConfig();

        //Fetch Rewards for Contests
        loadRewardsForContest(externalFiles, dailyContest.getTypeString(), dailyContest);
        loadRewardsForContest(externalFiles, weeklyContest.getTypeString(), weeklyContest);
        loadRewardsForContest(externalFiles, monthlyContest.getTypeString(), monthlyContest);

        //Add to List
        ArrayList<ContestInfo> enabledContestList = new ArrayList<>();
        updateContestInfo(dailyContest, enabledContestList, externalFiles);
        updateContestInfo(weeklyContest, enabledContestList, externalFiles);
        updateContestInfo(monthlyContest, enabledContestList, externalFiles);

        ArrayList<ContestInfo> customContests = configFile.getCustomContests();
        for(ContestInfo contest : customContests) {
            loadRewardsForContest(externalFiles, contest.getRewardCategoryName(), contest);
            contest.updateTimes(zoneId);
            enabledContestList.add(contest);
        }

        //Update the Database for removed and new contests, and savedContestList with the latest rewards
        return enabledContestList;
    }

    private void loadRewardsForContest(ExternalFiles externalFiles, String type, ContestInfo contest) {
        ArrayList<ContestRewardTier> mostAnswerRewards = externalFiles.getContestRewardByCategory(type + "Most", contest.isMostAnswerContestEnabled());
        ArrayList<ContestRewardTier> fastestRewards = externalFiles.getContestRewardByCategory(type + "Fastest", contest.isFastestAnswerContestEnabled());
        ArrayList<ContestRewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory(type + "BestAvg", contest.isBestAvgContestEnabled());
        ArrayList<ContestRewardTier> bestXRewards = externalFiles.getContestRewardByCategory(type + "BestX", contest.isBestXContestEnabled());
        contest.updateRewards(mostAnswerRewards, fastestRewards, bestAverageRewards, bestXRewards);
    }


    private void updateContestInfo(ContestInfo contest, ArrayList<ContestInfo> contestList, ExternalFiles externalFiles) {
        //Add contest to List if it is enabled
        if(contest.isContestEnabled()) {
            contestList.add(contest);
        }

        //Set disabled contest timestamp to 0 if it's timestamp isn't 0
        if(!contest.isContestEnabled() && (contest.getStartTime() != 0 || contest.getEndTime() != 0 )){
            externalFiles.updateRegularContestTimestamp(contest.getContestName(), 0 ,0);

            //Logging Purposes
            long endingTimestamp = contest.getEndTime();
            ZonedDateTime dateTime = fetchDateTimeByTimestamp(endingTimestamp);
            String contestType = contest.getTypeString();
            String logMessage = String.format(LOG_DELETED_CONTEST, contestType, dateTime, endingTimestamp);
            Bukkit.getLogger().info(logMessage);
        }

        //If contest is enabled and timestamp is 0, update with new timestamp
        if(contest.isContestEnabled() && (contest.getStartTime() == 0 || contest.getEndTime() == 0 )){
            generateIntervalForContest(contest, externalFiles);
            externalFiles.updateRegularContestTimestamp(contest.getContestName(), contest.getStartTime(), contest.getEndTime());

            //Logging Purposes
            String logMessage = String.format(LOG_CREATED_NEW_CONTEST, contest.getContestName(),
                    contest.getStartDate(), contest.getStartTime(),
                    contest.getEndDate(), contest.getEndTime());
            Bukkit.getLogger().info(logMessage);
        }
    }

    private void generateIntervalForContest(ContestInfo contest, ExternalFiles externalFiles) {
        //All contest start time is assumed to be 00:00 of the day it is set to be enabled.
        //Load their dates based on the current startDate
        LocalDate startDate = LocalDate.now(zoneId);
        if(contest.getTypeCode() == 0) {
            contest.generateDailyIntervalForContest(zoneId, startDate);
            return;
        }
        if(contest.getTypeCode() == 1) {
            int intendedStartDay = externalFiles.getConfigFile().getWeeklyResetDay(); //Sacrilegious
            contest.generateWeeklyIntervalForContest(zoneId, startDate, intendedStartDay);
            return;
        }
        if(contest.getTypeCode() == 2) {
            contest.generateMonthlyIntervalForContest(zoneId, startDate);
            return;
        }
        Bukkit.getLogger().info(String.format(ERROR_MESSAGE_IMPOSSIBLE_CODE, contest.getContestName(),  contest.getTypeCode()));
    }

    private ZonedDateTime fetchDateTimeByTimestamp(long time) {
        return Instant.ofEpochMilli(time).atZone(zoneId);
    }

    private void initialiseEndedContests(ArrayList<ContestInfo> enabledContests, ExternalFiles externalFiles) {
        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        long currTime = currentDateTime.toInstant().toEpochMilli();

        //Handle Regular Contests
        for (int i = 0; i < 3; i++) {
            ContestInfo contest = enabledContests.get(i);
            if (currTime < contest.getEndTime()) {
                continue;
            }

            //A Saved Contest expired. Load Config and Rewards from
            handleEndedContestTasks(contest);
            generateIntervalForContest(contest, externalFiles);
            externalFiles.updateRegularContestTimestamp(contest.getContestName(), contest.getStartTime(), contest.getEndTime());
        }

        //Handle Custom Contests
        for (int i = 3; i < enabledContests.size(); i++) {
            ContestInfo customContest = enabledContests.get(i);
            if (currTime < customContest.getEndTime()) {
                continue;
            }
            handleEndedContestTasks(customContest);
        }
    }

    //TODO Fix this absolute garbage spaghetti
    private void handleEndedContestTasks(ContestInfo oldContest) {
        logEndedContest(oldContest);
        ArrayList<ArrayList<PlayerContestStats>> allContestWinners = databaseManager.fetchContestWinners(oldContest);
        databaseManager.logContestWinners(allContestWinners, oldContest);
        ArrayList<ContestWinner> contestWinners = parseContestWinners(allContestWinners, oldContest);
        rewardsHandler.giveContestRewards(contestWinners, oldContest);
        if(oldContest.getTypeCode() == 3){
            externalFiles.setEndedCustomContest(oldContest.getContestName());
            allContests.remove(oldContest);
        }
    }

    private void logEndedContest(ContestInfo savedContest) {
        LocalDate startDate = savedContest.getStartDate();
        LocalDate endDate = savedContest.getEndDate();
        long startTime = savedContest.getStartTime();
        long endTime = savedContest.getEndTime();
        String logMessage = String.format(LOG_MESSAGE_CONTEST_ENDED, startDate, startTime, endDate, endTime);
        Bukkit.getLogger().info(logMessage);
    }

    private ArrayList<ContestWinner> parseContestWinners
            (ArrayList<ArrayList<PlayerContestStats>> contestWinnersData, ContestInfo contestInfo) {
        ArrayList<ContestWinner> contestWinners = new ArrayList<>();
        for (int i = 0; i < contestWinnersData.size(); i++) {
            ArrayList<PlayerContestStats> contestCategoryWinnersData = contestWinnersData.get(i);
            ArrayList<ContestRewardTier> contestRewardTiers = contestInfo.getRewardByCategory(i);
            int limit = Math.min(contestCategoryWinnersData.size(), contestRewardTiers.size());
            for (int j = 0; j < limit; j++) {
                ContestWinner winner = new ContestWinner(contestRewardTiers.get(j), contestCategoryWinnersData.get(j), j + 1);
                contestWinners.add(winner);
            }
        }
        return contestWinners;
    }

    private int countEnabledContests() {
        int count = 0;
        for (ContestInfo contestInfo : allContests) {
            if (contestInfo.isMostAnswerContestEnabled() && !contestInfo.getRewardByCategory(0).isEmpty()) {
                count++;
            }
            if (contestInfo.isFastestAnswerContestEnabled() && !contestInfo.getRewardByCategory(1).isEmpty()) {
                count++;
            }
            if (contestInfo.isBestAvgContestEnabled() && !contestInfo.getRewardByCategory(2).isEmpty()) {
                count++;
            }
            if (contestInfo.isBestXContestEnabled() && !contestInfo.getRewardByCategory(3).isEmpty()) {
                count++;
            }

        }
        return count;
    }
}

