package benloti.holoquiz.files;

import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.games.RewardsHandler;
import benloti.holoquiz.structs.ContestInfo;

import benloti.holoquiz.structs.ContestRewardTier;
import benloti.holoquiz.structs.ContestWinner;
import benloti.holoquiz.structs.PlayerData;
import org.bukkit.Bukkit;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;

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

    private ArrayList<ContestInfo> initialiseContestInfo(ExternalFiles externalFiles, ConfigFile configFile) {
        //Creates contests as though they don't exist.
        ArrayList<ContestInfo> enabledContestList = initialiseNewContests(externalFiles, configFile);
        //Fetch ongoing contests that were saved
        ArrayList<Long> savedContestList = databaseManager.fetchSavedContests();
        //Update the Database for removed and new contests
        updateContestDB(enabledContestList,savedContestList);
        //Check for ended contests
        initialiseEndedContests(enabledContestList,savedContestList);
        return enabledContestList;
    }

    public void updateContestsStatus() {
        long currTime = ZonedDateTime.now(zoneId).toInstant().toEpochMilli();

        for(int i = 0; i < 3; i++) {
            ContestInfo currContest = enabledContests.get(i);
            long endTime = currContest.getEndTime();
            checkForContestExpiry(currTime, i, currContest, endTime);
        }
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

    private void updateContestDB(ArrayList<ContestInfo> enabledContests, ArrayList<Long> savedContests) {
        for(int i = 0; i < 3; i++) {
            ContestInfo currentEnabledContest = enabledContests.get(i);
            long currentSavedContest = savedContests.get(i);
            if(currentEnabledContest != null && currentSavedContest == 0) {
                //Add new contest to db and the return list
                databaseManager.createOngoingContest(currentEnabledContest);
                String logMessage = String.format(LOG_CREATED_NEW_CONTEST, getContestTypeByID(i),
                        currentEnabledContest.getStartDate(), currentEnabledContest.getStartTime(),
                        currentEnabledContest.getEndDate(), currentEnabledContest.getEndTime()); //How long is this going
                Bukkit.getLogger().info(logMessage);
            } else if (currentEnabledContest == null && currentSavedContest > 0) {
                //Delete disabled contest.
                databaseManager.deleteOngoingContest(i);
                ZonedDateTime dateTime = Instant.ofEpochMilli(currentSavedContest).atZone(zoneId);
                String contestType = getContestTypeByID(i);
                String logMessage = String.format(LOG_DELETED_CONTEST, contestType, dateTime, currentSavedContest);
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

    private void initialiseEndedContests(ArrayList<ContestInfo> enabledContests, ArrayList<Long> savedContests) {
        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        long currTime = currentDateTime.toInstant().toEpochMilli();

        for(int i = 0; i < 3; i++) {
            ContestInfo currContest = enabledContests.get(i);
            long endTime = savedContests.get(i);
            if (endTime == 0) {
                continue; //The band-aid for when the contest is newly initialised.
            }
            checkForContestExpiry(currTime, i, currContest, endTime);
        }
    }

    private void checkForContestExpiry(long currTime, int i, ContestInfo currContest, long endTime) {
        if(currTime > endTime) {
            ArrayList<ArrayList<PlayerData>> allContestWinners = databaseManager.fetchContestWinners(currContest);
            String logMessage = String.format(LOG_MESSAGE_CONTEST_ENDED, currContest.getStartDate(), currContest.getEndDate());
            Bukkit.getLogger().info(logMessage);
            if(allContestWinners.isEmpty()) {
                Bukkit.getLogger().info("[HoloQuiz] The contest ended with no winners! Not even one!");
                currContest.updateTournamentDateToNextCycle(zoneId);
                return;
            }
            ArrayList<ContestWinner> contestWinners = parseContestWinners(allContestWinners, currContest);
            rewardsHandler.giveContestRewards(contestWinners, currContest);
            currContest.updateTournamentDateToNextCycle(zoneId);

            //Update Contests
            databaseManager.updateRunningContestInfo(i, currContest.getEndTime());
        }
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
