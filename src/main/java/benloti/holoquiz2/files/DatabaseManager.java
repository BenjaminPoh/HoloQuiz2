package benloti.holoquiz2.files;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class DatabaseManager {
    private static final String DB_NAME = "HoloQuiz";

    private static final String SQL_STATEMENT_CREATE_STATS_TABLE =
            "CREATE TABLE IF NOT EXISTS holoquiz_stats (user_id INT , best INT, total LONG, answers INT)";
    private static final String SQL_STATEMENT_CREATE_LOGS_TABLE =
            "CREATE TABLE IF NOT EXISTS answers_logs (user_id INT , timestamp LONG, took INT)";
    private static final String SQL_STATEMENT_CREATE_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS user_info (user_id INT , player_uuid STRING, username STRING)";
    
    private static final String SQL_STATEMENT_OBTAIN_USER_ID =
            "SELECT * FROM user_info WHERE player_uuid = '%s'";
    private static final String SQL_STATEMENT_ADD_NEW_USER_INFO =
            "INSERT INTO user_info (user_id, player_uuid, username) VALUES (?, ?, ?)";
    private static final String SQL_STATEMENT_ASSIGN_NEW_USER_ID =
            "SELECT COUNT (user_id) FROM user_info";
    private static final String SQL_STATEMENT_UPDATE_LOGS =
            "INSERT INTO answers_logs (user_id, timestamp, took) VALUES (?, ?, ?)";
    private static final String SQL_STATEMENT_FETCH_STATS = 
            "SELECT * FROM holoquiz_stats WHERE user_id = '%s'";
    private static final String SQL_STATEMENT_UPDATE_STATS = 
            "UPDATE holoquiz_stats SET best = ?, answers = ?, total = ? WHERE user_id = ?";
    private static final String SQL_STATEMENT_INSERT_NEW_STATS = 
            "INSERT INTO holoquiz_stats (best, answers, total, user_id) VALUES (?, ?, ?, ?)";
    
    private static final String ERROR_MSG_DB_FILE = "Yabe peko, what happened to the db peko";
    private static final String ERROR_MSG_UUID_USERNAME_MISMATCH =
            "Supposed to update table. Not important at this point given how minecraft works and this is intended for HoloCraft, which is a cracked server";

    private static Connection connection;
    private final JavaPlugin plugin;
    private final File dataFile;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = checkFile();
        initialiseTables();
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

    public void initialiseTables() {
        Connection connection = getConnection();
        createTable(connection, SQL_STATEMENT_CREATE_STATS_TABLE);
        createTable(connection, SQL_STATEMENT_CREATE_LOGS_TABLE);
        createTable(connection, SQL_STATEMENT_CREATE_USERS_TABLE);
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

    private void createTable(Connection connection, String query) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int obtainPlayerID(String PlayerUUID, String PlayerName) {
        String firstStatement = String.format(SQL_STATEMENT_OBTAIN_USER_ID, PlayerUUID);
        try {
            PreparedStatement statement = connection.prepareStatement(firstStatement);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Bukkit.getLogger().info("Player has answered before...");
                String savedName = resultSet.getString("username");
                if (!savedName.equals(PlayerName)) {
                    Bukkit.getLogger().info(ERROR_MSG_UUID_USERNAME_MISMATCH);
                }
                return resultSet.getInt("user_id");
            } else {
                PreparedStatement infoStatement = connection.prepareStatement(SQL_STATEMENT_ADD_NEW_USER_INFO);
                int newUserID = assignNewUserID();
                infoStatement.setInt(1, newUserID);
                infoStatement.setString(2, PlayerUUID);
                infoStatement.setString(3, PlayerName);
                infoStatement.executeUpdate();
                Bukkit.getLogger().info("Player has never answered before, assigned ID: " + newUserID);
                return newUserID;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int assignNewUserID() throws SQLException {
        PreparedStatement assignIDStatement = connection.prepareStatement(SQL_STATEMENT_ASSIGN_NEW_USER_ID);
        ResultSet resultSet = assignIDStatement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) + 1;
    }

    public void updateLogsRecord(int userID, long timeStamp, int timeTaken) {
        try (PreparedStatement logsStatement = connection.prepareStatement(SQL_STATEMENT_UPDATE_LOGS)) {
            logsStatement.setInt(1, userID);
            logsStatement.setLong(2, timeStamp);
            logsStatement.setInt(3, timeTaken);
            logsStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStatsRecord(int userID, int timeTaken) {
        String fetchStatsStatement = String.format(SQL_STATEMENT_FETCH_STATS, userID);
        String statsStatement;
        int totalAnswers, bestTime;
        long totalTimeTaken;
        
        try {
            PreparedStatement fetchStatsQuery = connection.prepareStatement(fetchStatsStatement);
            ResultSet resultSet = fetchStatsQuery.executeQuery();
            boolean isNotFirstAnswer = resultSet.next();
            
            if (isNotFirstAnswer) {
                Bukkit.getLogger().info("Player has answered before!");
                totalAnswers = resultSet.getInt("answers");
                totalTimeTaken = resultSet.getLong("total");
                bestTime = resultSet.getInt("best");

                if (bestTime > timeTaken) {
                    bestTime = timeTaken;
                }
                totalTimeTaken += timeTaken;
                totalAnswers += 1;

                statsStatement = SQL_STATEMENT_UPDATE_STATS;
            } else {
                Bukkit.getLogger().info("Player has never answered before!");
                totalAnswers = 1;
                totalTimeTaken = timeTaken;
                bestTime = timeTaken;
                statsStatement = SQL_STATEMENT_INSERT_NEW_STATS;
            }
            
            PreparedStatement statsSQLQuery = connection.prepareStatement(statsStatement);
            statsSQLQuery.setLong(1, bestTime);
            statsSQLQuery.setLong(2, totalAnswers);
            statsSQLQuery.setDouble(3, totalTimeTaken);
            statsSQLQuery.setInt(4, userID);
            statsSQLQuery.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int load(String playerId) {
        try (Connection connection = getConnection()) {
            String requestStatsStatement = "SELECT * FROM holoquiz_stats WHERE userId = ?";
            try (PreparedStatement statement = connection.prepareStatement(requestStatsStatement)) {

                statement.setString(1, playerId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getInt("score");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
