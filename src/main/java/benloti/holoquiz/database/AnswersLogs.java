package benloti.holoquiz.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class AnswersLogs {
    private static final String SQL_STATEMENT_CREATE_LOGS_TABLE =
            "CREATE TABLE IF NOT EXISTS answers_logs (user_id INT , timestamp BIGINT, took INT, mode varchar(1))";
    private static final String SQL_STATEMENT_UPDATE_LOGS =
            "INSERT INTO answers_logs (user_id, timestamp, took, mode) VALUES (?, ?, ?, ?)";

    public AnswersLogs(Connection connection) {
        createTable(connection);
    }

    public void createTable(Connection connection) {
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

}
