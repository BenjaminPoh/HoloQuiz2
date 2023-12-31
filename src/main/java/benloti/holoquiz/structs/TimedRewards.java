package benloti.holoquiz.structs;

import java.util.ArrayList;

public class TimedRewards {
    private static ArrayList<RewardTier> dailyMost;
    private static ArrayList<RewardTier> dailyFastest;
    private static ArrayList<RewardTier> dailyBestAvg;
    private static ArrayList<RewardTier> weeklyMost;
    private static ArrayList<RewardTier> weeklyFastest;
    private static ArrayList<RewardTier> weeklyBestAvg;
    private static ArrayList<RewardTier> monthlyMost;
    private static ArrayList<RewardTier> monthlyFastest;
    private static ArrayList<RewardTier> monthlyBestAvg;

    public TimedRewards() {
        dailyMost = new ArrayList<>();
        dailyFastest = new ArrayList<>();
        dailyBestAvg = new ArrayList<>();
        weeklyMost = new ArrayList<>();
        weeklyFastest = new ArrayList<>();
        weeklyBestAvg = new ArrayList<>();
        monthlyMost = new ArrayList<>();
        monthlyFastest = new ArrayList<>();
        monthlyBestAvg = new ArrayList<>();
    }

    public ArrayList<RewardTier> getRespectiveRewardsSection(String code) {
        switch (code) {
        default:
            return null;
        case ("DailyMost"):
            return dailyMost;
        case ("DailyFastest"):
            return dailyFastest;
        case ("DailyBestAvg"):
            return dailyBestAvg;
        case ("WeeklyMost"):
            return weeklyMost;
        case ("WeeklyFastest"):
            return weeklyFastest;
        case ("WeeklyBestAvg"):
            return weeklyBestAvg;
        case ("MonthlyMost"):
            return monthlyMost;
        case ("MonthlyFastest"):
            return monthlyFastest;
        case ("MonthlyBestAvg"):
            return monthlyBestAvg;
        }
    }

}
