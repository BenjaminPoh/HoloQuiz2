package benloti.holoquiz.files;

import benloti.holoquiz.structs.ContestRewardTier;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExternalFiles {

    private static final String CONFIG_FILE_NAME = "config.yml";
    private static final String QUESTION_BANK_FILE_NAME = "QuestionBank.yml";
    private static final String REWARDS_FILE_NAME = "Rewards.yml";
    private static final String BACKUP_DIRECTORY_PATH = "backup/";
    private static final String ARCHIVE_DIRECTORY_PATH = "archive/";
    private static final String ARCHIVE_CONFIG_FILE_NAME = "config_%d.yml";
    private static final String ARCHIVE_QUESTION_BANK_FILE_NAME = "QuestionBank_%d.yml";
    private static final String ARCHIVE_REWARDS_FILE_NAME = "Rewards_%d.yml";

    private static final String[] CONTEST_CATEGORIES = {"DailyMost", "DailyFastest", "DailyBestAvg", "DailyBestX",
            "WeeklyMost", "WeeklyFastest", "WeeklyBestAvg", "WeeklyBestX",
            "MonthlyMost", "MonthlyFastest", "MonthlyBestAvg", "MonthlyBestX"};

    public static final String LOG_MESSAGE_NUMBER_OF_CONTEST_REWARDS = "[HoloQuiz] Contest Type %s loaded in %d rewards";
    public static final String LOG_MESSAGE_NUMBER_OF_TRIVIA_QUESTIONS = "[HoloQuiz] Trivia Category loaded %d Questions!";
    public static final String WARNING_MISSING_PLUGIN_FOLDER = "[HoloQuiz] Warning: Plugin Folder Missing! Loading from Resource...";
    public static final String WARNING_MISSING_BACKUP_FOLDER = "[HoloQuiz] Warning: Backup Folder Missing! Loading from Resource...";
    public static final String WARNING_MISSING_ARCHIVE_FOLDER = "[HoloQuiz] Warning: Archive Folder Missing! Making a new one...";
    public static final String WARNING_REWARDS_SECTION_NOT_FOUND = "[HoloQuiz] Warning: Rewards Section not found!";
    public static final String WARNING_INVALID_MATERIAL = "[HoloQuiz] Warning: Failed to load item with the name - %s";
    public static final String WARNING_INVALID_QUESTION = "[HoloQuiz] Error with loading question: %s";
    public static final String ERROR_MSG_BROKEN_FILE = "[HoloQuiz] ERROR: Your %s file is broken! Loading from backups...";
    public static final String ERROR_MSG_BROKEN_FILE_ON_RELOAD = "[HoloQuiz] ERROR: Your %s file is broken! Reload has been Terminated!";
    public static final String ERROR_MSG_BROKEN_BACKUP_FILE = "[HoloQuiz] ERROR: Your %s backup file is also broken! Loading from Resource...";

    private final JavaPlugin plugin;
    private ConfigFile configFile;
    private ArrayList<RewardTier> allNormalRewards;
    private ArrayList<RewardTier> secretRewards;
    private Map<String, ArrayList<ContestRewardTier>> contestRewards;
    private ArrayList<Question> allQuestions;

    public ExternalFiles(JavaPlugin plugin) {
        this.plugin = plugin;
        //If plugin's data folder exists, all the necessary files are fetched from the resource section.
        if (!plugin.getDataFolder().exists()) {
            Bukkit.getLogger().info(WARNING_MISSING_PLUGIN_FOLDER);
            plugin.getDataFolder().mkdirs();
            loadFromResource(CONFIG_FILE_NAME, CONFIG_FILE_NAME);
            loadFromResource(QUESTION_BANK_FILE_NAME, QUESTION_BANK_FILE_NAME);
            loadFromResource(REWARDS_FILE_NAME, REWARDS_FILE_NAME);
        }
        File backupDir = new File(plugin.getDataFolder(), BACKUP_DIRECTORY_PATH);
        if(!backupDir.exists()) {
            Bukkit.getLogger().info(WARNING_MISSING_BACKUP_FOLDER);
            backupDir.mkdirs();
            loadFromResource(CONFIG_FILE_NAME,BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME);
            loadFromResource(QUESTION_BANK_FILE_NAME,BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME);
            loadFromResource(REWARDS_FILE_NAME  ,BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
        }
        File archiveDir = new File(plugin.getDataFolder(), ARCHIVE_DIRECTORY_PATH);
        if(!archiveDir.exists()) {
            Bukkit.getLogger().info(WARNING_MISSING_ARCHIVE_FOLDER);
            backupDir.mkdirs();
        }

        //Tries to load all the external files. If successful, the backup is updated with the most recent version.
        //If unsuccessful, the backup file is used. Broken files are replaced, with a copy of it moved to the Archive.
        //If still unsuccessful, the resource file is used.
        try {
            this.configFile = new ConfigFile(plugin, CONFIG_FILE_NAME);
            updateFile(BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME, CONFIG_FILE_NAME);
        } catch (Exception e) {
            String logMessage = String.format(ERROR_MSG_BROKEN_FILE, CONFIG_FILE_NAME);
            Bukkit.getLogger().info(logMessage);
            Bukkit.getLogger().info(e.toString());
            storeToArchive(CONFIG_FILE_NAME, ARCHIVE_CONFIG_FILE_NAME);
            try {
                this.configFile = new ConfigFile(plugin, BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME);
                updateFile(CONFIG_FILE_NAME, BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME);
            } catch (Exception e2) {
                logMessage = String.format(ERROR_MSG_BROKEN_BACKUP_FILE, CONFIG_FILE_NAME);
                Bukkit.getLogger().info(logMessage);
                Bukkit.getLogger().info(e2.toString());
                loadFromResource(CONFIG_FILE_NAME,BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME);
                loadFromResource(CONFIG_FILE_NAME, CONFIG_FILE_NAME);
                this.configFile = new ConfigFile(plugin,CONFIG_FILE_NAME);
            }
        }

        this.allNormalRewards = new ArrayList<>();
        this.secretRewards = new ArrayList<>();
        this.contestRewards = new HashMap<>();

        try {
            File rewardsYml = new File(plugin.getDataFolder(), REWARDS_FILE_NAME);
            loadAllRewards(rewardsYml, this.allNormalRewards, this.secretRewards, this.contestRewards);
            updateFile(BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME, REWARDS_FILE_NAME);
        } catch (Exception e) {
            String logMessage = String.format(ERROR_MSG_BROKEN_FILE, REWARDS_FILE_NAME);
            Bukkit.getLogger().info(logMessage);
            Bukkit.getLogger().info(e.toString());
            storeToArchive(REWARDS_FILE_NAME, ARCHIVE_REWARDS_FILE_NAME);
            try {
                File rewardsYml = new File(plugin.getDataFolder(), BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
                loadAllRewards(rewardsYml, this.allNormalRewards, this.secretRewards,this.contestRewards);
                updateFile(REWARDS_FILE_NAME, BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
            } catch (Exception e2) {
                logMessage = String.format(ERROR_MSG_BROKEN_BACKUP_FILE, REWARDS_FILE_NAME);
                Bukkit.getLogger().info(logMessage);
                Bukkit.getLogger().info(e2.toString());
                loadFromResource(REWARDS_FILE_NAME,BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
                loadFromResource(REWARDS_FILE_NAME, REWARDS_FILE_NAME);
                File rewardsYml = new File(plugin.getDataFolder(), REWARDS_FILE_NAME);
                loadAllRewards(rewardsYml, this.allNormalRewards, this.secretRewards,this.contestRewards);
            }
        }

        try {
            File questionsYml = new File(plugin.getDataFolder(), QUESTION_BANK_FILE_NAME);
            this.allQuestions = loadQuestions(questionsYml);
            updateFile(BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME, QUESTION_BANK_FILE_NAME);
        } catch (Exception e) {
            String logMessage = String.format(ERROR_MSG_BROKEN_FILE, QUESTION_BANK_FILE_NAME);
            Bukkit.getLogger().info(logMessage);
            Bukkit.getLogger().info(e.toString());
            storeToArchive(QUESTION_BANK_FILE_NAME, ARCHIVE_QUESTION_BANK_FILE_NAME);
            try {
                File questionsYml = new File(plugin.getDataFolder(), BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME);
                this.allQuestions = loadQuestions(questionsYml);
                updateFile(QUESTION_BANK_FILE_NAME, BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME);
            } catch (Exception e2) {
                logMessage = String.format(ERROR_MSG_BROKEN_BACKUP_FILE, QUESTION_BANK_FILE_NAME);
                Bukkit.getLogger().info(logMessage);
                Bukkit.getLogger().info(e2.toString());
                loadFromResource(QUESTION_BANK_FILE_NAME,BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME);
                loadFromResource(QUESTION_BANK_FILE_NAME, QUESTION_BANK_FILE_NAME);
                File questionsYml = new File(plugin.getDataFolder(), QUESTION_BANK_FILE_NAME);
                this.allQuestions = loadQuestions(questionsYml);
            }
        }
    }

    public boolean reloadAll() {
        if (!plugin.getDataFolder().exists()) {
            Bukkit.getLogger().info("[HoloQuiz] Plugin Folder Missing!");
            return false;
        }
        File backupDir = new File(plugin.getDataFolder(), BACKUP_DIRECTORY_PATH);
        if(!backupDir.exists()) {
            Bukkit.getLogger().info("[HoloQuiz] Backup Folder Missing!");
            return false;
        }

        ConfigFile newConfigFile;
        ArrayList<RewardTier> newAllNormalRewards = new ArrayList<>();
        ArrayList<RewardTier> newSecretRewards = new ArrayList<>();
        Map<String, ArrayList<ContestRewardTier>> newContestRewards = new HashMap<>();
        ArrayList<Question> newQuestions;

        try {
            newConfigFile = new ConfigFile(plugin, CONFIG_FILE_NAME);
        } catch (Exception e) {
            String logMessage = String.format(ERROR_MSG_BROKEN_FILE_ON_RELOAD, CONFIG_FILE_NAME);
            Bukkit.getLogger().info(logMessage);
            return false;
        }
        try {
            File rewardsYml = new File(plugin.getDataFolder(), REWARDS_FILE_NAME);
            loadAllRewards(rewardsYml, newAllNormalRewards, newSecretRewards, newContestRewards);
        } catch (Exception e) {
            String logMessage = String.format(ERROR_MSG_BROKEN_FILE_ON_RELOAD, REWARDS_FILE_NAME);
            Bukkit.getLogger().info(logMessage);
            return false;
        }
        try {
            File questionsYml = new File(plugin.getDataFolder(), QUESTION_BANK_FILE_NAME);
            newQuestions = loadQuestions(questionsYml);
        } catch (Exception e) {
            String logMessage = String.format(ERROR_MSG_BROKEN_FILE_ON_RELOAD, QUESTION_BANK_FILE_NAME);
            Bukkit.getLogger().info(logMessage);
            return false;
        }

        updateFile(BACKUP_DIRECTORY_PATH + CONFIG_FILE_NAME, CONFIG_FILE_NAME);
        updateFile(BACKUP_DIRECTORY_PATH + REWARDS_FILE_NAME, REWARDS_FILE_NAME);
        updateFile(BACKUP_DIRECTORY_PATH + QUESTION_BANK_FILE_NAME, QUESTION_BANK_FILE_NAME);

        this.allQuestions = newQuestions;
        this.allNormalRewards = newAllNormalRewards;
        this.secretRewards = newSecretRewards;
        this.contestRewards = newContestRewards;
        this.configFile = newConfigFile;

        return true;
    }

    /**
     * Used to load all 3 categories of rewards
     * @param rewardsYml the Rewards.yml File
     */
    private void loadAllRewards(File rewardsYml, ArrayList<RewardTier> allNormalRewards, ArrayList<RewardTier> secretRewards,
                                Map<String, ArrayList<ContestRewardTier>> contestRewards) {
        FileConfiguration rewardsFile = YamlConfiguration.loadConfiguration(rewardsYml);

        ConfigurationSection normalRewardsSection = rewardsFile.getConfigurationSection("Rewards");
        int normalRewardsLoaded = loadRewardsTier(normalRewardsSection, allNormalRewards);
        if (normalRewardsLoaded == 0) {
            Bukkit.getLogger().info(WARNING_REWARDS_SECTION_NOT_FOUND);
        }
        ConfigurationSection secretRewardsSection = rewardsFile.getConfigurationSection("SecretRewards");
        loadRewardsTier(secretRewardsSection, secretRewards);

        ConfigurationSection contestRewardsSection = rewardsFile.getConfigurationSection("ContestRewards");
        loadContestRewards(contestRewardsSection, contestRewards);
    }

    /**
     * Used to update the ArrayList of rewards used.
     * Invalid materials are replaced with a carrot. I don't know why a carrot.
     *
     * @param rewardsSection The section that has all the Trivia rewards.
     * @param rewardsList The ArrayList to be filled.
     */
    private int loadRewardsTier(ConfigurationSection rewardsSection, ArrayList<RewardTier> rewardsList) {
        int categoriesLoaded = 0;
        if(rewardsSection == null) {
            return categoriesLoaded;
        }

        for (String key : rewardsSection.getKeys(false)) {
            categoriesLoaded += 1;
            ConfigurationSection rewardTierSection = rewardsSection.getConfigurationSection(key);
            double maxTime = rewardTierSection.getDouble("MaxAnswerTime", 0);
            int maxTimeInMilliseconds = (int) maxTime * 1000;
            double moneyReward = rewardTierSection.getDouble("Money", 0);
            List<String> commandsExecuted = rewardTierSection.getStringList("Commands");
            ConfigurationSection rewardTierItemSection = rewardTierSection.getConfigurationSection("Items");
            ArrayList<ItemStack> itemReward = new ArrayList<>();
            loadItemReward(rewardTierItemSection, itemReward);
            rewardsList.add(new RewardTier(maxTimeInMilliseconds, moneyReward, commandsExecuted, itemReward, new ArrayList<>()));
        }

        return categoriesLoaded;
    }

    private void loadItemReward(ConfigurationSection rewardTierItemSection, ArrayList<ItemStack> itemReward) {
        if(rewardTierItemSection == null) {
            return;
        }
        for (String key : rewardTierItemSection.getKeys(false)) {
            ConfigurationSection rewardTierItem = rewardTierItemSection.getConfigurationSection(key);
            String itemType = rewardTierItem.getString("Material", "");
            Material itemMaterial = Material.matchMaterial(itemType);
            if (itemMaterial == null) {
                itemMaterial = Material.CARROT;
                Bukkit.getLogger().info(String.format(WARNING_INVALID_MATERIAL,itemType));
            }
            int itemQty = rewardTierItem.getInt("Qty");
            List<String> itemLore = rewardTierItem.getStringList("Lore");
            ItemStack itemStack = new ItemStack(itemMaterial, itemQty);

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setLore(itemLore);
            itemStack.setItemMeta(itemMeta);
            itemReward.add(itemStack);
        }
    }

    /**
     * Used to load the contest rewards.
     * @param rewardsSection The section that has all the Contest rewards.
     */
    private void loadContestRewards(ConfigurationSection rewardsSection,
                                    Map<String, ArrayList<ContestRewardTier>> contestRewards) {
        if(rewardsSection == null) {
            return;
        }

        for(String category: CONTEST_CATEGORIES) {
            ConfigurationSection section = rewardsSection.getConfigurationSection(category);

            ArrayList<ContestRewardTier> rewardsList = loadContestRewardsTier(section);
            contestRewards.put(category, rewardsList);
            String logMessage = String.format(LOG_MESSAGE_NUMBER_OF_CONTEST_REWARDS, category, rewardsList.size());
            Bukkit.getLogger().info(logMessage);
        }
    }

    private ArrayList<ContestRewardTier> loadContestRewardsTier(ConfigurationSection section) {
        ArrayList<ContestRewardTier> rewardsList = new ArrayList<>();
        if(section == null) {
            return rewardsList;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection rewardTierSection = section.getConfigurationSection(key);
            int reps = rewardTierSection.getInt("Reps", 0);
            double moneyReward = rewardTierSection.getDouble("Money", 0);
            String message = rewardTierSection.getString("Message", "");
            List<String> commandsExecuted = rewardTierSection.getStringList("Commands");
            ConfigurationSection rewardTierItemSection = rewardTierSection.getConfigurationSection("Items");
            ArrayList<ItemStack> itemReward = new ArrayList<>();
            loadItemReward(rewardTierItemSection, itemReward);
            ContestRewardTier rewardTier = new ContestRewardTier(moneyReward, commandsExecuted, itemReward, message);
            for(int i = 0; i < reps; i++) {
                rewardsList.add(rewardTier);
            }
        }
        return rewardsList;
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
            String questionColourCode = configSection.getString("QuestionColour", "");
            String messageColourCode = configSection.getString("MessageColour", "");
            String categoryLabel = configSection.getString("CategoryLabel", "");
            String categoryPrefix = categoryLabel + questionColourCode;
            ConfigurationSection questionListSection = configSection.getConfigurationSection("QuestionList");
            questionListLoader(questionList, questionListSection, categoryPrefix, messageColourCode);
        }
        String logMessage = String.format(LOG_MESSAGE_NUMBER_OF_TRIVIA_QUESTIONS, questionList.size());
        Bukkit.getLogger().info(logMessage);
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
            if(question == null || answers.isEmpty()) {
                Bukkit.getLogger().info(String.format(WARNING_INVALID_QUESTION, question));
                continue;
            }
            question = prefix + question;
            String message = questionConfig.getString("Message", "");
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

    public ArrayList<RewardTier> getAllNormalRewards() {
        return this.allNormalRewards;
    }

    public ArrayList<RewardTier> getAllSecretRewards() {
        return this.secretRewards;
    }

    public ArrayList<ContestRewardTier> getContestRewardByCategory(String category, boolean isEnabled) {
        if(!isEnabled) {
          return new ArrayList<>();
        }
        return contestRewards.get(category);
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
            Bukkit.getLogger().info(e.toString());
        }
    }


    /**
     * Used to update Files
     *
     * @param oldFileName The file to be replaced
     * @param newFileName The file that is used to replace the other file
     */
    private void updateFile (String oldFileName, String newFileName) {
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
            Bukkit.getLogger().info(e.toString());
        }
    }

    private void storeToArchive (String brokenFileName, String archiveFileNameFormat) {
        String archiveFileName = String.format(archiveFileNameFormat, System.currentTimeMillis());
        updateFile(archiveFileName, brokenFileName);
    }
}


