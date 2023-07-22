package benloti.holoquiz2.database;

import benloti.holoquiz2.structs.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*; //Blasphemy
import java.util.ArrayList;

public class DatabaseManager {
    private static final String DB_NAME = "HoloQuiz";
    private static final String ERROR_MSG_DB_FILE = "Yabe peko, what happened to the db peko";

    private static Connection connection;
    private final JavaPlugin plugin;
    private final File dataFile;
    private final HoloQuizStats holoQuizStats;
    private final AnswersLogs answersLogs;
    private final UserInfo userInfo;
    private final int numberOfEntries;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = checkFile();
        getConnection();
        this.holoQuizStats = new HoloQuizStats(connection);
        this.answersLogs = new AnswersLogs(connection);
        this.userInfo = new UserInfo(connection);
        this.numberOfEntries = userInfo.getSize(connection);
    }

    public File checkFile() {
        File dataFolder = new File(plugin.getDataFolder(), DB_NAME + ".db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().info(ERROR_MSG_DB_FILE);
                e.printStackTrace();
            }
        }
        return dataFolder;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Bukkit.getLogger().info("Making new connection!");
                // Establish a database connection
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile);
            }
            if (connection == null || connection.isClosed()) {
                Bukkit.getLogger().info("This ain't right peko");
                return null;
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PlayerData updateAfterCorrectAnswer (Player player, long timeAnswered, int timeTaken) {
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();
        int playerHoloQuizID = userInfo.getHoloQuizIDByUUID(connection, playerUUID, playerName, numberOfEntries);
        if(playerHoloQuizID == 0) {
            return null;
        }
        answersLogs.updateLogsRecord(connection, playerHoloQuizID, timeAnswered, timeTaken);
        return holoQuizStats.updateStatsRecord(connection, playerHoloQuizID, timeTaken, playerName);
    }

    public PlayerData loadPlayerData(String playerName) {
        int playerHoloQuizID = userInfo.getHoloQuizIDByUserName(connection, playerName);
        return holoQuizStats.loadPlayerData(connection,playerHoloQuizID,playerName);
    }

    public ArrayList<PlayerData> loadAllPlayerData() {
        String[] allPlayerNames = userInfo.getAllPlayerNames(connection, numberOfEntries);
        return holoQuizStats.getAllPlayerData(connection, allPlayerNames);
    }
}
