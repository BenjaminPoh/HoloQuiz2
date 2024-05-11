package benloti.holoquiz.database;

import benloti.holoquiz.structs.ContestInfo;
import benloti.holoquiz.structs.PlayerData;

import java.sql.*;
import java.util.ArrayList;

public class Contests {
    //TODO: Implement table for logs of winners
    //SCHEMA- ContestTimes : START, END, A/B/T, (D/W/M), ContestID (INT), Concluded
    //SCHEMA- ContestWinners : ContestID, HoloQuizID
    private static final String SQL_STATEMENT_CREATE_CONTEST_INFO_TABLE =
            "CREATE TABLE IF NOT EXISTS contest_info (type VARCHAR(1) PRIMARY KEY , start BIGINT, end BIGINT)";
    private static final String SQL_STATEMENT_CREATE_CONTEST_LOG_TABLE =
            "CREATE TABLE IF NOT EXISTS contest_log (type VARCHAR(2), start DATE, user_id INT, position INT)";

    private static final String SQL_STATEMENT_FETCH_ONGOING_CONTESTS =
            "SELECT * FROM contest_info";
    private static final String SQL_STATEMENT_DELETE_ONGOING_CONTEST =
            "DELETE FROM contest_info WHERE type = ?";
    private static final String SQL_STATEMENT_ADD_ONGOING_CONTEST =
            "INSERT INTO contest_info (type, start, end) VALUES (?, ?, ?)";
    private static final String SQL_STATEMENT_ADD_CONTEST_WINNER =
            "INSERT INTO contest_log (type, start, user_id, position) VALUES (?, ?, ?, ?)";
    private static final String SQL_STATEMENT_UPDATE_CONTEST =
            "UPDATE contest_info SET start = ?, end = ? WHERE type = ?";

    public Contests(Connection connection) {
        createTable(connection);
    }

    public ArrayList<ContestInfo> getOngoingTournaments(Connection connection) {
        ArrayList<ContestInfo> incompleteTournamentsCode = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_STATEMENT_FETCH_ONGOING_CONTESTS);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String type = resultSet.getString("type");
                long startTime = resultSet.getLong("start");
                long endTime = resultSet.getLong("end");
                ContestInfo temp = new ContestInfo(startTime, endTime, type, -1, null, null, null);
                incompleteTournamentsCode.add(temp);
            }
            return incompleteTournamentsCode;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return incompleteTournamentsCode;
    }

    public void deleteOngoingContest(Connection connection, String contestType) {
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_STATEMENT_DELETE_ONGOING_CONTEST);
            statement.setString(1, contestType);
            statement.executeQuery();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void createOngoingContest(Connection connection, ContestInfo contestInfo) {
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_STATEMENT_ADD_ONGOING_CONTEST);
            statement.setString(1, contestInfo.getType());
            statement.setLong(2, contestInfo.getStartTime());
            statement.setLong(3, contestInfo.getEndTime());
            statement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addContestWinners(Connection connection, ArrayList<PlayerData> contestWinners, ContestInfo contestInfo, String contestCategory) {
        try {
            int position = 1;
            String contestCode = contestInfo.getType() + contestCategory;
            for (PlayerData contestWinner: contestWinners) {
                PreparedStatement statement = connection.prepareStatement(SQL_STATEMENT_ADD_CONTEST_WINNER);
                statement.setString(1, contestCode);
                statement.setLong(2, contestInfo.getStartTime());
                statement.setInt(3, contestWinner.getHoloQuizID());
                statement.setInt(4, position);
                statement.executeQuery();
                position += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateContestInfo(Connection connection, ContestInfo endedContest, long newEndTime) {
        long newStartTime = endedContest.getEndTime();
        String contestCode = endedContest.getType();
        try {
            PreparedStatement statsSQLQuery = connection.prepareStatement(SQL_STATEMENT_UPDATE_CONTEST);
            statsSQLQuery.setLong(1, newStartTime);
            statsSQLQuery.setLong(2, newEndTime);
            statsSQLQuery.setString(3, contestCode);
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
