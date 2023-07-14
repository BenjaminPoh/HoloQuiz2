package benloti.holoquiz2.data;

import benloti.holoquiz2.files.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerData {
    private final String playerName;
    private final String playerUUID;
    private final int playerHoloQuizID;
    private long totalTime;
    private int bestTime;
    private int questionsAnswered;

    public PlayerData(String name, String UUID, int holoQuizID, long totalTime, int bestTime, int answers) {
        this.playerName = name;
        this.playerUUID = UUID;
        this.playerHoloQuizID = holoQuizID;
        this.bestTime = bestTime;
        this.totalTime = totalTime;
        this.questionsAnswered = answers;
    }

}