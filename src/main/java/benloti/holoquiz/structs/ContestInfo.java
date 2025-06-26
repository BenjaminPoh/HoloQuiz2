package benloti.holoquiz.structs;

import org.bukkit.Bukkit;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

public class ContestInfo {
    private final int typeCode; //0 -> Daily, 1 -> Weekly , 2 -> Monthly
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

    public ContestInfo(int type, boolean zeroEnabled, boolean oneEnabled, boolean twoEnabled, boolean threeEnabled,
                       int twoMinReq, int threeMinReq) {
        this.typeCode = type;
        this.bestAvgMinReq = twoMinReq;
        this.bestXMinReq = threeMinReq;
        this.mostAnswerContestEnabled = zeroEnabled;
        this.fastestAnswerContestEnabled = oneEnabled;
        this.bestAvgContestEnabled = twoEnabled;
        this.bestXContestEnabled = threeEnabled;
    }

    public ContestInfo(int type, long startTime, long endTime) {
        this.typeCode = type;
        this.bestAvgMinReq = 0;
        this.startTime = startTime;
        this.endTime = endTime;

        this.mostAnswerRewards = new ArrayList<>();
        this.bestAverageRewards = new ArrayList<>();
        this.fastestRewards = new ArrayList<>();
        this.bestXRewards = new ArrayList<>();
    }

    public void updateInfo(ContestInfo otherContest, ZoneId zoneId) {
        this.mostAnswerRewards.addAll(otherContest.getRewardByCategory(0));
        this.fastestRewards.addAll(otherContest.getRewardByCategory(1));
        this.bestAverageRewards.addAll(otherContest.getRewardByCategory(2));
        this.bestXRewards.addAll(otherContest.getRewardByCategory(3));
        this.bestAvgMinReq = otherContest.getBestAvgMinReq();
        this.bestXMinReq = otherContest.getBestXMinReq();
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

    public void updateTimes(ZoneId zoneId, LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
        LocalDate tempEndDate = endDate.plusDays(1);
        this.endTime = tempEndDate.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1;
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

    public boolean isContestEnabled() {
        return mostAnswerContestEnabled || fastestAnswerContestEnabled || bestAvgContestEnabled || bestXContestEnabled;
    }

    public int getBestXMinReq() {
        return bestXMinReq;
    }

    public int getBestAvgMinReq() {
        return bestAvgMinReq;
    }

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

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

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
}
