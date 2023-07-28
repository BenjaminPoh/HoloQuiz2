package benloti.holoquiz.database;

import benloti.holoquiz.structs.PlayerData;

import java.sql.*;
import java.util.ArrayList;

public class HoloQuizStats {
    private static final String SQL_STATEMENT_CREATE_STATS_TABLE =
            "CREATE TABLE IF NOT EXISTS holoquiz_stats (user_id INT , best INT, total LONG, answers INT)";
    private static final String SQL_STATEMENT_UPDATE_STATS =
            "UPDATE holoquiz_stats SET best = ?, answers = ?, total = ? WHERE user_id = ?";
    private static final String SQL_STATEMENT_INSERT_NEW_STATS =
            "INSERT INTO holoquiz_stats (best, answers, total, user_id) VALUES (?, ?, ?, ?)";
    private static final String SQL_STATEMENT_FETCH_STATS =
            "SELECT * FROM holoquiz_stats WHERE user_id = '%s'";
    private static final String SQL_STATEMENT_FETCH_ALL_STATS =
            "SELECT * FROM holoquiz_stats";

    public HoloQuizStats(Connection connection) {
        createTable(connection);
    }

    public void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(SQL_STATEMENT_CREATE_STATS_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PlayerData updateStatsRecord(Connection connection, int userID, int timeTaken, String playerName) {
        String fetchStatsStatement = String.format(SQL_STATEMENT_FETCH_STATS, userID);
        String statsStatement;
        int totalAnswers, bestTime;
        long totalTimeTaken;

        try {
            PreparedStatement fetchStatsQuery = connection.prepareStatement(fetchStatsStatement);
            ResultSet resultSet = fetchStatsQuery.executeQuery();
            boolean isNotFirstAnswer = resultSet.next();

            if (isNotFirstAnswer) {
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
                totalAnswers = 1;
                totalTimeTaken = timeTaken;
                bestTime = timeTaken;
                statsStatement = SQL_STATEMENT_INSERT_NEW_STATS;
            }

            PreparedStatement statsSQLQuery = connection.prepareStatement(statsStatement);
            statsSQLQuery.setLong(1, bestTime);
            statsSQLQuery.setLong(2, totalAnswers);
            statsSQLQuery.setLong(3, totalTimeTaken);
            statsSQLQuery.setInt(4, userID);
            statsSQLQuery.executeUpdate();

            return new PlayerData(playerName,bestTime,totalTimeTaken,totalAnswers);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PlayerData loadPlayerData(Connection connection, int holoQuizID, String playerName) {
        try {
            String fetchStatsStatement = String.format(SQL_STATEMENT_FETCH_STATS, holoQuizID);
            PreparedStatement fetchStatsQuery = connection.prepareStatement(fetchStatsStatement);
            ResultSet resultSet = fetchStatsQuery.executeQuery();
            resultSet.next();
            int totalAnswers = resultSet.getInt("answers");
            long totalTimeTaken = resultSet.getLong("total");
            int bestTime = resultSet.getInt("best");
            return new PlayerData(playerName, bestTime, totalTimeTaken, totalAnswers);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<PlayerData> getAllPlayerData(Connection connection, String[] allPlayerNames) {
        ArrayList<PlayerData> allPlayerData = new ArrayList<>();
        try {
            PreparedStatement fetchPlayerStatsQuery = connection.prepareStatement(SQL_STATEMENT_FETCH_ALL_STATS);
            ResultSet resultSet = fetchPlayerStatsQuery.executeQuery();
            while(resultSet.next()) {
                int totalAnswers = resultSet.getInt("answers");
                long totalTimeTaken = resultSet.getLong("total");
                int bestTime = resultSet.getInt("best");
                int holoQuizID = resultSet.getInt("user_id");
                String playerName = allPlayerNames[holoQuizID - 1];
                PlayerData playerData = new PlayerData(playerName, bestTime, totalTimeTaken, totalAnswers);
                allPlayerData.add(playerData);
            }
            return allPlayerData;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
