package benloti.holoquiz.database;
import benloti.holoquiz.structs.PlayerContestStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    private static final String SQL_STATEMENT_FETCH_BEST_X_ANSWERS_WITHIN_TIMESTAMP =
            "WITH ranked_answers AS (SELECT user_id, took, COUNT(*) OVER (PARTITION BY user_id) AS total, " +
                    "ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY took ASC) AS rank " +
                    "FROM answers_logs WHERE timestamp >= %d AND timestamp <= %d) " +
            "SELECT user_id, (SUM(took)/COUNT (*)) as score, MAX(took) as tti FROM ranked_answers " +
                    "WHERE rank <= %d and total >= %d GROUP BY user_id ORDER BY score ASC LIMIT %d";

    private static final String SQL_STATEMENT_FETCH_PLAYER_STATS_WITHIN_TIMESTAMP =
            "SELECT (SUM(took)/COUNT (*)) as average, COUNT (*) as ans_count, MIN(took) as best_time " +
            "FROM answers_logs WHERE timestamp >= %d AND timestamp <= %d AND user_id = %d";
    private static final String SQL_STATEMENT_FETCH_PLAYER_BEST_X_WITHIN_TIMESTAMP =
            "WITH top_answers AS (SELECT took FROM answers_logs " +
                    "WHERE timestamp >= %d AND timestamp <= %d AND user_id = %d ORDER BY took ASC LIMIT %d) " +
             "SELECT (SUM(took)/COUNT (*)) as score, MAX(took) as tti FROM top_answers";

    private static final String SQL_STATEMENT_FETCH_PREVIOUS_TIMINGS =
            "SELECT took FROM answers_logs WHERE user_id = %d ORDER BY timestamp DESC LIMIT %d";

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

    public ArrayList<PlayerContestStats> getTopAnswerersWithinTimestamp (Connection connection, long start, long end, int limit) {
        ArrayList<PlayerContestStats> winnersOfContest = new ArrayList<>();
        if(limit == 0) {
            return winnersOfContest;
        }
        String sqlQuery = String.format(SQL_STATEMENT_FETCH_MOST_ANSWERS_WITHIN_TIMESTAMP, start, end, limit);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                int holoQuizID = resultSet.getInt("user_id");
                int answers = resultSet.getInt("ans_count");
                String playerName = databaseManager.getPlayerNameByHoloQuizID(connection, holoQuizID);
                PlayerContestStats contestWinner = new PlayerContestStats(playerName, holoQuizID, answers, -1, -1, -1, -1);
                winnersOfContest.add(contestWinner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return winnersOfContest;
    }

    public ArrayList<PlayerContestStats> getFastestAnswerersWithinTimestamp(Connection connection, long start, long end, int limit) {
        ArrayList<PlayerContestStats> winnersOfContest = new ArrayList<>();
        if(limit == 0) {
            return winnersOfContest;
        }
        String sqlQuery = String.format(SQL_STATEMENT_FETCH_FASTEST_ANSWERS_WITHIN_TIMESTAMP, start, end, limit);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                int holoQuizID = resultSet.getInt("user_id");
                int took = resultSet.getInt("best_time");
                String playerName = databaseManager.getPlayerNameByHoloQuizID(connection, holoQuizID);
                PlayerContestStats contestWinner = new PlayerContestStats(playerName, holoQuizID,-1, took, -1, -1, -1);
                winnersOfContest.add(contestWinner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return winnersOfContest;
    }

    public ArrayList<PlayerContestStats> getBestAnswerersWithinTimestamp(Connection connection, long start, long end, int limit, int minReq) {
        ArrayList<PlayerContestStats> winnersOfContest = new ArrayList<>();
        if(limit == 0) {
            return winnersOfContest;
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
                PlayerContestStats contestWinner = new PlayerContestStats(playerName, holoQuizID,answers, -1, average, -1, -1);
                winnersOfContest.add(contestWinner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return winnersOfContest;
    }

    public ArrayList<PlayerContestStats> getBestXWithinTimestamp(Connection connection, long start, long end, int limit, int minReq) {
        ArrayList<PlayerContestStats> winnersOfContest = new ArrayList<>();
        if(limit == 0) {
            return winnersOfContest;
        }
        String sqlQuery = String.format(SQL_STATEMENT_FETCH_BEST_X_ANSWERS_WITHIN_TIMESTAMP, start, end, minReq, minReq, limit);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                int holoQuizID = resultSet.getInt("user_id");
                int avgOfBestX = resultSet.getInt("score");
                int tti =  resultSet.getInt("tti");
                String playerName = databaseManager.getPlayerNameByHoloQuizID(connection, holoQuizID);
                PlayerContestStats contestWinner = new PlayerContestStats(playerName, holoQuizID, -1, -1, -1, avgOfBestX, tti);
                winnersOfContest.add(contestWinner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return winnersOfContest;
    }

    public PlayerContestStats getPlayerStatsWithinTimestamp(Connection connection, long start, long end, int id, int minReq, String name) {
        String sqlQuery = String.format(SQL_STATEMENT_FETCH_PLAYER_STATS_WITHIN_TIMESTAMP, start, end, id);
        String sqlQuery2 = String.format(SQL_STATEMENT_FETCH_PLAYER_BEST_X_WITHIN_TIMESTAMP,start, end, id, minReq);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                int best = resultSet.getInt("best_time");
                int answers = resultSet.getInt("ans_count");
                int average = resultSet.getInt("average");
                PreparedStatement statement2 = connection.prepareStatement(sqlQuery2);
                ResultSet resultSet2 = statement2.executeQuery();
                int avgOfBestX = resultSet2.getInt("score");
                int tti =  resultSet2.getInt("tti");
                return new PlayerContestStats(name, id, answers, best, average, avgOfBestX, tti);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PlayerContestStats(name, id, -1, -1, -1 ,-1 , -1);
    }

    public List<Double> getPreviousTimings(Connection connection, int id, int count) {
        List<Double> list = new ArrayList<>();
        String sqlQuery = String.format(SQL_STATEMENT_FETCH_PREVIOUS_TIMINGS, id, count);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                double time = resultSet.getInt("took") / 1000.0;
                list.add(time);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
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
