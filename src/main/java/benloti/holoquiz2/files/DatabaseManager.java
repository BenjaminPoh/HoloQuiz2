package benloti.holoquiz2.files;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static Connection connection;
    private final JavaPlugin plugin;
    private final File dataFolder;

    private static final String DB_PATH = "plugins/HoloQuiz2/HoloQuiz.db";
    private static final String DB_NAME = "HoloQuiz";


    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = checkFile();
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
        try {
            Connection connection = getConnection();
            List<String> tableNames = new ArrayList<>();
            tableNames.add("holoquiz_stats");
            tableNames.add("answer_logs");
            //tableNames.add("player_info");
            for (String s : tableNames) {
                boolean tableExists = checkTableExists(connection, s);
                if (!tableExists) {
                    createTable(connection, s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Bukkit.getLogger().info("This has been ran!");
                // Establish a database connection
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateStatsRecord(int userID, long bestTime, long answers, double averageTime) {
        try (Connection connection = getConnection()) {
            String updateStatsStatement =
                    "UPDATE holoquiz_stats SET best = ?, answers = ?, average = ? WHERE userId = ?";
            try (PreparedStatement statsStatement = connection.prepareStatement(updateStatsStatement)) {
                statsStatement.setLong(1, bestTime);
                statsStatement.setLong(2, answers);
                statsStatement.setDouble(3, averageTime);
                statsStatement.setInt(4, userID);
                statsStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLogsRecord(int userID, long timeStamp, long timeTaken) {
        try (Connection connection = getConnection()) {
            if (!connection.isValid(3600)) {
                Bukkit.getLogger().info("what the peko why is it invalid");
            }
            if (connection != null && connection.isValid(3600)) {
                String updateLogsStatement = "INSERT INTO answer_logs (userID, timestamp, took) VALUES (?, ?, ?)";
                try (PreparedStatement logsStatement = connection.prepareStatement(updateLogsStatement)) {
                    logsStatement.setInt(1, userID);
                    logsStatement.setLong(2, timeStamp);
                    logsStatement.setLong(3, timeTaken);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLogsRecord2(int userID, long timeStamp, long timeTaken) {
        String updateLogsStatement = "INSERT INTO answer_logs (userID, timestamp, took) VALUES (?, ?, ?)";
        try (PreparedStatement logsStatement = connection.prepareStatement(updateLogsStatement)) {
            logsStatement.setInt(1, userID);
            logsStatement.setLong(2, timeStamp);
            logsStatement.setLong(3, timeTaken);
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

    private boolean checkTableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }

    private void createTable(Connection connection, String s) {
        String creationStatement;
        //TOD: Make into enum
        if (s.equals("holoquiz_stats")) {
            creationStatement =
                    "CREATE TABLE holoquiz_stats (userId INT , best LONG, average LONG, answers LONG)";
        } else {
            creationStatement =
                    "CREATE TABLE answers_logs (userId INT , timestamp LONG, took LONG)";
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(creationStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
