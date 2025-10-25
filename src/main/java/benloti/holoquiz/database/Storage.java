package benloti.holoquiz.database;

import benloti.holoquiz.files.Logger;
import benloti.holoquiz.structs.RewardTier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Storage {
    private static final String SQL_STATEMENT_CREATE_STORAGE_TABLE =
            "CREATE TABLE IF NOT EXISTS storage " +
                    "(user_id INT, type varchar(1), contents varchar(256), details TEXT, qty INT)";
    private static final String SQL_STATEMENT_ADD_NEW_ITEM =
            "INSERT INTO storage (user_id, type, contents, details, qty) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_STATEMENT_RETRIEVE_ITEM =
            "SELECT * FROM storage WHERE user_id = ?";
    private static final String SQL_STATEMENT_REMOVE_CLAIMED_ITEM =
            "DELETE FROM storage WHERE user_id = ?";

    public static final String ERROR_INVALID_STORAGE_FORMAT = "Invalid Storage format. You messed with the database... didnt you?";

    public Storage(Connection connection) {
        createTable(connection);
    }

    private void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(SQL_STATEMENT_CREATE_STORAGE_TABLE);
        } catch (SQLException e) {
            Logger.getLogger().dumpStackTrace(e);
        }
    }

    public void addToStorage(Connection connection, int user_id, String type, String contents, String details, int reps) {
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_STATEMENT_ADD_NEW_ITEM);
            statement.setInt(1, user_id);
            statement.setString(2, type);
            statement.setString(3, contents);
            statement.setString(4, details);
            statement.setInt(5, reps);
            statement.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger().dumpStackTrace(e);
        }
    }

    public RewardTier retrieveFromStorage(Connection connection, int user_id) {
        ArrayList<String> cmdList = new ArrayList<>();
        ArrayList<ItemStack> itemList = new ArrayList<>();
        ArrayList<String> msgList = new ArrayList<>();
        double money = 0;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_STATEMENT_RETRIEVE_ITEM);
            statement.setInt(1, user_id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String type = resultSet.getString("type");
                String content = resultSet.getString("contents");
                switch (type) {
                case "V":
                    double value = Double.parseDouble(content);
                    money += value;
                    continue;
                case "C":
                    cmdList.add(content);
                    continue;
                case "I":
                    String lore = resultSet.getString("details");
                    int count = resultSet.getInt("qty");
                    Material itemMaterial = Material.matchMaterial(content);
                    List<String> loreLines = Arrays.asList(lore.split("\n"));
                    ItemStack itemReward = new ItemStack(itemMaterial, count);
                    ItemMeta itemMeta = itemReward.getItemMeta();
                    itemMeta.setLore(loreLines);
                    itemReward.setItemMeta(itemMeta);
                    itemList.add(itemReward);
                    continue;
                case "M":
                    msgList.add(content);
                    continue;
                }
                Logger.getLogger().error(ERROR_INVALID_STORAGE_FORMAT);
            }
            PreparedStatement updateStatement = connection.prepareStatement(SQL_STATEMENT_REMOVE_CLAIMED_ITEM);
            updateStatement.setInt(1, user_id);
            updateStatement.executeUpdate();
        } catch (NumberFormatException | NullPointerException e) {
            Logger.getLogger().error(ERROR_INVALID_STORAGE_FORMAT);
            Logger.getLogger().dumpStackTrace(e);
            return new RewardTier(0, 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        } catch (Exception e) {
            Logger.getLogger().dumpStackTrace(e);
            return new RewardTier(0, 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        return new RewardTier(0, money, cmdList, itemList, msgList);
    }
}
