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

    private final JavaPlugin plugin;
    private ConfigFile configFile;
    private final ArrayList<RewardTier> allRewards;
    private ArrayList<Question> allQuestions;

    public ExternalFiles(JavaPlugin plugin) {
        this.plugin = plugin;
        //If plugin's data folder exists, all the necessary files are fetched from the resource section.
        if (!plugin.getDataFolder().exists()) {
            Bukkit.getLogger().info("[HoloQuiz] Plugin Folder Missing! Loading from Resource...");
            plugin.getDataFolder().mkdirs();
            loadFromResource(CONFIG_FILE_NAME, CONFIG_FILE_NAME);
            loadFromResource(QUESTION_BANK_FILE_NAME, QUESTION_BANK_FILE_NAME);
            loadFromResource(REWARDS_FILE_NAME, REWARDS_FILE_NAME);
        }
        File backupDir = new File(plugin.getDataFolder(), BACKUP_DIRECTORY_PATH);
        if(!backupDir.exists()) {
            Bukkit.getLogger().info("[HoloQuiz] Backup Folder Missing! Loading from Resource...");
            backupDir.mkdirs();
            loadFromResource(CONFIG_FILE_NAME,BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME);
            loadFromResource(QUESTION_BANK_FILE_NAME,BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME);
            loadFromResource(REWARDS_FILE_NAME  ,BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
        }

        //Tries to load all the external files. If successful, the backup is updated with the most recent version.
        //If unsuccessful, the backup file is used. Broken files are replaced.
        //If still unsuccessful, the resource file is used.
        try {
            Bukkit.getLogger().info("[HoloQuiz] Loading config.yml ...");
            this.configFile = new ConfigFile(plugin, CONFIG_FILE_NAME);
            updateFile(BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME, CONFIG_FILE_NAME);
        } catch (Exception e) {
            Bukkit.getLogger().info("[HoloQuiz] Your config.yml file is broken! Loading from backups...");
            try {
                this.configFile = new ConfigFile(plugin, BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME);
                updateFile(CONFIG_FILE_NAME, BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME);
            } catch (Exception e2) {
                Bukkit.getLogger().info("[HoloQuiz] Your config.yml backup file is also broken! Loading from Resource...");
                loadFromResource(CONFIG_FILE_NAME,BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME);
                loadFromResource(CONFIG_FILE_NAME, CONFIG_FILE_NAME);
                this.configFile = new ConfigFile(plugin,CONFIG_FILE_NAME);
            }
        }

        this.allRewards = new ArrayList<>();
        try {
            Bukkit.getLogger().info("[HoloQuiz] Loading Rewards.yml ...");
            File rewardsYml = new File(plugin.getDataFolder(), REWARDS_FILE_NAME);
            loadRewards (rewardsYml);
            updateFile(BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME, REWARDS_FILE_NAME);
        } catch (Exception e) {
            Bukkit.getLogger().info("[HoloQuiz] Your Rewards.yml file is broken! Loading from backups...");
            try {
                File rewardsYml = new File(plugin.getDataFolder(), BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
                loadRewards(rewardsYml);
                updateFile(REWARDS_FILE_NAME, BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
            } catch (Exception e2) {
                Bukkit.getLogger().info("[HoloQuiz] Your Rewards.yml backup file is also broken! Loading from Resource...");
                loadFromResource(REWARDS_FILE_NAME,BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
                loadFromResource(REWARDS_FILE_NAME, REWARDS_FILE_NAME);
                File rewardsYml = new File(plugin.getDataFolder(), REWARDS_FILE_NAME);
                loadRewards (rewardsYml);
            }
        }

        try {
            Bukkit.getLogger().info("[HoloQuiz] Loading QuestionBank.yml ...");
            File questionsYml = new File(plugin.getDataFolder(), QUESTION_BANK_FILE_NAME);
            this.allQuestions = loadQuestions(questionsYml);
            updateFile(BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME, QUESTION_BANK_FILE_NAME);
        } catch (Exception e) {
            Bukkit.getLogger().info("[HoloQuiz] Your QuestionBank.yml file is broken! Loading from backups...");
            try {
                File questionsYml = new File(plugin.getDataFolder(), BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
                this.allQuestions = loadQuestions(questionsYml);
                updateFile(QUESTION_BANK_FILE_NAME, BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME);
            } catch (Exception e2) {
                Bukkit.getLogger().info("[HoloQuiz] Your QuestionBank.yml backup file is also broken! Loading from Resource...");
                loadFromResource(QUESTION_BANK_FILE_NAME,BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME);
                loadFromResource(QUESTION_BANK_FILE_NAME, QUESTION_BANK_FILE_NAME);
                File questionsYml = new File(plugin.getDataFolder(), QUESTION_BANK_FILE_NAME);
                this.allQuestions = loadQuestions(questionsYml);
            }
        }
    }
    /**
     * Used to create the ArrayList of Questions used for the Trivia Mode
     * Invalid materials are replaced with a carrot. I don't know why a carrot.
     *
     * @param rewardsYml The file that has all the rewards.
     */

    private void loadRewards(File rewardsYml) {
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

    /**
     * Used to create the ArrayList of Questions used for the Trivia Mode
     * Missing fields are replaced with an empty string
     *
     * @param questionYml The file that has all the questions.
     */
    private ArrayList<Question> loadQuestions(File questionYml) {
        ArrayList<Question> questionList = new ArrayList<>();
        FileConfiguration config = YamlConfiguration.loadConfiguration(questionYml);
        for (String key : config.getKeys(false)) {
            ConfigurationSection configSection = config.getConfigurationSection(key);
            String questionColourCode = nullReplacer(configSection.getString("QuestionColour"));
            String messageColourCode = nullReplacer(configSection.getString("MessageColour"));
            String categoryLabel = nullReplacer(configSection.getString("CategoryLabel"));
            String categoryPrefix = categoryLabel + questionColourCode;
            ConfigurationSection questionListSection = configSection.getConfigurationSection("QuestionList");
            questionListLoader(questionList, questionListSection, categoryPrefix, messageColourCode);
        }
        return questionList;
    }

    /**
     * Helper function used to load the ArrayList of Questions in a specific category.
     * Question and Answer cannot be empty, and hence will be skipped and logged if so.
     */
    private void questionListLoader(ArrayList<Question> questionList, ConfigurationSection config,
                                    String prefix, String msgColorCode) {
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

            questionList.add(newQuestion);
        }
    }

    private String nullReplacer(String x) {
        if(x == null) {
            return "";
        }
        return x;
    }

    public boolean reloadQuestions() {
        File newQuestionFile = new File(plugin.getDataFolder(), QUESTION_BANK_FILE_NAME);
        try {
            this.allQuestions = loadQuestions(newQuestionFile);
            updateFile(BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME, QUESTION_BANK_FILE_NAME);
        } catch (Exception e) {
            return false;
        }
        return true;
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

    private void loadFromResource(String fileName, String dest) {
        InputStream inputStream = plugin.getResource(fileName);
        assert inputStream != null;
        File createdFile = new File(plugin.getDataFolder(), dest);
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

    private void updateFile(String oldFileName, String newFileName) {
        Bukkit.getLogger().info("[HoloQuiz] Replacing " + oldFileName + " with " + newFileName);
        File oldFile = new File(plugin.getDataFolder(), oldFileName);
        File newFile = new File(plugin.getDataFolder(), newFileName);
        try {
            FileInputStream inputStream = new FileInputStream(newFile);
            FileOutputStream outputStream = new FileOutputStream(oldFile);
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


