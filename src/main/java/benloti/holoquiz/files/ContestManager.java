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
    private static final String LOG_CURRENT_TIME =
            "[HoloQuiz] The set TimeZone is %s, giving a current date of %s (%d)";
    private static final String LOG_CREATED_NEW_CONTEST =  "[HoloQuiz] Scheduled new %s Contest from %s (%d) to %s (%d)";
    private static final String LOG_DELETED_CONTEST =  "[HoloQuiz] Removed old %s Contest that ends on %s (%d)";
    private static final String LOG_MESSAGE_CONTEST_ENDED = "[HoloQuiz] Contest from %s to %s ended!";

    private final DatabaseManager databaseManager;
    private final RewardsHandler rewardsHandler;
    private final ZoneId zoneId;

    private final boolean dailyEnabled;
    private final boolean weeklyEnabled;
    private final boolean monthlyEnabled;

    private final ArrayList<ContestInfo> enabledContests;
    private final Set<String> playersWithOpenGUI = new HashSet<>();

    public ContestManager(DatabaseManager databaseManager, ConfigFile configFile,
                          ExternalFiles externalFiles, GameManager gameManager) {
        this.databaseManager = databaseManager;
        this.rewardsHandler = gameManager.getRewardsHandler();
        this.dailyEnabled = configFile.isDailyContest();
        this.weeklyEnabled = configFile.isWeeklyContest();
        this.monthlyEnabled = configFile.isMonthlyContest();
        this.zoneId = configFile.getTimezoneOffset();

        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        long currentTimestamp = currentDateTime.toInstant().toEpochMilli();
        String logMessage = String.format(LOG_CURRENT_TIME,zoneId, currentDateTime,currentTimestamp);
        Bukkit.getLogger().info(logMessage);

        this.enabledContests = initialiseContestInfo(externalFiles, configFile);
    }

    public void updateContestsStatus() {
        long currTime = ZonedDateTime.now(zoneId).toInstant().toEpochMilli();

        for(int i = 0; i < 3; i++) {
            ContestInfo currContest = enabledContests.get(i);
            long endTime = currContest.getEndTime();
            if(currTime < endTime) {
                continue;
            }
            handleEndedContestTasks(currContest);
            currContest.updateTournamentDateToNextCycle(zoneId);
            databaseManager.updateRunningContestInfo(i, currContest.getStartTime(), currContest.getEndTime());
        }
    }

    public ContestProgressGUI fetchPlayerContestStatus(String playerName, String playerUUID, UserInterface userInterface) {
        if(this.playersWithOpenGUI.contains(playerName)) {
            Bukkit.getLogger().info("[HoloQuiz] BUG: A player managed to open the Contests GUI while having it open.");
        } else {
            this.playersWithOpenGUI.add(playerName);
        }
        ContestProgressGUI contestProgressGUI = new ContestProgressGUI(this, playerName, userInterface);
        for (ContestInfo contest : this.enabledContests) {
            ArrayList<ArrayList<PlayerData>> allContestWinners = databaseManager.fetchContestWinners(contest);
            PlayerData targetPlayerPlacement = databaseManager.fetchPlayerContestPlacement(contest,playerName,playerUUID);
            contestProgressGUI.addInfo(contest, allContestWinners, targetPlayerPlacement);
        }
        return contestProgressGUI;
    }

    public void updateClosedContestGUI(String playerName) {
        if(!this.playersWithOpenGUI.contains(playerName)) {
            Bukkit.getLogger().info("[HoloQuiz] BUG: A player managed to close the Contests GUI while not having it open.");
            return;
        }
        this.playersWithOpenGUI.remove(playerName);
    }

    private ArrayList<ContestInfo> initialiseContestInfo(ExternalFiles externalFiles, ConfigFile configFile) {
        //Creates contests as though they don't exist.
        ArrayList<ContestInfo> enabledContestList = initialiseNewContests(externalFiles, configFile);
        //Fetch ongoing contests that were saved
        ArrayList<ContestInfo> savedContestList = databaseManager.fetchSavedContests();
        //Update the Database for removed and new contests
        updateContestDB(enabledContestList,savedContestList);
        //Check for ended contests
        initialiseEndedContests(enabledContestList,savedContestList);
        return enabledContestList;
    }

    private ArrayList<ContestInfo> initialiseNewContests(ExternalFiles externalFiles, ConfigFile configFile) {
        // All contest start time is assumed to be 00:00 of the day it is set to be enabled.
        ArrayList<ContestInfo> enabledContestList = new ArrayList<>(Arrays.asList(null,null,null));
        if(dailyEnabled) {
            LocalDate startDate = LocalDate.now(zoneId); //Also EndDate

            ArrayList<ContestRewardTier> topAnswerRewards = externalFiles.getContestRewardByCategory("DailyMost");
            ArrayList<ContestRewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory("DailyBestAvg");
            ArrayList<ContestRewardTier> fastestRewards = externalFiles.getContestRewardByCategory("DailyFastest");
            int dailyMin = configFile.getDailyMin();
            ContestInfo dailyContestInfo = new ContestInfo(zoneId,0, dailyMin,
                    topAnswerRewards, bestAverageRewards, fastestRewards, startDate, startDate);

            enabledContestList.set(0, dailyContestInfo);
        }
        if(weeklyEnabled) {
            LocalDate startDate = LocalDate.now(zoneId);
            int daysFromMonday = startDate.getDayOfWeek().getValue() - 1;
            startDate = startDate.minusDays(daysFromMonday);
            LocalDate endDate = startDate.plusWeeks(1);
            endDate = endDate.minusDays(1);

            ArrayList<ContestRewardTier> topAnswerRewards = externalFiles.getContestRewardByCategory("WeeklyMost");
            ArrayList<ContestRewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory("WeeklyBestAvg");
            ArrayList<ContestRewardTier> fastestRewards = externalFiles.getContestRewardByCategory("WeeklyFastest");
            int weeklyMin = configFile.getWeeklyMin();
            ContestInfo weeklyContestInfo = new ContestInfo(zoneId, 1, weeklyMin,
                    topAnswerRewards, bestAverageRewards, fastestRewards, startDate, endDate);

            enabledContestList.set(1, weeklyContestInfo);
        }
        if(monthlyEnabled) {
            LocalDate startDate = LocalDate.now(zoneId);
            int daysFromFirstDayOfMonth = startDate.getDayOfMonth() - 1;
            startDate = startDate.minusDays(daysFromFirstDayOfMonth);
            LocalDate endDate = startDate.plusMonths(1);
            endDate = endDate.minusDays(1);

            ArrayList<ContestRewardTier> topAnswerRewards = externalFiles.getContestRewardByCategory("MonthlyMost");
            ArrayList<ContestRewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory("MonthlyBestAvg");
            ArrayList<ContestRewardTier> fastestRewards = externalFiles.getContestRewardByCategory("MonthlyFastest");
            int monthlyMin = configFile.getMonthlyMin();
            ContestInfo monthlyContestInfo = new ContestInfo(zoneId, 2, monthlyMin,
                    topAnswerRewards, bestAverageRewards, fastestRewards, startDate, endDate);

            enabledContestList.set(2, monthlyContestInfo);
        }
        return enabledContestList;
    }

    private void updateContestDB(ArrayList<ContestInfo> enabledContests, ArrayList<ContestInfo> savedContests) {
        for(int i = 0; i < 3; i++) {
            ContestInfo currentEnabledContest = enabledContests.get(i);
            ContestInfo currentSavedContest = savedContests.get(i);
            if(currentEnabledContest != null && currentSavedContest == null) {
                //Add new contest to db and the return list
                databaseManager.createOngoingContest(currentEnabledContest);
                String logMessage = String.format(LOG_CREATED_NEW_CONTEST, getContestTypeByID(i),
                        currentEnabledContest.getStartDate(), currentEnabledContest.getStartTime(),
                        currentEnabledContest.getEndDate(), currentEnabledContest.getEndTime()); //How long is this going
                Bukkit.getLogger().info(logMessage);
            } else if (currentEnabledContest == null && currentSavedContest != null) {
                //Delete disabled contest.
                databaseManager.deleteOngoingContest(i);
                long endingTimestamp = currentSavedContest.getEndTime();
                ZonedDateTime dateTime = Instant.ofEpochMilli(endingTimestamp).atZone(zoneId);
                String contestType = getContestTypeByID(i);
                String logMessage = String.format(LOG_DELETED_CONTEST, contestType, dateTime, endingTimestamp);
                Bukkit.getLogger().info(logMessage);
            }
        }
    }

    private String getContestTypeByID(int i) {
        if(i == 0) {
            return "daily";
        }
        if(i == 1) {
            return "weekly";
        }
        if(i == 2) {
            return "monthly";
        }
        return "some non-existent value because you edited the db you lil punk";
    }

    private void initialiseEndedContests(ArrayList<ContestInfo> enabledContests, ArrayList<ContestInfo> savedContests) {
        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        long currTime = currentDateTime.toInstant().toEpochMilli();

        for(int i = 0; i < 3; i++) {
            ContestInfo currContest = enabledContests.get(i);
            ContestInfo savedContest = savedContests.get(i);
            if (savedContest == null) {
                continue; //The band-aid for when the contest is newly initialised.
            }
            if(currTime < savedContest.getEndTime()) {
                continue;
            }

            //Update Contests
            handleEndedContestTasks(savedContest);
            databaseManager.updateRunningContestInfo(i, currContest.getStartTime(), currContest.getEndTime());
        }
    }

    private void handleEndedContestTasks(ContestInfo oldContest) {
        String logMessage = String.format(LOG_MESSAGE_CONTEST_ENDED, oldContest.getStartDate(), oldContest.getEndDate());
        Bukkit.getLogger().info(logMessage);
        ArrayList<ArrayList<PlayerData>> allContestWinners = databaseManager.fetchContestWinners(oldContest);
        databaseManager.logContestWinners(allContestWinners, oldContest);
        if(allContestWinners.isEmpty()) {
            Bukkit.getLogger().info("[HoloQuiz] The contest ended with no winners! Not even one!");
            return;
        }
        ArrayList<ContestWinner> contestWinners = parseContestWinners(allContestWinners, oldContest);
        rewardsHandler.giveContestRewards(contestWinners, oldContest);
    }

    private ArrayList<ContestWinner> parseContestWinners
(ArrayList<ArrayList<PlayerData>> contestWinnersData, ContestInfo contestInfo) {
        ArrayList<ContestWinner> contestWinners = new ArrayList<>();
        for(int i = 0; i < contestWinnersData.size(); i++) {
            ArrayList<PlayerData> contestCategoryWinnersData = contestWinnersData.get(i);
            ArrayList<ContestRewardTier> contestRewardTiers = contestInfo.getRewardByCategory(i);
            int limit = Math.min(contestCategoryWinnersData.size(), contestRewardTiers.size());
            for(int j = 0; j < limit; j++) {
                ContestWinner winner = new ContestWinner(contestRewardTiers.get(j), contestCategoryWinnersData.get(j), j);
                contestWinners.add(winner);
            }
        }
        return contestWinners;
    }
}
