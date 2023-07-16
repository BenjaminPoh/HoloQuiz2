package benloti.holoquiz2.data;

public class PlayerData {
    private final String playerName;
    private double averageTime;
    private int bestTime;
    private int questionsAnswered;

    public PlayerData(String name, double averageTime, int bestTime, int answers) {
        this.playerName = name;
        this.bestTime = bestTime;
        this.averageTime = averageTime;
        this.questionsAnswered = answers;
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
}