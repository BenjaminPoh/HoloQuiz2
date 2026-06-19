package benloti.holoquiz.database;
import benloti.holoquiz.files.Logger;

import java.sql.*;
import java.util.ArrayList;

import java.sql.Connection;

public class BanList {
    private static final String SQL_STATEMENT_CREATE_BAN_TABLE =
            "CREATE TABLE IF NOT EXISTS ban_list (user_id INT , timestamp BIGINT)";
    private static final String SQL_STATEMENT_ADD_BANNED_PLAYER =
            "INSERT INTO ban_list (user_id, timestamp) VALUES (?, ?)";
    private static final String SQL_STATEMENT_REMOVE_BANNED_PLAYER =
            "DELETE FROM ban_list WHERE user_id = ?";
    private static final String SQL_STATEMENT_FETCH_ALL_BANNED_PLAYERS =
            "SELECT * FROM ban_list";

    public BanList(Connection connection) {
        createTable(connection);
    }

    public boolean addBannedPlayer(Connection connection, int userID, long timeStamp) {
        try (PreparedStatement logsStatement = connection.prepareStatement(SQL_STATEMENT_ADD_BANNED_PLAYER)) {
            logsStatement.setInt(1, userID);
            logsStatement.setLong(2, timeStamp);
            logsStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            Logger.getLogger().dumpStackTrace(e);
            return false;
        }
    }

    public boolean removeBannedPlayer(Connection connection, int userID) {
        try (PreparedStatement logsStatement = connection.prepareStatement(SQL_STATEMENT_REMOVE_BANNED_PLAYER)) {
            logsStatement.setInt(1, userID);
            logsStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            Logger.getLogger().dumpStackTrace(e);
            return false;
        }
    }

    public ArrayList<Integer> getAllBannedPlayers(Connection connection) {
        ArrayList<Integer> bannedIDs = new ArrayList<>();
        try {
            PreparedStatement fetchPlayerStatsQuery = connection.prepareStatement(SQL_STATEMENT_FETCH_ALL_BANNED_PLAYERS);
            ResultSet resultSet = fetchPlayerStatsQuery.executeQuery();
            while (resultSet.next()) {
                int holoQuizID = resultSet.getInt("user_id");
                bannedIDs.add(holoQuizID);
            }
            return bannedIDs;
        } catch (SQLException e) {
            Logger.getLogger().dumpStackTrace(e);
            return bannedIDs;
        }
    }

    private void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(SQL_STATEMENT_CREATE_BAN_TABLE);
        } catch (SQLException e) {
            Logger.getLogger().dumpStackTrace(e);
        }
    }
}
