package benloti.holoquiz.database;

import benloti.holoquiz.structs.ContestInfo;
import benloti.holoquiz.structs.PlayerContestStats;

import java.sql.*;
import java.util.ArrayList;

public class Contests {
    private static final String SQL_STATEMENT_CREATE_CONTEST_LOG_TABLE =
            "CREATE TABLE IF NOT EXISTS contest_log (type INT, category INT, start BIGINT, end BIGINT, user_id INT, position INT)";

    private static final String SQL_STATEMENT_ADD_CONTEST_WINNER =
            "INSERT INTO contest_log (type, category, start, end, user_id, position) VALUES (?, ?, ?, ?, ?, ?)";

    public Contests(Connection connection) {
        createTable(connection);
    }

    public void logContestWinners(Connection connection, ArrayList<PlayerContestStats> contestWinners, ContestInfo contestInfo, int contestCategory) {
        try {
            int position = 1;
            for (PlayerContestStats contestWinner: contestWinners) {
                PreparedStatement statement = connection.prepareStatement(SQL_STATEMENT_ADD_CONTEST_WINNER);
                statement.setInt(1, contestInfo.getTypeCode());
                statement.setInt(2,contestCategory);
                statement.setLong(3, contestInfo.getStartTime());
                statement.setLong(4, contestInfo.getEndTime());
                statement.setInt(5, contestWinner.getHoloQuizID());
                statement.setInt(6, position);
                statement.executeUpdate();
                position += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(SQL_STATEMENT_CREATE_CONTEST_LOG_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
