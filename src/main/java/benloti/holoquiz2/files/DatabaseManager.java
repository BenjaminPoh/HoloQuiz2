package benloti.holoquiz2.files;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class DatabaseManager {
    //public static final String HOLOQUIZ_STATS_TABLE_NAME = "holoquiz_stats";
    //public static final String ANSWER_LOGS_TABLE_NAME = "answer_logs";
    private static final String SQL_STATEMENT_CREATE_STATS_TABLE =
            "CREATE TABLE IF NOT EXISTS holoquiz_stats (userId INT , best LONG, average LONG, answers LONG)";
    private static final String SQL_STATEMENT_CREATE_LOGS_TABLE =
            "CREATE TABLE IF NOT EXISTS answers_logs (userId INT , timestamp LONG, took LONG)";
    //private static final String SQL_STATEMENT_CREATE_USERS_TABLE =
            //"CREATE TABLE IF NOT EXISTS users_ids (userId INT , player_uuid STRING, username STRING)";

    private static Connection connection;
    private final JavaPlugin plugin;
    private final File dataFile;

    //private static final String DB_PATH = "plugins/HoloQuiz2/HoloQuiz.db";
    private static final String DB_NAME = "HoloQuiz";

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
        //createTable(connection, SQL_STATEMENT_CREATE_USERS_TABLE);
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

    public void updateStatsRecord(int userID, long bestTime, long answers, double averageTime) {
            String updateStatsStatement =
                    "UPDATE holoquiz_stats SET best = ?, answers = ?, average = ? WHERE userId = ?";
            try (PreparedStatement statsStatement = connection.prepareStatement(updateStatsStatement)) {
                statsStatement.setLong(1, bestTime);
                statsStatement.setLong(2, answers);
                statsStatement.setDouble(3, averageTime);
                statsStatement.setInt(4, userID);
                statsStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLogsRecord(int userID, long timeStamp, long timeTaken) {
        String updateLogsStatement = "INSERT INTO answers_logs (userID, timestamp, took) VALUES (?, ?, ?)";
        try (PreparedStatement logsStatement = connection.prepareStatement(updateLogsStatement)) {
            logsStatement.setInt(1, userID);
            logsStatement.setLong(2, timeStamp);
            logsStatement.setLong(3, timeTaken);
            logsStatement.executeUpdate();
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
