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
    private static final String LOG_MESSAGE_CONTEST_ENDED = "[HoloQuiz] Contest from %s (%d) to %s (%d) ended!";

    private final DatabaseManager databaseManager;
    private final RewardsHandler rewardsHandler;
    private final ZoneId zoneId;

    private final ContestInfo dailyContest;
    private final ContestInfo weeklyContest;
    private final ContestInfo monthlyContest;

    private final ArrayList<ContestInfo> enabledContests;
    private final Set<String> playersWithOpenGUI = new HashSet<>();

    public ContestManager(DatabaseManager databaseManager, ConfigFile configFile,
                          ExternalFiles externalFiles, GameManager gameManager) {
        this.databaseManager = databaseManager;
        this.rewardsHandler = gameManager.getRewardsHandler();
        this.dailyContest = configFile.getDailyContestConfig();
        this.weeklyContest = configFile.getWeeklyContestConfig();
        this.monthlyContest = configFile.getMonthlyContestConfig();
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
            if(currContest == null) {
                continue;
            }
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
            if(contest == null) {
                continue;
            }
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
        //Update the Database for removed and new contests, and savedContestList with the latest rewards
        updateContestInfo(enabledContestList,savedContestList);
        //Check for ended contests
        initialiseEndedContests(enabledContestList,savedContestList);
        return enabledContestList;
    }

    private ArrayList<ContestInfo> initialiseNewContests(ExternalFiles externalFiles, ConfigFile configFile) {
        // All contest start time is assumed to be 00:00 of the day it is set to be enabled.
        ArrayList<ContestInfo> enabledContestList = new ArrayList<>(Arrays.asList(null,null,null));
        if(dailyContest.isContestEnabled()) {
            LocalDate startDate = LocalDate.now(zoneId); //Also EndDate

            ArrayList<ContestRewardTier> mostAnswerRewards = externalFiles.getContestRewardByCategory("DailyMost");
            ArrayList<ContestRewardTier> fastestRewards = externalFiles.getContestRewardByCategory("DailyFastest");
            ArrayList<ContestRewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory("DailyBestAvg");
            ArrayList<ContestRewardTier> bestXRewards = externalFiles.getContestRewardByCategory("DailyBestX");

            dailyContest.updateRewards(mostAnswerRewards,fastestRewards,bestAverageRewards,bestXRewards);
            dailyContest.updateTimes(zoneId,startDate,startDate);

            enabledContestList.set(0, dailyContest);
        }
        if(weeklyContest.isContestEnabled()) {
            LocalDate startDate = LocalDate.now(zoneId);
            int intendedStartDay = configFile.getWeeklyResetDay();
            int daysFromStartDay = getDaysFromStartDay(startDate.getDayOfWeek().getValue(), intendedStartDay);
            startDate = startDate.minusDays(daysFromStartDay);
            LocalDate endDate = startDate.plusWeeks(1);
            endDate = endDate.minusDays(1);

            ArrayList<ContestRewardTier> mostAnswerRewards = externalFiles.getContestRewardByCategory("WeeklyMost");
            ArrayList<ContestRewardTier> fastestRewards = externalFiles.getContestRewardByCategory("WeeklyFastest");
            ArrayList<ContestRewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory("WeeklyBestAvg");
            ArrayList<ContestRewardTier> bestXRewards = externalFiles.getContestRewardByCategory("WeeklyBestX");

            weeklyContest.updateRewards(mostAnswerRewards,fastestRewards,bestAverageRewards,bestXRewards);
            weeklyContest.updateTimes(zoneId,startDate,endDate);

            enabledContestList.set(1, weeklyContest);
        }
        if(monthlyContest.isContestEnabled()) {
            LocalDate startDate = LocalDate.now(zoneId);
            int daysFromFirstDayOfMonth = startDate.getDayOfMonth() - 1;
            startDate = startDate.minusDays(daysFromFirstDayOfMonth);
            LocalDate endDate = startDate.plusMonths(1);
            endDate = endDate.minusDays(1);

            ArrayList<ContestRewardTier> mostAnswerRewards = externalFiles.getContestRewardByCategory("MonthlyMost");
            ArrayList<ContestRewardTier> fastestRewards = externalFiles.getContestRewardByCategory("MonthlyFastest");
            ArrayList<ContestRewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory("MonthlyBestAvg");
            ArrayList<ContestRewardTier> bestXRewards = externalFiles.getContestRewardByCategory("MonthlyBestX");

            monthlyContest.updateRewards(mostAnswerRewards,fastestRewards,bestAverageRewards,bestXRewards);
            monthlyContest.updateTimes(zoneId,startDate,endDate);

            enabledContestList.set(2, monthlyContest);
        }
        return enabledContestList;
    }

    private int getDaysFromStartDay(int currentDay, int intendedDay) {
        if(currentDay >= intendedDay) {
            return currentDay - intendedDay;
        }
        return currentDay - intendedDay + 7;
    }

    private void updateContestInfo(ArrayList<ContestInfo> enabledContests, ArrayList<ContestInfo> savedContests) {
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
                ZonedDateTime dateTime = fetchDateTimeByTimestamp(endingTimestamp);
                String contestType = getContestTypeByID(i);
                String logMessage = String.format(LOG_DELETED_CONTEST, contestType, dateTime, endingTimestamp);
                Bukkit.getLogger().info(logMessage);
            } else if (currentEnabledContest != null && currentSavedContest != null) {
                currentSavedContest.updateInfo(currentEnabledContest, zoneId);
            }
        }
    }

    private ZonedDateTime fetchDateTimeByTimestamp(long time) {
        return Instant.ofEpochMilli(time).atZone(zoneId);
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
        logEndedContest(oldContest);
        ArrayList<ArrayList<PlayerData>> allContestWinners = databaseManager.fetchContestWinners(oldContest);
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
(ArrayList<ArrayList<PlayerData>> contestWinnersData, ContestInfo contestInfo) {
        ArrayList<ContestWinner> contestWinners = new ArrayList<>();
        for(int i = 0; i < contestWinnersData.size(); i++) {
            ArrayList<PlayerData> contestCategoryWinnersData = contestWinnersData.get(i);
            ArrayList<ContestRewardTier> contestRewardTiers = contestInfo.getRewardByCategory(i);
            int limit = Math.min(contestCategoryWinnersData.size(), contestRewardTiers.size());
            for(int j = 0; j < limit; j++) {
                ContestWinner winner = new ContestWinner(contestRewardTiers.get(j), contestCategoryWinnersData.get(j), j + 1);
                contestWinners.add(winner);
            }
        }
        return contestWinners;
    }
}

