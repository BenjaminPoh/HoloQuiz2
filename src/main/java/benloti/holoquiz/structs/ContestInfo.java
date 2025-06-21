package benloti.holoquiz.structs;

import org.bukkit.Bukkit;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

public class ContestInfo {
    private final int typeCode; //0 -> Daily, 1 -> Weekly , 2 -> Monthly
    private int minAnswersNeeded;

    private final ArrayList<ContestRewardTier> topAnswerRewards; // Code 0
    private final ArrayList<ContestRewardTier> fastestRewards; // Code 1
    private final ArrayList<ContestRewardTier> bestAverageRewards; // Code 2

    private long startTime;
    private long endTime;
    private LocalDate startDate;
    private LocalDate endDate;

    public ContestInfo(int type, long startTime, long endTime) {
        this.typeCode = type;
        this.minAnswersNeeded = 0;
        this.topAnswerRewards = new ArrayList<>();
        this.fastestRewards = new ArrayList<>();
        this.bestAverageRewards = new ArrayList<>();

        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ContestInfo(ZoneId zoneId, int type, int minReq, ArrayList<ContestRewardTier> topAnswerRewards,
                       ArrayList<ContestRewardTier> bestAverageRewards, ArrayList<ContestRewardTier> fastestRewards,
                       LocalDate startDate, LocalDate endDate) {
        this.typeCode = type;
        this.minAnswersNeeded = minReq;
        this.topAnswerRewards = topAnswerRewards;
        this.bestAverageRewards = bestAverageRewards;
        this.fastestRewards = fastestRewards;

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

    public int getMinAnswersNeeded() {
        return minAnswersNeeded;
    }

    public ArrayList<ContestRewardTier> getRewardByCategory(int code) {
        if(code == 0) {
            return topAnswerRewards;
        }
        if(code == 1) {
            return fastestRewards;
        }
        if(code == 2) {
            return bestAverageRewards;
        }
        Bukkit.getLogger().info("[HoloQuiz] Error! If you see this, I need to retire from coding :p");
        return null;
    }

    public int getRewardCountByCategory(int code) {
        if(code == 0) {
            return topAnswerRewards.size();
        }
        if(code == 1) {
            return fastestRewards.size();
        }
        if(code == 2) {
            return bestAverageRewards.size();
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

    public void updateInfo(ContestInfo otherContest, ZoneId zoneId) {
        this.topAnswerRewards.addAll(otherContest.getRewardByCategory(0));
        this.fastestRewards.addAll(otherContest.getRewardByCategory(1));
        this.bestAverageRewards.addAll(otherContest.getRewardByCategory(2));
        this.minAnswersNeeded = otherContest.getMinAnswersNeeded();
        this.startDate = Instant.ofEpochMilli(this.startTime).atZone(zoneId).toLocalDate();
        this.endDate = Instant.ofEpochMilli(this.endTime).atZone(zoneId).toLocalDate();
    }
}
