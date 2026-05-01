package benloti.holoquiz.structs;

public class PlayerContestStats{

    private final String playerName;
    private final int holoQuizID;

    private final int averageTime;
    private final int bestTime;
    private final int questionsAnswered;
    private final int bestXTimes;
    private final int timeToImprove;

    public PlayerContestStats(String name, int holoQuizID, int answers, int bestTime, int averageTime, int bestXTimes, int timeToImprove) {
        this.holoQuizID = holoQuizID;
        this.playerName = name;

        this.questionsAnswered = answers;
        this.bestTime = bestTime;
        this.averageTime = averageTime;
        this.bestXTimes = bestXTimes;
        this.timeToImprove = timeToImprove;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getAverageTime() {
        return averageTime;
    }

    public int getBestXTime() {
        return bestXTimes;
    }

    public int getBestTime() {
        return bestTime;
    }

    public String getAverageTimeInSeconds3DP() {
        Double time = averageTime / 1000.0;
        return String.format("%.3f",time);
    }

    public String getBestTimeInSeconds3DP() {
        Double time = bestTime / 1000.0;
        return String.format("%.3f",time);
    }


    public int getQuestionsAnswered() {
        return questionsAnswered;
    }

    public String getBestXTimesInSeconds3DP() {
        Double time = bestXTimes / 1000.0;
        return String.format("%.3f",time);
    }

    public int getHoloQuizID() {
        return holoQuizID;
    }

    public String getTimeToImproveInSeconds3dp() {
        Double time = timeToImprove / 1000.0;
        return String.format("%.3f",time);
    }
}
