package benloti.holoquiz.database;

import org.bukkit.Bukkit;

import java.sql.*;

public class UserInfo {
    private static final String SQL_STATEMENT_CREATE_USERINFO_TABLE =
            "CREATE TABLE IF NOT EXISTS user_info (user_id INT , player_uuid STRING, username STRING)";

    private static final String SQL_STATEMENT_OBTAIN_USER_ID =
            "SELECT * FROM user_info WHERE player_uuid = '%s'";
    private static final String SQL_STATEMENT_ADD_NEW_USER_INFO =
            "INSERT INTO user_info (user_id, player_uuid, username) VALUES (?, ?, ?)";
    private static final String SQL_STATEMENT_FIND_SIZE =
            "SELECT COUNT (user_id) FROM user_info";
    private static final String SQL_STATEMENT_OBTAIN_ALL_USER_NAME =
            "SELECT * FROM user_info";

    private static final String ERROR_MSG_UUID_USERNAME_MISMATCH =
            "Supposed to update table. Not important at this point given how minecraft works and this is intended for HoloCraft, which is a cracked server";
    public static final String SQL_STATEMENT_OBTAIN_HOLOQUIZ_ID = "SELECT * FROM user_info WHERE username = '%s'";


    public UserInfo(Connection connection) {
        createTable(connection);
    }

    public void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(SQL_STATEMENT_CREATE_USERINFO_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getHoloQuizIDByUUID(Connection connection, String PlayerUUID, String PlayerName, int numberOfEntries) {
        String firstStatement = String.format(SQL_STATEMENT_OBTAIN_USER_ID, PlayerUUID);
        try {
            PreparedStatement statement = connection.prepareStatement(firstStatement);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String savedName = resultSet.getString("username");
                if (!savedName.equals(PlayerName)) {
                    Bukkit.getLogger().info(ERROR_MSG_UUID_USERNAME_MISMATCH);
                }
                return resultSet.getInt("user_id");
            } else {
                PreparedStatement infoStatement = connection.prepareStatement(SQL_STATEMENT_ADD_NEW_USER_INFO);
                int newUserID = numberOfEntries + 1;
                infoStatement.setInt(1, newUserID);
                infoStatement.setString(2, PlayerUUID);
                infoStatement.setString(3, PlayerName);
                infoStatement.executeUpdate();
                return newUserID;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSize(Connection connection) {
        try {
            PreparedStatement assignIDStatement = connection.prepareStatement(SQL_STATEMENT_FIND_SIZE);
            ResultSet resultSet = assignIDStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getHoloQuizIDByUserName(Connection connection, String playerName) {
        try {
            String fetchHoloQuizIDStatement = String.format(SQL_STATEMENT_OBTAIN_HOLOQUIZ_ID, playerName);
            PreparedStatement fetchHoloQuizIDQuery = connection.prepareStatement(fetchHoloQuizIDStatement);
            ResultSet resultSet = fetchHoloQuizIDQuery.executeQuery();
            boolean resultExists = resultSet.next();
            if (!resultExists) {
                return 0;
            }
            return resultSet.getInt("user_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String[] getAllPlayerNames(Connection connection, int size) {
        String[] allPlayerNamesByHoloQuizID = new String[size];
        try {
            PreparedStatement fetchAllPlayerNamesQuery = connection.prepareStatement(SQL_STATEMENT_OBTAIN_ALL_USER_NAME);
            ResultSet resultSet = fetchAllPlayerNamesQuery.executeQuery();
            while(resultSet.next()) {
                int holoQuizID = resultSet.getInt("user_id");
                String userName = resultSet.getString("username");
                allPlayerNamesByHoloQuizID[holoQuizID - 1] = userName;
            }
            return allPlayerNamesByHoloQuizID;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
