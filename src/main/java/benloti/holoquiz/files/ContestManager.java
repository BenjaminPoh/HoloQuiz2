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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ContestManager {
    private static final String LOG_CURRENT_TIME =  "[HoloQuiz] The set TimeZone is %s, giving a current date of %s (%d)";
    private static final String LOG_CREATED_NEW_CONTEST = "[HoloQuiz] Scheduled new %s Contest from %s (%d) to %s (%d)";
    private static final String LOG_DELETED_CONTEST = "[HoloQuiz] Removed old %s Contest that ends on %s (%d)";
    private static final String LOG_MESSAGE_CONTEST_ENDED = "[HoloQuiz] Contest from %s (%d) to %s (%d) ended!";

    private final DatabaseManager databaseManager;
    private final RewardsHandler rewardsHandler;
    private final ZoneId zoneId;

    private final ArrayList<ContestInfo> allContests;
    private final int totalEnabledSubcontests;
    private final Set<String> playersWithOpenGUI = new HashSet<>();
    private final int contestLeaderboardMaxSize;

    public ContestManager(DatabaseManager databaseManager, ConfigFile configFile,
                          ExternalFiles externalFiles, GameManager gameManager) {
        this.databaseManager = databaseManager;
        this.rewardsHandler = gameManager.getRewardsHandler();
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

        for (int i = 0; i < 3; i++) {
            ContestInfo currContest = allContests.get(i);
            if (currContest == null) {
                continue;
            }
            long endTime = currContest.getEndTime();
            if (currTime < endTime) {
                continue;
            }

            handleEndedContestTasks(currContest);
            currContest.updateTournamentDateToNextCycle(zoneId);
            databaseManager.updateRunningContestInfo(i, currContest.getStartTime(), currContest.getEndTime());
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
        //Fetch the timestamps of the regular contests that were saved
        ArrayList<Pair<Long, Long>> savedContestTimes = databaseManager.fetchSavedContests();
        //Creates contests as though they don't exist.
        ArrayList<ContestInfo> enabledContestList = initialiseContests(externalFiles, configFile, savedContestTimes);
        //Check for ended contests
        initialiseEndedContests(enabledContestList, savedContestTimes);
        return enabledContestList;
    }

    private ArrayList<ContestInfo> initialiseContests(ExternalFiles externalFiles, ConfigFile configFile, ArrayList<Pair<Long, Long>> savedContestTimes) {
        //Fetch Contest info from Config
        ContestInfo dailyContest = configFile.getDailyContestConfig();
        ContestInfo weeklyContest = configFile.getWeeklyContestConfig();
        ContestInfo monthlyContest = configFile.getMonthlyContestConfig();

        //Fetch Rewards for Contests
        loadRewardsForContest(externalFiles, dailyContest.getTypeString(), dailyContest);
        loadRewardsForContest(externalFiles, weeklyContest.getTypeString(), weeklyContest);
        loadRewardsForContest(externalFiles, monthlyContest.getTypeString(), monthlyContest);

        //All contest start time is assumed to be 00:00 of the day it is set to be enabled.
        //Load their dates based on the current startDate
        LocalDate startDate = LocalDate.now(zoneId);
        int intendedStartDay = configFile.getWeeklyResetDay();
        dailyContest.generateDailyIntervalForContest(zoneId, startDate);
        weeklyContest.generateWeeklyIntervalForContest(zoneId, startDate, intendedStartDay);
        monthlyContest.generateMonthlyIntervalForContest(zoneId, startDate);

        //Set contest
        ArrayList<ContestInfo> enabledContestList = new ArrayList<>(Arrays.asList(null, null, null));
        enabledContestList.set(0, dailyContest);
        enabledContestList.set(1, weeklyContest);
        enabledContestList.set(2, monthlyContest);

        for (int i = 0; i < 3; i++) {
            ContestInfo currentEnabledContest = enabledContestList.get(i);
            Pair<Long, Long> currentSavedContestTimes = savedContestTimes.get(i);
            updateContestInfo(currentEnabledContest, currentSavedContestTimes, i);
        }

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


    private void updateContestInfo(ContestInfo currentEnabledContest, Pair<Long, Long> currentSavedContestTimes, int i) {
        if (currentEnabledContest != null && currentSavedContestTimes == null) {
            //Add new contest to db and the return list
            databaseManager.createOngoingContest(currentEnabledContest);
            String logMessage = String.format(LOG_CREATED_NEW_CONTEST, currentEnabledContest.getContestName(),
                    currentEnabledContest.getStartDate(), currentEnabledContest.getStartTime(),
                    currentEnabledContest.getEndDate(), currentEnabledContest.getEndTime()); //How long is this going
            Bukkit.getLogger().info(logMessage);
        } else if (currentEnabledContest == null && currentSavedContestTimes != null) {
            //Delete disabled contest.
            databaseManager.deleteOngoingContest(i);
            long endingTimestamp = currentSavedContestTimes.getRight();
            ZonedDateTime dateTime = fetchDateTimeByTimestamp(endingTimestamp);
            String contestType = getContestTypeByID(i);
            String logMessage = String.format(LOG_DELETED_CONTEST, contestType, dateTime, endingTimestamp);
            Bukkit.getLogger().info(logMessage);
        }
    }

    private ZonedDateTime fetchDateTimeByTimestamp(long time) {
        return Instant.ofEpochMilli(time).atZone(zoneId);
    }

    private String getContestTypeByID(int i) {
        if (i == 0) {
            return "daily";
        }
        if (i == 1) {
            return "weekly";
        }
        if (i == 2) {
            return "monthly";
        }
        return "some non-existent value because you edited the db you lil punk";
    }

    private void initialiseEndedContests(ArrayList<ContestInfo> enabledContests, ArrayList<Pair<Long, Long>> savedContests) {
        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        long currTime = currentDateTime.toInstant().toEpochMilli();

        //Handle Regular Contests
        for (int i = 0; i < 3; i++) {
            Pair<Long, Long> savedContestTimes = savedContests.get(i);
            if (savedContestTimes == null || currTime < savedContestTimes.getRight()) {
                continue;
            }

            //A Saved Contest expired. Load Config and Rewards from
            ContestInfo currContest = enabledContests.get(i);
            ContestInfo savedContest = new ContestInfo(currContest, savedContestTimes.getLeft(), savedContestTimes.getRight(), zoneId);
            handleEndedContestTasks(savedContest);
            databaseManager.updateRunningContestInfo(i, currContest.getStartTime(), currContest.getEndTime());
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

    private void handleEndedContestTasks(ContestInfo oldContest) {
        logEndedContest(oldContest);
        ArrayList<ArrayList<PlayerContestStats>> allContestWinners = databaseManager.fetchContestWinners(oldContest);
        databaseManager.logContestWinners(allContestWinners, oldContest);
        ArrayList<ContestWinner> contestWinners = parseContestWinners(allContestWinners, oldContest);
        rewardsHandler.giveContestRewards(contestWinners, oldContest);
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

