package benloti.holoquiz2.data;

import benloti.holoquiz2.files.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerData {
    private final String playerName;
    private final String playerUUID;
    private long totalTime;
    private long bestTime;
    private long questionsAnswered;

    public PlayerData(String name, String UUID, long totalTime, long bestTime, long answers) {
        this.playerName = name;
        this.playerUUID = UUID;
        this.bestTime = bestTime;
        this.totalTime = totalTime;
        this.questionsAnswered = answers;
    }
        // Getters and setters for player data properties
    // ...
}