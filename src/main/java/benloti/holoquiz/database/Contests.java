package benloti.holoquiz.database;

import benloti.holoquiz.structs.ContestInfo;
import benloti.holoquiz.structs.PlayerData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Contests {
    private static final String SQL_STATEMENT_CREATE_CONTEST_INFO_TABLE =
            "CREATE TABLE IF NOT EXISTS contest_info (type INT PRIMARY KEY , end BIGINT)";
    private static final String SQL_STATEMENT_CREATE_CONTEST_LOG_TABLE =
            "CREATE TABLE IF NOT EXISTS contest_log (type INT, category INT, start BIGINT, end BIGINT, user_id INT, position INT)";

    private static final String SQL_STATEMENT_FETCH_ONGOING_CONTESTS =
            "SELECT * FROM contest_info";
    private static final String SQL_STATEMENT_DELETE_ONGOING_CONTEST =
            "DELETE FROM contest_info WHERE type = ?";
    private static final String SQL_STATEMENT_ADD_ONGOING_CONTEST =
            "INSERT INTO contest_info (type, end) VALUES (?, ?)";
    private static final String SQL_STATEMENT_UPDATE_CONTEST =
            "UPDATE contest_info SET end = ? WHERE type = ?";
    private static final String SQL_STATEMENT_ADD_CONTEST_WINNER =
            "INSERT INTO contest_log (type, category, start, end, user_id, position) VALUES (?, ?, ?, ?, ?, ?)";

    public Contests(Connection connection) {
        createTable(connection);
    }

    public ArrayList<Long> getOngoingTournaments(Connection connection) {
        ArrayList<Long> incompleteTournamentsCode = new ArrayList<>(Arrays.asList(0L, 0L, 0L));
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_STATEMENT_FETCH_ONGOING_CONTESTS);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int type = resultSet.getInt("type");
                long endTime = resultSet.getLong("end");
                incompleteTournamentsCode.set(type,endTime);
            }
            return incompleteTournamentsCode;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return incompleteTournamentsCode;
    }

    public void deleteOngoingContest(Connection connection, int contestType) {
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_STATEMENT_DELETE_ONGOING_CONTEST);
            statement.setInt(1, contestType);
            statement.executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void createOngoingContest(Connection connection, ContestInfo contestInfo) {
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_STATEMENT_ADD_ONGOING_CONTEST);
            statement.setInt(1, contestInfo.getTypeCode());
            statement.setLong(2, contestInfo.getEndTime());
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logContestWinners(Connection connection, ArrayList<PlayerData> contestWinners, ContestInfo contestInfo, int contestCategory) {
        try {
            int position = 1;
            for (PlayerData contestWinner: contestWinners) {
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

    public void updateContestInfo(Connection connection, int code, long newEndTime) {
        try {
            PreparedStatement statsSQLQuery = connection.prepareStatement(SQL_STATEMENT_UPDATE_CONTEST);
            statsSQLQuery.setLong(1, newEndTime);
            statsSQLQuery.setInt(2, code);
            statsSQLQuery.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(SQL_STATEMENT_CREATE_CONTEST_INFO_TABLE);
            Statement statement2 = connection.createStatement();
            statement2.executeUpdate(SQL_STATEMENT_CREATE_CONTEST_LOG_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
