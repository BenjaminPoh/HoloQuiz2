package benloti.holoquiz.archive;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.HashMap;

public class DataMigrator {
    private static final String FETCH_TIME = "SELECT * FROM answers_logs";
    private static final String FETCH_COUNT = "SELECT * FROM holoquiz_stats";
    private static final String REPLACE_AVERAGE = "UPDATE holoquiz_stats SET best = ?, total = ? WHERE user_id = ?";

    public DataMigrator(JavaPlugin plugin) {
        HashMap<Integer, Long> uuidToTotalTime = new HashMap<>();
        File dataFolder = new File(plugin.getDataFolder(), "Old.db");
        try {
            Bukkit.getLogger().info("Let us begin peko");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);

            PreparedStatement fetchPlayerStatsQuery = connection.prepareStatement(FETCH_TIME);
            ResultSet resultSet = fetchPlayerStatsQuery.executeQuery();
            while(resultSet.next()) {
                int id = resultSet.getInt("user_id");
                long timeTaken = resultSet.getLong("took");
                if(uuidToTotalTime.containsKey(id)) {
                    long newTimeTaken = uuidToTotalTime.get(id) + timeTaken;
                    uuidToTotalTime.put(id, newTimeTaken);
                } else {
                    uuidToTotalTime.put(id, timeTaken);
                }
            }

            PreparedStatement secondStatement = connection.prepareStatement(FETCH_COUNT);
            ResultSet resultSet2 = secondStatement.executeQuery();
            while(resultSet2.next()) {
                int id = resultSet2.getInt("user_id");
                int answers = resultSet2.getInt("answers");
                double bestTime = resultSet2.getDouble("best");
                bestTime = bestTime * 1000;
                long totalTime = uuidToTotalTime.get(id);
                double oldAverage = resultSet2.getDouble("total");
                double newAverage = totalTime / (answers * 1.0) / 1000;
                double difference = oldAverage - newAverage;

                PreparedStatement updateStatement = connection.prepareStatement(REPLACE_AVERAGE);
                updateStatement.setInt(1, (int) bestTime );
                updateStatement.setLong(2, totalTime);
                updateStatement.setInt(3, id);
                updateStatement.executeUpdate();

                if(difference > 0.03 || difference < -0.03) {
                    Bukkit.getLogger().info(String.format("Old average: %ss vs New Average: %ss found for ID:%s with %s answers and time %s"
                            , oldAverage, newAverage, id, answers, totalTime));
                }
            }
            Bukkit.getLogger().info("GUCCI PEKO");
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().info("NOT GUCCI PEKO");
        }

    }
}