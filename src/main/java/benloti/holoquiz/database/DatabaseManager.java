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
    private static final String ERROR_MSG_DB_FILE = "[HoloQuiz] Database File is bugged!";

    private static final String SQL_STATEMENT_FETCH_ALL_LOGS =
            "SELECT * FROM answers_logs";
    private static final String SQL_STATEMENT_UPDATE_STATS =
            "UPDATE holoquiz_stats SET best = ?, answers = ?, total = ?, average = ? WHERE user_id = ?";

    private Connection connection;
    private final JavaPlugin plugin;
    private final File dataFile;
    private final HoloQuizStats holoQuizStats;
    private final AnswersLogs answersLogs;
    private final UserInfo userInfo;
    private final UserPersonalisation userPersonalisation;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = checkFile();
        this.connection = getConnection();
        this.holoQuizStats = new HoloQuizStats(connection, this);
        this.answersLogs = new AnswersLogs(connection);
        this.userInfo = new UserInfo(connection);
        this.userPersonalisation = new UserPersonalisation(connection);
    }

    public File checkFile() {
        File dataFile = new File(plugin.getDataFolder(), DB_NAME);
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().info(ERROR_MSG_DB_FILE);
                e.printStackTrace();
            }
        }
        return dataFile;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Bukkit.getLogger().info("[HoloQuiz] Making new SQL connection!");
                // Establish a database connection
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile);
                if (connection == null || connection.isClosed()) {
                    Bukkit.getLogger().log(Level.SEVERE, ERROR_MSG_DB_FILE);
                    return null;
                }
                Bukkit.getLogger().info("[HoloQuiz] New SQL connection established!");
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
    public PlayerData updateAfterCorrectAnswer(Player player, long timeAnswered, int timeTaken, String gameMode) {
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();
        connection = getConnection();
        int playerHoloQuizID = userInfo.getHoloQuizIDByUUID(connection, playerUUID, playerName);
        if (playerHoloQuizID == 0) {
            Bukkit.getLogger().info("[HoloQuiz] Error: Player doesn't exist. You should NOT see this.");
            return null;
        }
        answersLogs.updateLogsRecord(connection, playerHoloQuizID, timeAnswered, timeTaken, gameMode);
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
        connection = getConnection();
        int playerHoloQuizID = userInfo.getHoloQuizIDByUserName(connection, playerName);
        if (playerHoloQuizID == 0) {
            return null;
        }
        return holoQuizStats.loadPlayerData(connection, playerHoloQuizID, playerName);
    }

    public UserPersonalisation getUserPersonalisation() {
        return this.userPersonalisation;
    }

    /**
     * Recomputes holoquiz_stats based on the information in answers_logs.
     */
    public int reloadDatabase() {
        connection = getConnection();
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
                int totalAnswers = totalAnsRecord.get(keyValueSet.getKey());
                long totalTimeTaken = keyValueSet.getValue();
                int averageTimeTaken = (int)(totalTimeTaken / totalAnswers);
                PreparedStatement insertStatsQuery = connection.prepareStatement(SQL_STATEMENT_UPDATE_STATS);
                insertStatsQuery.setInt(1, bestRecord.get(keyValueSet.getKey()));
                insertStatsQuery.setInt(2, totalAnswers);
                insertStatsQuery.setLong(3,totalTimeTaken);
                insertStatsQuery.setInt(4, averageTimeTaken);
                insertStatsQuery.setInt(5, keyValueSet.getKey());
                insertStatsQuery.executeUpdate();
            }
            return size;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * In version 1.3.0, Database was completely rewritten
     * This allowed for leaderboards to be maintained without funny tricks.
     */
    public ArrayList<PlayerData> loadLeaderboard(int size, int minReq, String column, boolean order) {
        ArrayList<PlayerData> leaderboardList = new ArrayList<>();
        String formattedColumn = column;
        if(order) {
            formattedColumn += " ASC";
        } else {
            formattedColumn += " DESC";
        }
        holoQuizStats.getLeaderboardForColumn(connection, formattedColumn, size, minReq, leaderboardList);
        return leaderboardList;
    }

    public String getPlayerNameByHoloQuizID (Connection connection, int holoQuizID) {
        return userInfo.getPlayerNameByHoloQuizID(connection, holoQuizID);
    }
}
