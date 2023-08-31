package benloti.holoquiz.database;

import benloti.holoquiz.structs.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*; //Blasphemy
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class DatabaseManager {
    private static final String DB_NAME = "HoloQuiz.db";
    private static final String ERROR_MSG_DB_FILE = "Yabe peko, what happened to the db peko";

    private static final String SQL_STATEMENT_FETCH_ALL_LOGS =
            "SELECT * FROM answers_logs";
    private static final String SQL_STATEMENT_UPDATE_STATS =
            "UPDATE holoquiz_stats SET best = ?, answers = ?, total = ? WHERE user_id = ?";

    private Connection connection;
    private final JavaPlugin plugin;
    private final File dataFile;
    private final HoloQuizStats holoQuizStats;
    private final AnswersLogs answersLogs;
    private final UserInfo userInfo;
    private final UserPersonalisation userPersonalisation;
    private final int numberOfEntries;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = checkFile();
        this.connection = getConnection();
        this.holoQuizStats = new HoloQuizStats(connection);
        this.answersLogs = new AnswersLogs(connection);
        this.userInfo = new UserInfo(connection);
        this.userPersonalisation = new UserPersonalisation(connection);
        this.numberOfEntries = userInfo.getSize(connection);
    }

    public File checkFile() {
        File dataFolder = new File(plugin.getDataFolder(), DB_NAME);
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
                Bukkit.getLogger().info("[HoloQuiz] Making new SQL connection!");
                // Establish a database connection
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile);
            }
            if (connection == null || connection.isClosed()) {
                Bukkit.getLogger().log(Level.SEVERE, "This ain't right peko");
                return null;
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Is called after players give an accepted answer.
     * Begins by obtaining the player's HoloQuiz ID given their name.
     * Next, updates the answer logs and the players stats in the database.
     *
     * @param player       The player who answered correctly
     * @param timeAnswered UNIX timestamp of when the player answered
     * @param timeTaken    Time taken for player to answer, in milliseconds
     * @return The player's PlayerData, to update the leaderboards
     */
    public PlayerData updateAfterCorrectAnswer(Player player, long timeAnswered, int timeTaken) {
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();
        int playerHoloQuizID = userInfo.getHoloQuizIDByUUID(connection, playerUUID, playerName, numberOfEntries);
        if (playerHoloQuizID == 0) {
            return null;
        }
        answersLogs.updateLogsRecord(connection, playerHoloQuizID, timeAnswered, timeTaken);
        return holoQuizStats.updateStatsRecord(connection, playerHoloQuizID, timeTaken, playerName);
    }

    /**
     * Fetches the player's PlayerData from the database
     * Begins by obtaining the player's HoloQuiz ID given their name.
     * Next, obtains the player's information from the database
     *
     * @param playerName The player's name
     * @return The player's PlayerData
     */
    public PlayerData loadPlayerData(String playerName) {
        int playerHoloQuizID = userInfo.getHoloQuizIDByUserName(connection, playerName);
        if (playerHoloQuizID == 0) {
            return null;
        }
        return holoQuizStats.loadPlayerData(connection, playerHoloQuizID, playerName);
    }

    public ArrayList<PlayerData> loadAllPlayerData() {
        String[] allPlayerNames = userInfo.getAllPlayerNames(connection, numberOfEntries);
        return holoQuizStats.getAllPlayerData(connection, allPlayerNames);
    }

    public UserPersonalisation getUserPersonalisation() {
        return this.userPersonalisation;
    }

    /**
     * In Version 1.1.3, a bug was created which caused the same question to be broadcast.
     * This function is now added to fix that. Once illegitimate answers are removed from answers_logs,
     * This function will recompute holoquiz_stats
     */
    public int reloadDatabase() {
        HashMap<Integer, Long> timeRecord = new HashMap<>();
        HashMap<Integer, Integer> bestRecord = new HashMap<>();
        HashMap<Integer, Integer> totalAnsRecord = new HashMap<>();
        try {
            PreparedStatement fetchPlayerStatsQuery = connection.prepareStatement(SQL_STATEMENT_FETCH_ALL_LOGS);
            ResultSet resultSet = fetchPlayerStatsQuery.executeQuery();
            //First read the logs
            while (resultSet.next()) {
                int timeTaken = resultSet.getInt("took");
                int holoQuizID = resultSet.getInt("user_id");
                if(timeRecord.containsKey(holoQuizID)) {
                    timeRecord.put(holoQuizID, timeTaken + timeRecord.get(holoQuizID));
                    totalAnsRecord.put(holoQuizID, 1 + totalAnsRecord.get(holoQuizID));
                    int tempBest = bestRecord.get(holoQuizID);
                    if(tempBest > timeTaken) {
                        bestRecord.put(holoQuizID, timeTaken);
                    }
                } else {
                    timeRecord.put(holoQuizID, (long) timeTaken);
                    bestRecord.put(holoQuizID, timeTaken);
                    totalAnsRecord.put(holoQuizID, 1);
                }
            }
            //Then recompute
            int size = 0;
            for(Map.Entry<Integer, Long> keyValueSet : timeRecord.entrySet()) {
                size++;
                PreparedStatement insertStatsQuery = connection.prepareStatement(SQL_STATEMENT_UPDATE_STATS);
                insertStatsQuery.setInt(1, bestRecord.get(keyValueSet.getKey()));
                insertStatsQuery.setInt(2, totalAnsRecord.get(keyValueSet.getKey()));
                insertStatsQuery.setLong(3, keyValueSet.getValue());
                insertStatsQuery.setInt(4, keyValueSet.getKey());
                insertStatsQuery.executeUpdate();
            }
            return size;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
