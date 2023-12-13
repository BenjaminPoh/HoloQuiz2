package benloti.holoquiz.database;

import benloti.holoquiz.structs.PlayerData;

import java.sql.*;
import java.util.ArrayList;

public class HoloQuizStats {
    private static final String SQL_STATEMENT_CREATE_STATS_TABLE =
            "CREATE TABLE IF NOT EXISTS holoquiz_stats (user_id INT PRIMARY KEY , best INT, total BIGINT, answers INT, average INT)";
    private static final String SQL_STATEMENT_UPDATE_STATS =
            "UPDATE holoquiz_stats SET best = ?, total = ?, answers = ?, average = ? WHERE user_id = ?";
    private static final String SQL_STATEMENT_INSERT_NEW_STATS =
            "INSERT INTO holoquiz_stats (best, total, answers, average, user_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_STATEMENT_FETCH_STATS =
            "SELECT * FROM holoquiz_stats WHERE user_id = ?";
    private static final String SQL_STATEMENT_FETCH_ALL_STATS =
            "SELECT * FROM holoquiz_stats";
    private static final String SQL_STATEMENT_FETCH_LEADERBOARD =
            "SELECT * FROM holoquiz_stats ORDER BY %s LIMIT %d";

    private final DatabaseManager databaseManager;

    public HoloQuizStats(Connection connection, DatabaseManager databaseManager) {
        createTable(connection);
        this.databaseManager = databaseManager;
    }

    public void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(SQL_STATEMENT_CREATE_STATS_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a player's all-time statistics.
     * First, the player's information is fetched from the database.
     * Next, the player's best time, total answers and total time taken is updated.
     * If the player is not found in the database, they will be added into it.
     * Finally, the updated information is then returned in the form of a PlayerData class
     *
     * @param connection the Connection to the database
     * @param userID the player's HoloQuiz ID
     * @param timeTaken the time the player took to answer
     * @param playerName the player's name, used to return the PlayerData class
     * @return a PlayerData class with the player's updated statistics, null if something goes wrong.
     */
    public PlayerData updateStatsRecord(Connection connection, int userID, int timeTaken, String playerName) {
        int totalAnswers, bestTime, averageTime;
        long totalTimeTaken;

        try {
            String nextStatement;
            PreparedStatement fetchStatsQuery = connection.prepareStatement(SQL_STATEMENT_FETCH_STATS);
            fetchStatsQuery.setInt(1, userID);
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
                averageTime = (int)(totalTimeTaken / totalAnswers);

                nextStatement = SQL_STATEMENT_UPDATE_STATS;
            } else {
                totalAnswers = 1;
                totalTimeTaken = timeTaken;
                bestTime = timeTaken;
                averageTime = timeTaken;

                nextStatement = SQL_STATEMENT_INSERT_NEW_STATS;
            }

            PreparedStatement statsSQLQuery = connection.prepareStatement(nextStatement);
            statsSQLQuery.setInt(1, bestTime);
            statsSQLQuery.setLong(2, totalTimeTaken);
            statsSQLQuery.setInt(3, totalAnswers);
            statsSQLQuery.setInt(4, averageTime);
            statsSQLQuery.setInt(5, userID);
            statsSQLQuery.executeUpdate();

            return new PlayerData(playerName,bestTime,totalAnswers, averageTime);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the player's all-time statistics
     * Given a HoloQuiz ID, fetches the player's information
     *
     * @param connection The connection to the database
     * @param holoQuizID A Valid ID matching the player's name
     * @param playerName The player's name
     * @return The player's statistics in a PlayerData class, null if something goes wrong.
     */
    public PlayerData loadPlayerData(Connection connection, int holoQuizID, String playerName) {
        try {
            PreparedStatement fetchStatsQuery = connection.prepareStatement(SQL_STATEMENT_FETCH_STATS);
            fetchStatsQuery.setInt(1, holoQuizID);
            ResultSet resultSet = fetchStatsQuery.executeQuery();
            resultSet.next();
            int totalAnswers = resultSet.getInt("answers");
            int bestTime = resultSet.getInt("best");
            int averageTime = resultSet.getInt("average");
            return new PlayerData(playerName,bestTime,totalAnswers, averageTime);
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
                int averageTime = resultSet.getInt("average");
                int bestTime = resultSet.getInt("best");
                int holoQuizID = resultSet.getInt("user_id");
                String playerName = allPlayerNames[holoQuizID - 1];
                PlayerData playerData = new PlayerData(playerName,bestTime,totalAnswers, averageTime);
                allPlayerData.add(playerData);
            }
            return allPlayerData;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getLeaderboardForColumn(Connection connection, String columnAndOrder, int size, ArrayList<PlayerData> list) {
        String sqlQuery = String.format(SQL_STATEMENT_FETCH_LEADERBOARD, columnAndOrder, size);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                int totalAnswers = resultSet.getInt("answers");
                int averageTime = resultSet.getInt("average");
                int bestTime = resultSet.getInt("best");
                int holoQuizID = resultSet.getInt("user_id");
                String playerName = databaseManager.getPlayerNameByHoloQuizID(connection, holoQuizID);
                PlayerData leaderboardEntry = new PlayerData(playerName, bestTime, totalAnswers, averageTime);
                list.add(leaderboardEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
