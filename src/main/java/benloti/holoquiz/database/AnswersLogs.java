package benloti.holoquiz.database;

import benloti.holoquiz.structs.PlayerData;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;

public class AnswersLogs {
    private final DatabaseManager databaseManager;

    private static final String SQL_STATEMENT_CREATE_LOGS_TABLE =
            "CREATE TABLE IF NOT EXISTS answers_logs (user_id INT , timestamp BIGINT, took INT, mode varchar(1))";
    private static final String SQL_STATEMENT_UPDATE_LOGS =
            "INSERT INTO answers_logs (user_id, timestamp, took, mode) VALUES (?, ?, ?, ?)";

    private static final String SQL_STATEMENT_FETCH_MOST_ANSWERS_WITHIN_TIMESTAMP =
            "SELECT user_id, COUNT (*) as ans_count FROM answers_logs WHERE timestamp >= %d AND timestamp <= %d " +
            "GROUP BY user_id ORDER BY ans_count DESC LIMIT %d";
    private static final String SQL_STATEMENT_FETCH_FASTEST_ANSWERS_WITHIN_TIMESTAMP =
            "SELECT user_id, MIN(took) as best_time FROM answers_logs WHERE timestamp >= %d AND timestamp <= %d " +
            "GROUP BY user_id ORDER BY best_time ASC LIMIT %d";
    private static final String SQL_STATEMENT_FETCH_BEST_AVG_ANSWERS_WITHIN_TIMESTAMP =
            "SELECT user_id, (SUM(took)/COUNT (*)) as average, COUNT (*) as ans_count FROM answers_logs " +
            "WHERE timestamp >= %d AND timestamp <= %d GROUP BY user_id " +
            "HAVING ans_count >= %d ORDER BY average ASC LIMIT %d";

    private static final String SQL_STATEMENT_FETCH_PLAYER_STATS_WITHIN_TIMESTAMP =
            "SELECT (SUM(took)/COUNT (*)) as average, COUNT (*) as ans_count, MIN(took) as best_time " +
            "FROM answers_logs WHERE timestamp >= %d AND timestamp <= %d AND user_id = %d";


    public AnswersLogs(Connection connection, DatabaseManager databaseManager) {
        createTable(connection);
        this.databaseManager = databaseManager;
    }

    private void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(SQL_STATEMENT_CREATE_LOGS_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLogsRecord(Connection connection, int userID, long timeStamp, int timeTaken, String gameMode) {
        try (PreparedStatement logsStatement = connection.prepareStatement(SQL_STATEMENT_UPDATE_LOGS)) {
            logsStatement.setInt(1, userID);
            logsStatement.setLong(2, timeStamp);
            logsStatement.setInt(3, timeTaken);
            logsStatement.setString(4, gameMode);
            logsStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<PlayerData> getTopAnswerersWithinTimestamp (Connection connection, long start, long end, int limit) {
        ArrayList<PlayerData> topAnswerers = new ArrayList<>();
        if(limit == 0) {
            return topAnswerers;
        }
        String sqlQuery = String.format(SQL_STATEMENT_FETCH_MOST_ANSWERS_WITHIN_TIMESTAMP, start, end, limit);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                int holoQuizID = resultSet.getInt("user_id");
                int answers = resultSet.getInt("ans_count");
                String playerName = databaseManager.getPlayerNameByHoloQuizID(connection, holoQuizID);
                PlayerData contestWinner = new PlayerData(playerName, -1, answers, -1, holoQuizID);
                topAnswerers.add(contestWinner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topAnswerers;
    }

    public ArrayList<PlayerData> getFastestAnswerersWithinTimestamp(Connection connection, long start, long end, int limit) {
        ArrayList<PlayerData> fastestAnswerers = new ArrayList<>();
        if(limit == 0) {
            return fastestAnswerers;
        }
        String sqlQuery = String.format(SQL_STATEMENT_FETCH_FASTEST_ANSWERS_WITHIN_TIMESTAMP, start, end, limit);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                int holoQuizID = resultSet.getInt("user_id");
                int took = resultSet.getInt("best_time");
                String playerName = databaseManager.getPlayerNameByHoloQuizID(connection, holoQuizID);
                PlayerData contestWinner = new PlayerData(playerName, took, -1, -1, holoQuizID);
                fastestAnswerers.add(contestWinner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fastestAnswerers;
    }

    public ArrayList<PlayerData> getBestAnswerersWithinTimestamp(Connection connection, long start, long end, int limit, int minReq) {
        ArrayList<PlayerData> bestAnswerers = new ArrayList<>();
        if(limit == 0) {
            return bestAnswerers;
        }
        String sqlQuery = String.format(SQL_STATEMENT_FETCH_BEST_AVG_ANSWERS_WITHIN_TIMESTAMP, start, end, minReq, limit);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                int holoQuizID = resultSet.getInt("user_id");
                int answers = resultSet.getInt("ans_count");
                int average = resultSet.getInt("average");
                String playerName = databaseManager.getPlayerNameByHoloQuizID(connection, holoQuizID);
                PlayerData contestWinner = new PlayerData(playerName, -1, answers, average, holoQuizID);
                bestAnswerers.add(contestWinner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bestAnswerers;
    }

    public PlayerData getPlayerStatsWithinTimestamp(Connection connection, long start, long end, int id, String name) {
        String sqlQuery = String.format(SQL_STATEMENT_FETCH_PLAYER_STATS_WITHIN_TIMESTAMP, start, end, id);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                int best = resultSet.getInt("best_time");
                int answers = resultSet.getInt("ans_count");
                int average = resultSet.getInt("average");
                return new PlayerData(name, best, answers, average, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PlayerData(name, -1, -1, -1, id);
    }

    /* @Deprecated
    public ArrayList<PlayerData> getFastestAnswerersWithinTimestampNoRepeat
            (Connection connection, long start, long end, int limit) {
        ArrayList<PlayerData> fastestAnswerers = new ArrayList<>();
        if(limit == 0) {
            return fastestAnswerers;
        }
        Set<Integer> contestWinners = new HashSet<>();
        String sqlQuery = String.format("SELECT user_id, took FROM answers_logs WHERE timestamp >= %d AND timestamp <= %d ORDER BY took ASC", start, end);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next() || contestWinners.size() < limit) {
                int holoQuizID = resultSet.getInt("user_id");
                if(contestWinners.contains(holoQuizID)) {
                    continue;
                }
                contestWinners.add(holoQuizID);
                int took = resultSet.getInt("took");
                String playerName = databaseManager.getPlayerNameByHoloQuizID(connection, holoQuizID);
                PlayerData contestWinner = new PlayerData(playerName, took, -1, -1, holoQuizID);
                fastestAnswerers.add(contestWinner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fastestAnswerers;
    }
    */
}
