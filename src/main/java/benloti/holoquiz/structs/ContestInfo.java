package benloti.holoquiz.structs;

import org.bukkit.Bukkit;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

public class ContestInfo {
    private final String type;
    private final int minAnswersNeeded;

    private final ArrayList<ContestRewardTier> topAnswerRewards;
    private final ArrayList<ContestRewardTier> bestAverageRewards;
    private final ArrayList<ContestRewardTier> fastestRewards;
    private final int topAnswerPlacements;
    private final int bestAverageAnswerPlacements;
    private final int fastestAnswerPlacements;

    private long startTime;
    private long endTime;
    private LocalDate startDate;
    private LocalDate endDate;

    public ContestInfo(long start, long end, String type, int minReq, ArrayList<ContestRewardTier> topAnswerRewards,
                       ArrayList<ContestRewardTier> bestAverageRewards, ArrayList<ContestRewardTier> fastestRewards) {
        this.startTime = start;
        this.endTime = end;
        this.type = type;
        this.minAnswersNeeded = minReq;
        this.topAnswerRewards = topAnswerRewards;
        this.bestAverageRewards = bestAverageRewards;
        this.fastestRewards = fastestRewards;
        this.topAnswerPlacements = checkPlacementCount(topAnswerRewards);
        this.bestAverageAnswerPlacements = checkPlacementCount(bestAverageRewards);
        this.fastestAnswerPlacements = checkPlacementCount(fastestRewards);

    }

    public long getStartTime() {
        return this.endTime;
    }

    public long getEndTime() {
        return this.startTime;
    }

    public String getType() {
        return type;
    }

    public void updateContestTimes(long start, long end) {
        this.startTime = start;
        this.endTime = end;
    }

    public int getMinAnswersNeeded() {
        return minAnswersNeeded;
    }

    public int getTopAnswerPlacements() {
        return topAnswerPlacements;
    }

    public int getBestAverageAnswerPlacements() {
        return bestAverageAnswerPlacements;
    }

    public int getFastestAnswerPlacements() {
        return fastestAnswerPlacements;
    }

    public void loadDates (ZoneId zoneId) {
        startDate = Instant.ofEpochMilli(startTime).atZone(zoneId).toLocalDate();
        endDate = Instant.ofEpochMilli(endTime).atZone(zoneId).toLocalDate();
    }


    public ArrayList<ContestRewardTier> getRewardByCategory(String categoryCode) {
        if(categoryCode.equals("M")) {
            return topAnswerRewards;
        }
        if(categoryCode.equals("F")) {
            return fastestRewards;
        }
        if(categoryCode.equals("B")) {
            return bestAverageRewards;
        }
        Bukkit.getLogger().info("[HoloQuiz] Error! If you see this, I need to retire from coding :p");
        return null;
    }

    private int checkPlacementCount(ArrayList<ContestRewardTier> rewardTiers) {
        if(rewardTiers == null) {
            return 0;
        }

        int totalPlacements = 0;
        for(ContestRewardTier rewardTier : rewardTiers) {
            totalPlacements += rewardTier.getReps();
        }
        return totalPlacements;
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
