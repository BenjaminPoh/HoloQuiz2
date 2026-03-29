package benloti.holoquiz.database;

import benloti.holoquiz.files.Logger;
import benloti.holoquiz.structs.PlayerSettings;

import java.sql.*;
import java.util.HashMap;

public class UserPersonalisation {
    private final HashMap<String, PlayerSettings> userSettingsMap;
    private final HashMap<String, PlayerSettings> localUpdateList;
    private final HashMap<String, PlayerSettings> localAddList;

    private final Connection connection;

    private static final String SQL_STATEMENT_CREATE_USER_PERSONALISATION_TABLE =
            "CREATE TABLE IF NOT EXISTS user_personalisation (player_uuid STRING , toggle_quiz BOOL, suffix STRING, toggle_alert BOOL)";
    private static final String SQL_STATEMENT_FETCH_ALL_INFO =
            "SELECT * FROM user_personalisation";
    private static final String SQL_STATEMENT_UPDATE_INFO =
            "UPDATE user_personalisation SET toggle_quiz = ?, suffix = ?, toggle_alert = ? WHERE player_uuid = ?";
    private static final String SQL_STATEMENT_ADD_INFO =
            "INSERT INTO user_personalisation (player_uuid, toggle_quiz, suffix, toggle_alert) VALUES (?, ?, ?, ?)";

    public UserPersonalisation(Connection connection) {
        createTable(connection);
        this.connection = connection;
        this.userSettingsMap = new HashMap<>();
        initialiseHashMap(connection);
        this.localUpdateList = new HashMap<>();
        this.localAddList = new HashMap<>();
    }

    public void setSuffix(String player_uuid, String suffix) {
        if(!userSettingsMap.containsKey(player_uuid)) {
            createPlayerSettings(player_uuid, suffix, true, false);
            return;
        }
        PlayerSettings newPlayerSettings = userSettingsMap.get(player_uuid);
        newPlayerSettings.setSuffix(suffix);
        updateLocallyStoredInfo(player_uuid, newPlayerSettings);
    }

    public PlayerSettings toggleNotificationSetting(String player_uuid) {
        if(!userSettingsMap.containsKey(player_uuid)) {
            return createPlayerSettings(player_uuid, "", false, false);
        }
        PlayerSettings currentSettings = userSettingsMap.get(player_uuid);
        currentSettings.setNotificationSetting(!currentSettings.isNotificationEnabled());
        updateLocallyStoredInfo(player_uuid, currentSettings);
        return currentSettings;
    }

    public PlayerSettings toggleAlertSetting(String player_uuid) {
        if(!userSettingsMap.containsKey(player_uuid)) {
            return createPlayerSettings(player_uuid, "", true, true);
        }
        PlayerSettings currentSettings = userSettingsMap.get(player_uuid);
        currentSettings.setAlertSetting(!currentSettings.isAlertEnabled());
        updateLocallyStoredInfo(player_uuid, currentSettings);
        return currentSettings;
    }


    public PlayerSettings getPlayerSettings(String player_uuid) {
        if(!userSettingsMap.containsKey(player_uuid)) {
            return null;
        }
        return userSettingsMap.get(player_uuid);
    }

    //Called when HoloQuiz is disabled to save all changes
    public void savePlayerSettings() {
        for(String key: localUpdateList.keySet()) {
            PlayerSettings currentPlayer = localUpdateList.get(key);
            try {
                PreparedStatement statsSQLQuery = connection.prepareStatement(SQL_STATEMENT_UPDATE_INFO);
                statsSQLQuery.setBoolean(1, currentPlayer.isNotificationEnabled());
                statsSQLQuery.setString(2, currentPlayer.getSuffix());
                statsSQLQuery.setBoolean(3, currentPlayer.isAlertEnabled());
                statsSQLQuery.setString(4, key);
                statsSQLQuery.executeUpdate();
            } catch (SQLException e) {
                Logger.getLogger().dumpStackTrace(e);
            }
        }

        for(String key: localAddList.keySet()) {
            PlayerSettings currentPlayer = localAddList.get(key);
            try {
                PreparedStatement statsSQLQuery = connection.prepareStatement(SQL_STATEMENT_ADD_INFO);
                statsSQLQuery.setString(1, key);
                statsSQLQuery.setBoolean(2, currentPlayer.isNotificationEnabled());
                statsSQLQuery.setString(3, currentPlayer.getSuffix());
                statsSQLQuery.setBoolean(4, currentPlayer.isAlertEnabled());
                statsSQLQuery.executeUpdate();
            } catch (SQLException e) {
                Logger.getLogger().dumpStackTrace(e);
            }
        }
    }

    private void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(SQL_STATEMENT_CREATE_USER_PERSONALISATION_TABLE);
        } catch (SQLException e) {
            Logger.getLogger().dumpStackTrace(e);
        }
    }

    private void initialiseHashMap(Connection connection) {
        try {
            PreparedStatement fetchPlayerStatsQuery = connection.prepareStatement(SQL_STATEMENT_FETCH_ALL_INFO);
            ResultSet resultSet = fetchPlayerStatsQuery.executeQuery();
            while(resultSet.next()) {
                String player_uuid = resultSet.getString("player_uuid");
                boolean quizEnabled = resultSet.getBoolean("toggle_quiz");
                boolean alertEnabled = resultSet.getBoolean("toggle_alert");
                String suffix = resultSet.getString("suffix");
                PlayerSettings playerSetting = new PlayerSettings(suffix, quizEnabled, alertEnabled);
                userSettingsMap.put(player_uuid, playerSetting);
            }
        } catch (SQLException e) {
            Logger.getLogger().dumpStackTrace(e);
        }
    }

    private PlayerSettings createPlayerSettings(String player_uuid, String suffix, boolean notificationsEnabled, boolean alertEnabled) {
        PlayerSettings defaultSettings = new PlayerSettings(suffix, notificationsEnabled, alertEnabled);
        userSettingsMap.put(player_uuid,defaultSettings);
        localAddList.put(player_uuid,defaultSettings);
        return defaultSettings;
    }

    private void updateLocallyStoredInfo(String player_uuid, PlayerSettings newPlayerSettings) {
        //Update current state
        userSettingsMap.replace(player_uuid,newPlayerSettings);
        //Update Changes List
        if(localAddList.containsKey(player_uuid)) {
            localAddList.replace(player_uuid,newPlayerSettings);
            return;
        }
        localUpdateList.put(player_uuid,newPlayerSettings);
    }


}
