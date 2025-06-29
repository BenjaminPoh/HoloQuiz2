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
            ArrayList<ArrayList<PlayerContestStats>> allContestWinners = databaseManager.fetchContestWinners(contest);
            PlayerContestStats targetPlayerPlacement = databaseManager.fetchPlayerContestPlacement(contest,playerName,playerUUID);
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
        //Load Rewards
        loadRewardsForContest(externalFiles, dailyContest.getTypeString(), dailyContest);
        loadRewardsForContest(externalFiles, weeklyContest.getTypeString(), weeklyContest);
        loadRewardsForContest(externalFiles, monthlyContest.getTypeString(), monthlyContest);

        // All contest start time is assumed to be 00:00 of the day it is set to be enabled.
        LocalDate startDate = LocalDate.now(zoneId);
        //Load End Dates for Daily...
        dailyContest.updateTimes(zoneId,startDate,startDate);

        //Weekly
        int intendedStartDay = configFile.getWeeklyResetDay();
        int daysFromStartDayOfWeek = getDaysFromStartDay(startDate.getDayOfWeek().getValue(), intendedStartDay);
        LocalDate startDateOfWeek = startDate.minusDays(daysFromStartDayOfWeek);
        LocalDate endDateOfWeek = startDateOfWeek.plusWeeks(1);
        endDateOfWeek = endDateOfWeek.minusDays(1);
        weeklyContest.updateTimes(zoneId,startDateOfWeek,endDateOfWeek);

        //Monthly
        int daysFromFirstDayOfMonth = startDate.getDayOfMonth() - 1;
        LocalDate startDateOfMonth = startDate.minusDays(daysFromFirstDayOfMonth);
        LocalDate endDateOfMonth = startDateOfMonth.plusMonths(1);
        endDateOfMonth = endDateOfMonth.minusDays(1);
        monthlyContest.updateTimes(zoneId,startDateOfMonth,endDateOfMonth);

        //Set contest
        ArrayList<ContestInfo> enabledContestList = new ArrayList<>(Arrays.asList(null,null,null));
        enabledContestList.set(0, dailyContest);
        enabledContestList.set(1, weeklyContest);
        enabledContestList.set(2, monthlyContest);
        return enabledContestList;
    }

    private void loadRewardsForContest(ExternalFiles externalFiles, String type, ContestInfo contest) {
        ArrayList<ContestRewardTier> mostAnswerRewards = externalFiles.getContestRewardByCategory(type + "Most", contest.isMostAnswerContestEnabled());
        ArrayList<ContestRewardTier> fastestRewards = externalFiles.getContestRewardByCategory(type + "Fastest", contest.isFastestAnswerContestEnabled());
        ArrayList<ContestRewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory(type + "BestAvg", contest.isBestAvgContestEnabled());
        ArrayList<ContestRewardTier> bestXRewards = externalFiles.getContestRewardByCategory(type + "BestX", contest.isBestXContestEnabled());
        contest.updateRewards(mostAnswerRewards,fastestRewards,bestAverageRewards,bestXRewards);
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
        for(int i = 0; i < contestWinnersData.size(); i++) {
            ArrayList<PlayerContestStats> contestCategoryWinnersData = contestWinnersData.get(i);
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

