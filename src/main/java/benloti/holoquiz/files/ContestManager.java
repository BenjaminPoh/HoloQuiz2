package benloti.holoquiz.files;

import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.games.RewardsHandler;
import benloti.holoquiz.structs.ContestInfo;
import benloti.holoquiz.structs.ContestProgressGUI;
import benloti.holoquiz.structs.PlayerContestStats;
import benloti.holoquiz.structs.RewardTier;
import benloti.holoquiz.structs.ContestWinner;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ContestManager {
    private static final String LOG_CURRENT_TIME =  "The set TimeZone is %s, giving a current date of %s (%d)";
    private static final String LOG_CREATED_NEW_CONTEST = "Scheduled new %s Contest from %s (%d) to %s (%d)";
    private static final String LOG_DELETED_CONTEST = "Removed old %s Contest that ends on %s (%d)";
    private static final String LOG_MESSAGE_CONTEST_ENDED = "Contest from %s (%d) to %s (%d) ended!";
    private static final String DEV_ERROR_IMPOSSIBLE_CODE = "Contest %s has Type Code %d";
    private static final String DEV_ERROR_CONTEST_GUI_DOUBLE_OPENED = "A player managed to open the Contests GUI while having it open.";
    private static final String DEV_ERROR_CONTEST_GUI_DOUBLE_CLOSED = "A player managed to close the Contests GUI while not having it open.";

    private final DatabaseManager databaseManager;
    private final RewardsHandler rewardsHandler;
    private final ExternalFiles externalFiles;
    private final UserInterface userInterface;

    private final ZoneId zoneId;
    private final int intendedStartDay;
    private final ArrayList<ContestInfo> allContests;
    private final int totalEnabledSubcontests;
    private final Set<String> playersWithOpenGUI = new HashSet<>();
    private final int contestLeaderboardMaxSize;

    public ContestManager(DatabaseManager databaseManager, ConfigFile configFile,
                          ExternalFiles externalFiles, GameManager gameManager, UserInterface userInterface) {
        this.databaseManager = databaseManager;
        this.rewardsHandler = gameManager.getRewardsHandler();
        this.externalFiles = externalFiles;
        this.userInterface = userInterface;

        this.zoneId = configFile.getTimezoneOffset();
        this.intendedStartDay = externalFiles.getConfigFile().getWeeklyResetDay();

        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        long currentTimestamp = currentDateTime.toInstant().toEpochMilli();
        String logMessage = String.format(LOG_CURRENT_TIME, zoneId, currentDateTime, currentTimestamp);
        Logger.getLogger().info_low(logMessage);

        this.allContests = initialiseContests(externalFiles, configFile);
        updateContestsStatus(true); //Check for expired Contests
        this.totalEnabledSubcontests = countEnabledContests();
        this.contestLeaderboardMaxSize = configFile.getContestLeaderboardMaxSize();
    }

    public ContestProgressGUI fetchPlayerContestStatus(String playerName, String playerUUID, UserInterface userInterface) {
        if (this.playersWithOpenGUI.contains(playerName)) {
            Logger.getLogger().devError(DEV_ERROR_CONTEST_GUI_DOUBLE_OPENED);
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
            Logger.getLogger().devError(DEV_ERROR_CONTEST_GUI_DOUBLE_CLOSED);
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
            if(!contest.isContestEnabled()) {
                continue;
            }
            loadRewardsForContest(externalFiles, contest.getRewardCategoryName(), contest);
            enabledContestList.add(contest);
        }

        //Update the Database for removed and new contests, and savedContestList with the latest rewards
        return enabledContestList;
    }

    private void loadRewardsForContest(ExternalFiles externalFiles, String type, ContestInfo contest) {
        ArrayList<RewardTier> mostAnswerRewards = externalFiles.getContestRewardByCategory(type + "Most", contest.isMostAnswerContestEnabled());
        ArrayList<RewardTier> fastestRewards = externalFiles.getContestRewardByCategory(type + "Fastest", contest.isFastestAnswerContestEnabled());
        ArrayList<RewardTier> bestAverageRewards = externalFiles.getContestRewardByCategory(type + "BestAvg", contest.isBestAvgContestEnabled());
        ArrayList<RewardTier> bestXRewards = externalFiles.getContestRewardByCategory(type + "BestX", contest.isBestXContestEnabled());
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
            Logger.getLogger().info_low(logMessage);
        }

        //If contest is enabled and timestamp is 0, update with new timestamp
        if(contest.isContestEnabled() && (contest.getStartTime() == 0 || contest.getEndTime() == 0 )){
            generateIntervalForContest(contest);
            externalFiles.updateRegularContestTimestamp(contest.getContestName(), contest.getStartTime(), contest.getEndTime());

            //Logging Purposes
            String logMessage = String.format(LOG_CREATED_NEW_CONTEST, contest.getContestName(),
                    contest.getStartDate(), contest.getStartTime(),
                    contest.getEndDate(), contest.getEndTime());
            Logger.getLogger().info_low(logMessage);
        }
    }

    private void generateIntervalForContest(ContestInfo contest) {
        //All contest start time is assumed to be 00:00 of the day it is set to be enabled.
        //Load their dates based on the current startDate
        LocalDate startDate = LocalDate.now(zoneId);
        if(contest.getTypeCode() == 0) {
            contest.generateDailyIntervalForContest(zoneId, startDate);
            return;
        }
        if(contest.getTypeCode() == 1) {
            contest.generateWeeklyIntervalForContest(zoneId, startDate, this.intendedStartDay);
            return;
        }
        if(contest.getTypeCode() == 2) {
            contest.generateMonthlyIntervalForContest(zoneId, startDate);
            return;
        }
        Logger.getLogger().devError(String.format(DEV_ERROR_IMPOSSIBLE_CODE, contest.getContestName(),  contest.getTypeCode()));
    }

    private ZonedDateTime fetchDateTimeByTimestamp(long time) {
        return Instant.ofEpochMilli(time).atZone(zoneId);
    }

    public void updateContestsStatus(boolean overrideIntervalWithCurrent) {
        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        long currTime = currentDateTime.toInstant().toEpochMilli();

        ArrayList<ContestInfo> toRemove = new ArrayList<>();
        for (ContestInfo contest : allContests) {
            if (currTime < contest.getEndTime()) {
                continue;
            }

            //Contest ended!
            handleEndedContestTasks(contest, overrideIntervalWithCurrent, toRemove);
        }
        allContests.removeAll(toRemove);
    }

    private void handleEndedContestTasks(ContestInfo contest, boolean overrideIntervalWithCurrent, ArrayList<ContestInfo> toRemove) {
        logEndedContest(contest);
        ArrayList<ArrayList<PlayerContestStats>> allContestWinners = databaseManager.fetchContestWinners(contest);
        databaseManager.logContestWinners(allContestWinners, contest);
        ArrayList<ContestWinner> contestWinners = parseContestWinners(allContestWinners, contest);
        rewardsHandler.giveContestRewards(contestWinners);
        if(contest.getTypeCode() == 3){
            //Custom contest ended. Set to disable
            externalFiles.setEndedCustomContest(contest.getContestName());
            toRemove.add(contest);
        } else {
            //Regular contest ended
            if(overrideIntervalWithCurrent) {
                //Override with the interval for this cycle
                generateIntervalForContest(contest);
            } else {
                //Update the Contest to Next Cycle
                contest.updateContestDateToNextCycle(zoneId);
            }
            externalFiles.updateRegularContestTimestamp(contest.getTypeString(), contest.getStartTime(), contest.getEndTime());
        }
    }

    private void logEndedContest(ContestInfo savedContest) {
        LocalDate startDate = savedContest.getStartDate();
        LocalDate endDate = savedContest.getEndDate();
        long startTime = savedContest.getStartTime();
        long endTime = savedContest.getEndTime();
        String logMessage = String.format(LOG_MESSAGE_CONTEST_ENDED, startDate, startTime, endDate, endTime);
        Logger.getLogger().info_low(logMessage);
    }

    private ArrayList<ContestWinner> parseContestWinners(ArrayList<ArrayList<PlayerContestStats>> contestWinnersData, ContestInfo contestInfo) {
        ArrayList<ContestWinner> contestWinners = new ArrayList<>();
        for (int i = 0; i < contestWinnersData.size(); i++) {
            ArrayList<PlayerContestStats> contestCategoryWinnersData = contestWinnersData.get(i);
            ArrayList<RewardTier> contestRewardTiers = contestInfo.getRewardByCategory(i);
            int limit = Math.min(contestCategoryWinnersData.size(), contestRewardTiers.size());
            for (int j = 0; j < limit; j++) {
                ContestWinner winner = new ContestWinner(contestRewardTiers.get(j), contestCategoryWinnersData.get(j), j + 1, contestInfo, userInterface);
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

