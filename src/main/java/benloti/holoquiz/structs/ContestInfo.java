package benloti.holoquiz.structs;

import org.bukkit.Bukkit;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

public class ContestInfo {
    private final int typeCode; //0 -> Daily, 1 -> Weekly , 2 -> Monthly
    private final boolean isEnabled;
    private final String contestName;
    private final String rewardCategoryName;

    private long startTime;
    private long endTime;
    private LocalDate startDate;
    private LocalDate endDate;

    private boolean mostAnswerContestEnabled; // Code 0
    private boolean fastestAnswerContestEnabled; // Code 1
    private boolean bestAvgContestEnabled; // Code 2
    private boolean bestXContestEnabled; //Code 3

    private ArrayList<ContestRewardTier> mostAnswerRewards; // Code 0
    private ArrayList<ContestRewardTier> fastestRewards; // Code 1
    private ArrayList<ContestRewardTier> bestAverageRewards; // Code 2
    private ArrayList<ContestRewardTier> bestXRewards; //Code 3

    private int bestAvgMinReq;
    private int bestXMinReq;

    public ContestInfo(int type, boolean isEnabled, boolean zeroEnabled, boolean oneEnabled, boolean twoEnabled, boolean threeEnabled,
                       int twoMinReq, int threeMinReq, long startTime, long endTime) {
        this.typeCode = type;
        this.isEnabled = isEnabled;
        this.contestName = getTypeString();
        this.rewardCategoryName = getTypeString();
        this.bestAvgMinReq = twoMinReq;
        this.bestXMinReq = threeMinReq;
        this.mostAnswerContestEnabled = zeroEnabled;
        this.fastestAnswerContestEnabled = oneEnabled;
        this.bestAvgContestEnabled = twoEnabled;
        this.bestXContestEnabled = threeEnabled;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ContestInfo(boolean isEnabled, boolean zeroEnabled, boolean oneEnabled, boolean twoEnabled, boolean threeEnabled,
                       int twoMinReq, int threeMinReq, long startTime, long endTime, String name, String rewardCategory) {
        this.typeCode = 3;
        this.isEnabled = isEnabled;
        this.contestName = name;
        this.rewardCategoryName = rewardCategory;
        this.bestAvgMinReq = twoMinReq;
        this.bestXMinReq = threeMinReq;
        this.mostAnswerContestEnabled = zeroEnabled;
        this.fastestAnswerContestEnabled = oneEnabled;
        this.bestAvgContestEnabled = twoEnabled;
        this.bestXContestEnabled = threeEnabled;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ContestInfo(ContestInfo otherContest, long startTime, long endTime, ZoneId zoneId) {
        this.typeCode = otherContest.getTypeCode();
        this.isEnabled = otherContest.isContestEnabled();
        this.contestName = otherContest.getContestName();
        this.rewardCategoryName = otherContest.getRewardCategoryName();
        this.startTime = startTime;
        this.endTime = endTime;
        this.mostAnswerContestEnabled = otherContest.isMostAnswerContestEnabled();
        this.fastestAnswerContestEnabled = otherContest.isFastestAnswerContestEnabled();
        this.bestAvgContestEnabled = otherContest.isBestAvgContestEnabled();
        this.bestXContestEnabled = otherContest.isBestXContestEnabled();
        this.mostAnswerRewards = otherContest.getRewardByCategory(0);
        this.fastestRewards = otherContest.getRewardByCategory(1);
        this.bestAverageRewards = otherContest.getRewardByCategory(2);
        this.bestXRewards = otherContest.getRewardByCategory(3);
        this.bestAvgMinReq = otherContest.getBestAvgMinReq();
        this.bestXMinReq = otherContest.getBestXMinReq();
        updateTimes(zoneId);

    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public int getBestXMinReq() {
        return bestXMinReq;
    }

    public int getBestAvgMinReq() {
        return bestAvgMinReq;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isMostAnswerContestEnabled() {
        return mostAnswerContestEnabled;
    }

    public boolean isFastestAnswerContestEnabled() {
        return fastestAnswerContestEnabled;
    }

    public boolean isBestAvgContestEnabled() {
        return bestAvgContestEnabled;
    }

    public boolean isBestXContestEnabled() {
        return bestXContestEnabled;
    }

    public String getRewardCategoryName() {
        return rewardCategoryName;
    }

    public String getContestName() {
        return contestName;
    }

    public boolean isContestEnabled() {
        return this.isEnabled;
    }
    //Used for Initialisation
    public void updateTimes(Pair<Long, Long> newTimes, ZoneId zoneId) {
        this.startTime = newTimes.getLeft();
        this.endTime = newTimes.getRight();
        this.startDate = Instant.ofEpochMilli(this.startTime).atZone(zoneId).toLocalDate();
        this.endDate = Instant.ofEpochMilli(this.endTime).atZone(zoneId).toLocalDate();
    }

    public void updateTimes(ZoneId zoneId) {
        this.startDate = Instant.ofEpochMilli(this.startTime).atZone(zoneId).toLocalDate();
        this.endDate = Instant.ofEpochMilli(this.endTime).atZone(zoneId).toLocalDate();
    }

    public void updateRewards(ArrayList<ContestRewardTier> topAnswerRewards, ArrayList<ContestRewardTier> fastestRewards,
                              ArrayList<ContestRewardTier> bestAverageRewards, ArrayList<ContestRewardTier> bestXRewards) {
        this.mostAnswerRewards = topAnswerRewards;
        this.bestAverageRewards = bestAverageRewards;
        this.fastestRewards = fastestRewards;
        this.bestXRewards = bestXRewards;
    }

    //Methods related to Intervals
    public void updateTournamentDateToNextCycle(ZoneId zoneId) {
        boolean theSafeguardThatShouldNeverTrigger = true;
        if(this.typeCode == 0) {
            //typeCode == 0 -> Daily
            theSafeguardThatShouldNeverTrigger = false;
            this.startDate = endDate.plusDays(1);
            this.endDate = startDate.plusDays(1);
        }
        else if(this.typeCode == 1) {
            //typeCode == 1 -> Weekly
            theSafeguardThatShouldNeverTrigger = false;
            this.startDate = endDate.plusDays(1);
            this.endDate = startDate.plusWeeks(1);
        }
        else if(this.typeCode == 2) {
            //typeCode == 2 -> Monthly
            theSafeguardThatShouldNeverTrigger = false;
            this.startDate = endDate.plusDays(1);
            this.endDate = startDate.plusMonths(1);
        }
        this.startTime = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
        this.endTime = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1;
        if(theSafeguardThatShouldNeverTrigger) {
            Bukkit.getLogger().info("[HoloQuiz] Contest StatusCode not between 1 to 3. How did you even get here?");
        }
    }

    public String getTypeString() {
        if(typeCode == 0) {
            return "Daily";
        }
        if(typeCode == 1) {
            return "Weekly";
        }
        if(typeCode == 2) {
            return "Monthly";
        }
        return "You found a bug!";
    }

    //Methods related to Rewards
    public ArrayList<ContestRewardTier> getRewardByCategory(int code) {
        if(code == 0) {
            return mostAnswerRewards;
        }
        if(code == 1) {
            return fastestRewards;
        }
        if(code == 2) {
            return bestAverageRewards;
        }
        if(code == 3) {
            return bestXRewards;
        }
        Bukkit.getLogger().info("[HoloQuiz] Error! If you see this, I need to retire from coding :p");
        return null;
    }

    public int getRewardCountByCategory(int code) {
        if(code == 0) {
            return mostAnswerRewards.size();
        }
        if(code == 1) {
            return fastestRewards.size();
        }
        if(code == 2) {
            return bestAverageRewards.size();
        }
        if(code == 3) {
            return bestXRewards.size();
        }
        Bukkit.getLogger().info("[HoloQuiz] Error! If you see this, I need to retire from coding :p");
        return 0;
    }

    //Methods related to setting up Intervals
    public void generateDailyIntervalForContest(ZoneId zoneId, LocalDate startDate) {
        if(typeCode != 0) {
            return;
        }
        setTimestampForRegularContests(zoneId, startDate, startDate);
    }

    public void generateWeeklyIntervalForContest(ZoneId zoneId, LocalDate startDate, int intendedStartDay) {
        if(typeCode != 1) {
            return;
        }
        int daysFromStartDayOfWeek = getDaysFromStartDay(startDate.getDayOfWeek().getValue(), intendedStartDay);
        LocalDate startDateOfWeek = startDate.minusDays(daysFromStartDayOfWeek);
        LocalDate endDateOfWeek = startDateOfWeek.plusWeeks(1);
        endDateOfWeek = endDateOfWeek.minusDays(1);
        setTimestampForRegularContests(zoneId,startDateOfWeek,endDateOfWeek);
    }

    public void generateMonthlyIntervalForContest(ZoneId zoneId, LocalDate startDate) {
        if(typeCode != 2) {
            return;
        }
        int daysFromFirstDayOfMonth = startDate.getDayOfMonth() - 1;
        LocalDate startDateOfMonth = startDate.minusDays(daysFromFirstDayOfMonth);
        LocalDate endDateOfMonth = startDateOfMonth.plusMonths(1);
        endDateOfMonth = endDateOfMonth.minusDays(1);
        setTimestampForRegularContests(zoneId,startDateOfMonth,endDateOfMonth);
    }

    private int getDaysFromStartDay(int currentDay, int intendedDay) {
        if(currentDay >= intendedDay) {
            return currentDay - intendedDay;
        }
        return currentDay - intendedDay + 7;
    }

    private void setTimestampForRegularContests(ZoneId zoneId, LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
        LocalDate tempEndDate = endDate.plusDays(1);
        this.endTime = tempEndDate.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1;
    }
}
