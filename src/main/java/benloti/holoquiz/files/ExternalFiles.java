package benloti.holoquiz.files;

import benloti.holoquiz.structs.Question;
import benloti.holoquiz.structs.RewardTier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExternalFiles {

    private static final String CONFIG_FILE_NAME = "config.yml";
    private static final String QUESTION_BANK_FILE_NAME = "QuestionBank.yml";
    private static final String REWARDS_FILE_NAME = "Rewards.yml";
    private static final String BACKUP_DIRECTORY_PATH = "backup/";

    private ConfigFile configFile;
    private final ArrayList<RewardTier> allRewards;
    private final ArrayList<Question> allQuestions;

    public ExternalFiles(JavaPlugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
            loadFromResource(plugin, CONFIG_FILE_NAME);
            loadFromResource(plugin, QUESTION_BANK_FILE_NAME);
            loadFromResource(plugin, REWARDS_FILE_NAME);
            loadFromResource(plugin, BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME);
            loadFromResource(plugin, BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME);
            loadFromResource(plugin, BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
        }
        try {
            this.configFile = new ConfigFile(plugin, CONFIG_FILE_NAME);
            updateFile(plugin, BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME, CONFIG_FILE_NAME);
        } catch (Exception e) {
            Bukkit.getLogger().info("[HoloQuiz] Your config.yml file is broken! Loading from backups...");
            this.configFile = new ConfigFile(plugin, BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME);
        }

        this.allRewards = new ArrayList<>();
        try {
            loadRewards (plugin, REWARDS_FILE_NAME);
            updateFile(plugin, BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME, REWARDS_FILE_NAME);
        } catch (Exception e) {
            Bukkit.getLogger().info("[HoloQuiz] Your Rewards.yml file is broken! Loading from backups...");
            loadRewards(plugin, BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
        }

        this.allQuestions = new ArrayList<>();
        try {
            loadQuestions(plugin, QUESTION_BANK_FILE_NAME);
            updateFile(plugin, BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME, QUESTION_BANK_FILE_NAME);
        } catch (Exception e) {
            Bukkit.getLogger().info("[HoloQuiz] Your QuestionBank.yml file is broken! Loading from backups...");
            loadQuestions(plugin, BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
        }
    }

    private void loadQuestions(JavaPlugin plugin, String fileName) {
        File questionYml = new File(plugin.getDataFolder(), fileName);
        FileConfiguration config = YamlConfiguration.loadConfiguration(questionYml);
        for (String key : config.getKeys(false)) {
            ConfigurationSection configSection = config.getConfigurationSection(key);
            questionCategoryLoader(configSection);
        }
    }

    private void questionCategoryLoader(ConfigurationSection config) {
        String questionColourCode = nullReplacer(config.getString("QuestionColour"));
        String messageColourCode = nullReplacer(config.getString("MessageColour"));
        String categoryLabel = nullReplacer(config.getString("CategoryLabel"));
        String categoryPrefix = categoryLabel + questionColourCode;
        ConfigurationSection questionListSection = config.getConfigurationSection("QuestionList");
        questionListLoader(questionListSection, categoryPrefix, messageColourCode);
    }

    private void questionListLoader(ConfigurationSection config, String prefix, String msgColorCode) {
        for (String key : config.getKeys(false)) {
            ConfigurationSection questionConfig = config.getConfigurationSection(key);
            String question = questionConfig.getString("Question");
            List<String> answers = questionConfig.getStringList("Answers");
            if(question == null || answers.size() == 0) {
                Bukkit.getLogger().info("[HoloQuiz] Error with loading question: " + question);
                continue;
            }
            question = prefix + question;
            String message = nullReplacer(questionConfig.getString("Message"));
            message = msgColorCode + " " + message;
            List<String> secretAnswers = questionConfig.getStringList("SecretAnswers");
            String secretMessage = questionConfig.getString("SecretMessage");
            secretMessage = msgColorCode + secretMessage;

            secretAnswers.replaceAll(String::trim);
            answers.replaceAll(String::trim);
            Question newQuestion = new Question(question, answers, message, secretAnswers, secretMessage);

            allQuestions.add(newQuestion);
        }
    }

    private String nullReplacer(String x) {
        if(x == null) {
            return "";
        }
        return x;
    }


    private void loadRewards(JavaPlugin plugin, String fileName) {
        File rewardsYml = new File(plugin.getDataFolder(), fileName);
        FileConfiguration rewardsFile = YamlConfiguration.loadConfiguration(rewardsYml);
        ConfigurationSection rewardsSection = rewardsFile.getConfigurationSection("Rewards");

        for (String key : rewardsSection.getKeys(false)) {
            ConfigurationSection rewardTierSection = rewardsSection.getConfigurationSection(key);
            double maxTime = rewardTierSection.getDouble("MaxAnswerTime");
            int maxTimeInMilliseconds = (int) maxTime * 1000;
            double moneyReward = rewardTierSection.getDouble("Money");
            List<String> commandsExecuted = rewardTierSection.getStringList("Commands");
            ConfigurationSection rewardTierItemSection = rewardTierSection.getConfigurationSection("Items");

            ArrayList<ItemStack> itemReward = new ArrayList<>();
            for (String key2 : rewardTierItemSection.getKeys(false)) {
                ConfigurationSection rewardTierItem = rewardTierItemSection.getConfigurationSection(key2);
                String itemType = rewardTierItem.getString("Material");
                Material itemMaterial = Material.matchMaterial(itemType);
                if (itemMaterial == null) {
                    itemMaterial = Material.CARROT;
                    Bukkit.getLogger().info("[HoloQuiz] Error! Failed to load item of name: " + itemType);
                }
                int itemQty = rewardTierItem.getInt("Qty");
                List<String> itemLore = rewardTierItem.getStringList("Lore");
                ItemStack itemStack = new ItemStack(itemMaterial, itemQty);

                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setLore(itemLore);
                itemStack.setItemMeta(itemMeta);
                itemReward.add(itemStack);
            }

            allRewards.add(new RewardTier(maxTimeInMilliseconds, moneyReward, commandsExecuted, itemReward));
        }
    }

    public ConfigFile getConfigFile() {
        return this.configFile;
    }

    public ArrayList<Question> getAllQuestions() {
        return this.allQuestions;
    }

    public ArrayList<RewardTier> getAllRewards() {
        return this.allRewards;
    }

    private void loadFromResource(JavaPlugin plugin, String fileName) {
        InputStream inputStream = plugin.getResource(fileName);
        assert inputStream != null;
        File createdFile = new File(plugin.getDataFolder(), fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(createdFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFile(JavaPlugin plugin, String oldFile, String newFile) {
        File backupFile = new File(plugin.getDataFolder(), oldFile);
        File brokenFile = new File(plugin.getDataFolder(), newFile);
        try {
            FileInputStream inputStream = new FileInputStream(backupFile);
            FileOutputStream outputStream = new FileOutputStream(brokenFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


