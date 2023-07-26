package benloti.holoquiz.database;

import benloti.holoquiz.structs.PlayerSettings;

import java.sql.*;
import java.util.HashMap;

public class UserPersonalisation {
    private final HashMap<String, PlayerSettings> userSettings;
    private final HashMap<String, PlayerSettings> updateList;
    private final HashMap<String, PlayerSettings> addList;

    private final Connection connection;

    private static final String SQL_STATEMENT_CREATE_USER_PERSONALISATION_TABLE =
            "CREATE TABLE IF NOT EXISTS user_personalisation (player_uuid STRING , toggle_quiz BOOL, suffix STRING)";
    private static final String SQL_STATEMENT_FETCH_ALL_INFO =
            "SELECT * FROM user_personalisation";
    private static final String SQL_STATEMENT_UPDATE_INFO =
            "UPDATE user_personalisation SET toggle_quiz = ?, suffix = ? WHERE player_uuid = ?";
    private static final String SQL_STATEMENT_ADD_INFO =
            "INSERT INTO user_personalisation (player_uuid, toggle_quiz, suffix) VALUES (?, ?, ?)";

    public UserPersonalisation(Connection connection) {
        createTable(connection);
        this.connection = connection;
        this.userSettings = new HashMap<>();
        initialiseHashMap(connection);
        this.updateList = new HashMap<>();
        this.addList = new HashMap<>();
    }
    public void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(SQL_STATEMENT_CREATE_USER_PERSONALISATION_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initialiseHashMap(Connection connection) {
        try {
            PreparedStatement fetchPlayerStatsQuery = connection.prepareStatement(SQL_STATEMENT_FETCH_ALL_INFO);
            ResultSet resultSet = fetchPlayerStatsQuery.executeQuery();
            while(resultSet.next()) {
                String player_uuid = resultSet.getString("player_uuid");
                boolean quizEnabled = resultSet.getBoolean("toggle_quiz");
                String suffix = resultSet.getString("suffix");
                PlayerSettings playerSetting = new PlayerSettings(suffix, quizEnabled);
                userSettings.put(player_uuid, playerSetting);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setSuffix(String player_uuid, String suffix) {
        PlayerSettings newPlayerSettings;
        //Check if in master listing. If not, the player must be a new entry, and must be added to the addList.
        if(!userSettings.containsKey(player_uuid)) {
            newPlayerSettings = new PlayerSettings(suffix, true);
            userSettings.put(player_uuid,newPlayerSettings);
            addList.put(player_uuid,newPlayerSettings);
            return;
        }
        //Player is guaranteed in master listing. Check if player info is in addList or updateList.
        newPlayerSettings = userSettings.get(player_uuid);
        newPlayerSettings.setSuffix(suffix);

        userSettings.replace(player_uuid,newPlayerSettings);
        if(addList.containsKey(player_uuid)) {
            addList.replace(player_uuid,newPlayerSettings);
            return;
        }
        updateList.put(player_uuid,newPlayerSettings);
    }

    public void setNotificationSetting(String player_uuid, boolean notification) {
        PlayerSettings newPlayerSettings;
        if(!userSettings.containsKey(player_uuid)) {
            newPlayerSettings = new PlayerSettings("", notification);
            userSettings.put(player_uuid, newPlayerSettings);
            addList.put(player_uuid, newPlayerSettings);
            return;
        }
        newPlayerSettings = userSettings.get(player_uuid);
        newPlayerSettings.setNotificationSetting(notification);
        userSettings.replace(player_uuid,newPlayerSettings);

        if(addList.containsKey(player_uuid)) {
            addList.replace(player_uuid,newPlayerSettings);
            return;
        }
        updateList.put(player_uuid,newPlayerSettings);
    }

    public PlayerSettings getPlayerSettings(String player_uuid) {
        if(!userSettings.containsKey(player_uuid)) {
            return null;
        }
        return userSettings.get(player_uuid);
    }

    public void savePlayerSettings() {
        for(String key: updateList.keySet()) {
            PlayerSettings currentPlayer = updateList.get(key);
            try {
                PreparedStatement statsSQLQuery = connection.prepareStatement(SQL_STATEMENT_UPDATE_INFO);
                statsSQLQuery.setBoolean(1, currentPlayer.isNotificationEnabled());
                statsSQLQuery.setString(2, currentPlayer.getSuffix());
                statsSQLQuery.setString(3, key);
                statsSQLQuery.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        for(String key: addList.keySet()) {
            PlayerSettings currentPlayer = addList.get(key);
            try {
                PreparedStatement statsSQLQuery = connection.prepareStatement(SQL_STATEMENT_ADD_INFO);
                statsSQLQuery.setString(1, key);
                statsSQLQuery.setBoolean(2, currentPlayer.isNotificationEnabled());
                statsSQLQuery.setString(3, currentPlayer.getSuffix());
                statsSQLQuery.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    /*
    public String prefixAdder(String unformattedString, String suffix) {
        return unformattedString + suffix;
    }
     */

}
