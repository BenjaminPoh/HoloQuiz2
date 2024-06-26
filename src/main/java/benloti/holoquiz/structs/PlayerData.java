package benloti.holoquiz.structs;

public class PlayerData {
    private final String playerName;
    private final int holoQuizID;

    private final double averageTime;
    private final int bestTime;
    private final int questionsAnswered;

    public PlayerData(String name, int bestTime, int answers, int averageTime, int holoQuizID) {
        this.playerName = name;
        this.bestTime = bestTime;
        this.questionsAnswered = answers;
        this.averageTime = averageTime;
        this.holoQuizID = holoQuizID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getAverageTime() {
        return averageTime;
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

    public int getHoloQuizID() {
        return holoQuizID;
    }
}