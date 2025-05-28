package benloti.holoquiz.files;

import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.games.RewardsHandler;
import benloti.holoquiz.structs.ContestInfo;

import benloti.holoquiz.structs.ContestRewardTier;
import benloti.holoquiz.structs.ContestWinner;
import benloti.holoquiz.structs.PlayerData;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;

public class ContestManager {
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
        LocalDate todayDate = LocalDate.now(zoneId);
        long currTime = todayDate.atStartOfDay(zoneId).toInstant().toEpochMilli();

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
            } else if (currentEnabledContest == null && currentSavedContest > 0) {
                //Delete disabled contest.
                databaseManager.deleteOngoingContest(i);
            }
        }
    }

    private void initialiseEndedContests(ArrayList<ContestInfo> enabledContests, ArrayList<Long> savedContests) {
        LocalDate todayDate = LocalDate.now(zoneId);
        long currTime = todayDate.atStartOfDay(zoneId).toInstant().toEpochMilli();

        for(int i = 0; i < 3; i++) {
            ContestInfo currContest = enabledContests.get(i);
            long endTime = savedContests.get(i);
            checkForContestExpiry(currTime, i, currContest, endTime);
        }
    }

    private void checkForContestExpiry(long currTime, int i, ContestInfo currContest, long endTime) {
        if(currTime > endTime) {
            ArrayList<ArrayList<PlayerData>> dailyContestWinners = databaseManager.fetchContestWinners(currContest);
            ArrayList<ContestWinner> contestWinners = parseContestWinners(dailyContestWinners, currContest);
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
            for(int j = 0; j< contestInfo.getRewardCountByCategory(i); j++) {
                ContestWinner winner = new ContestWinner(contestRewardTiers.get(j), contestCategoryWinnersData.get(j), j);
                contestWinners.add(winner);
            }
        }
        return contestWinners;
    }

}
