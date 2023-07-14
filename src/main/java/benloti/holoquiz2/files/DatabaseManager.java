package benloti.holoquiz2.files;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.transform.Result;
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

    public static final String SQL_STATEMENT_UPDATE_LOGS =
            "INSERT INTO answers_logs (userID, timestamp, took) VALUES (?, ?, ?)";

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
                Bukkit.getLogger().info("Yabe peko, what happened to the db peko");
                e.printStackTrace();
            }
        }
        return dataFolder;
    }
    public void initialiseTables() {
        Connection connection = getConnection();
        createTable(connection, SQL_STATEMENT_CREATE_STATS_TABLE);
        createTable(connection,SQL_STATEMENT_CREATE_LOGS_TABLE);
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
                Bukkit.getLogger().info("This aint right peko");
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
        String SQLQuery = "SELECT * FROM user_info WHERE player_uuid = '" + PlayerUUID + "'";
        try {
            PreparedStatement statement = connection.prepareStatement(SQLQuery);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Bukkit.getLogger().info("Player has answered before...");
                String savedName = resultSet.getString("username");
                if(!savedName.equals(PlayerName)) {
                    Bukkit.getLogger().info("Supposed to update table. Not important at this point given how" +
                            " minecraft works and this is intended for HoloCraft, which is a cracked server");
                }
                return resultSet.getInt("user_id");
            } else {
                String insertUserInfoStatement =
                        "INSERT INTO user_info (user_id, player_uuid, username) VALUES (?, ?, ?)";
                PreparedStatement infoStatement = connection.prepareStatement(insertUserInfoStatement);
                int newUserID = assignNewUserID();
                infoStatement.setInt(1, newUserID);
                infoStatement.setString(2, PlayerUUID);
                infoStatement.setString(3, PlayerName);
                infoStatement.executeUpdate();
                Bukkit.getLogger().info("Player has never answered before, assigned ID: " + newUserID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int assignNewUserID() throws SQLException {
        String assignNewUserIDStatement = "SELECT COUNT (user_id) FROM user_info";
        PreparedStatement assignIDStatement = connection.prepareStatement(assignNewUserIDStatement);
        ResultSet resultSet = assignIDStatement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) + 1;
    }

    public void updateLogsRecord(int userID, long timeStamp, int timeTaken) {
        String updateLogsStatement = SQL_STATEMENT_UPDATE_LOGS;
        try (PreparedStatement logsStatement = connection.prepareStatement(updateLogsStatement)) {
            logsStatement.setInt(1, userID);
            logsStatement.setLong(2, timeStamp);
            logsStatement.setInt(3, timeTaken);
            logsStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStatsRecord(int userID, int timeTaken) {
            String fetchStatsStatement = "SELECT * FROM holoquiz_stats WHERE user_id = '" + userID + "'";
        String updateStatsStatement =
                "UPDATE holoquiz_stats SET best = ?, answers = ?, total = ? WHERE userId = ?";
        try {
            PreparedStatement fetchStatsQuery = connection.prepareStatement(fetchStatsStatement);
            ResultSet resultSet = fetchStatsQuery.executeQuery();
            resultSet.next();
            int totalAnswers = resultSet.getInt("answers");
            long totalTimeTaken = resultSet.getLong("total");
            int bestTime = resultSet.getInt("best");

            if(bestTime > timeTaken) {
                bestTime = timeTaken;
            }
            totalTimeTaken += timeTaken;
            totalAnswers += 1;

            PreparedStatement statsStatement = connection.prepareStatement(updateStatsStatement);
                statsStatement.setLong(1, bestTime);
                statsStatement.setLong(2, totalAnswers);
                statsStatement.setDouble(3, totalTimeTaken);
                statsStatement.setInt(4, userID);
                statsStatement.executeUpdate();
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
