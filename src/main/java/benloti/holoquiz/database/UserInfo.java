package benloti.holoquiz.database;

import org.bukkit.Bukkit;

import java.sql.*;

public class UserInfo {

    private static final String SQL_STATEMENT_CREATE_USERINFO_TABLE =
            "CREATE TABLE IF NOT EXISTS user_info " +
                    "(user_id INT PRIMARY KEY, player_uuid varchar(36), username varchar(20))";
    private static final String SQL_STATEMENT_OBTAIN_USER_ID =
            "SELECT * FROM user_info WHERE player_uuid = ?";
    private static final String SQL_STATEMENT_ADD_NEW_USER_INFO =
            "INSERT INTO user_info (user_id, player_uuid, username) VALUES (?, ?, ?)";
    private static final String SQL_STATEMENT_OBTAIN_USER_NAME =
            "SELECT username FROM user_info WHERE user_id = ?";

    private static final String SQL_STATEMENT_FIND_SIZE =
            "SELECT COUNT (user_id) FROM user_info";
    private static final String SQL_STATEMENT_OBTAIN_ALL_USER_NAME =
            "SELECT * FROM user_info";
    private static final String SQL_STATEMENT_UPDATE_USER_NAME =
            "UPDATE user_info SET username = ? WHERE player_uuid = ?";
    private static final String SQL_STATEMENT_OBTAIN_HOLOQUIZ_ID =
            "SELECT * FROM user_info WHERE username = ?";

    private static final String LOG_MSG_UPDATED_USERNAME_FOR_UUID_SUCCESS =
            "[HoloQuiz] Detected that Player %s changed name to %s. New name will now be used.";
    private static final String LOG_MSG_UPDATED_USERNAME_FOR_UUID_FAILED =
            "[HoloQuiz] Error: Detected that Player %s changed name to %s, but NOT updated due to unknown error";

    public UserInfo(Connection connection) {
        createTable(connection);
    }

    private void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(SQL_STATEMENT_CREATE_USERINFO_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getHoloQuizIDByUUID(Connection connection, String PlayerUUID, String PlayerName) {
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_STATEMENT_OBTAIN_USER_ID);
            statement.setString(1, PlayerUUID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String savedName = resultSet.getString("username");
                updateUsernameForUUID(connection, PlayerName, PlayerUUID, savedName);
                return resultSet.getInt("user_id");
            } else {
                PreparedStatement infoStatement = connection.prepareStatement(SQL_STATEMENT_ADD_NEW_USER_INFO);
                int newId = getSize(connection) + 1;
                Bukkit.getLogger().info("[HoloQuiz] Assigning new player with ID:" + newId);
                if(newId == 0) {
                    Bukkit.getLogger().info("[HoloQuiz] Error: If you see this, I retire from coding");
                }
                infoStatement.setInt(1, newId);
                infoStatement.setString(2, PlayerUUID);
                infoStatement.setString(3, PlayerName);
                infoStatement.executeUpdate();
                return newId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateUsernameForUUID(Connection connection, String newName, String playerUUID, String oldName) {
        if (newName.equals(oldName)) {
            return;
        }

        try {
            PreparedStatement updateStatement = connection.prepareStatement(SQL_STATEMENT_UPDATE_USER_NAME);
            updateStatement.setString(1, newName);
            updateStatement.setString(2, playerUUID);
            updateStatement.executeUpdate();
            String logMessage = String.format(LOG_MSG_UPDATED_USERNAME_FOR_UUID_SUCCESS, newName, oldName);
            Bukkit.getLogger().info(logMessage);
        } catch (SQLException e) {
            e.printStackTrace();
            String logMessage = String.format(LOG_MSG_UPDATED_USERNAME_FOR_UUID_FAILED, newName, oldName);
            Bukkit.getLogger().info(logMessage);
        }
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
            PreparedStatement statement = connection.prepareStatement(SQL_STATEMENT_OBTAIN_HOLOQUIZ_ID);
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
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

    public String getPlayerNameByHoloQuizID(Connection connection, int holoQuizID) {
        try {
            PreparedStatement statement = connection.prepareStatement(  SQL_STATEMENT_OBTAIN_USER_NAME);
            statement.setInt(1, holoQuizID);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.getString("username");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
