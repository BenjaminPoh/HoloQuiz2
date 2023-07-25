package benloti.holoquiz.games;

import benloti.holoquiz.database.UserPersonalisation;
import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.structs.Question;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private final JavaPlugin plugin;
    private final UserPersonalisation userPersonalisation;
    private final long interval;
    private String gameMode; //currently pointless because we only have 1 gameMode. will add more... 1 day.
    private boolean gameRunning;
    private RewardsHandler rewardsHandler;

    private Trivia trivia;
    private final List<Question> triviaQuestionBank;

    public GameManager(JavaPlugin plugin, ConfigFile configFile, UserPersonalisation userPersonalisation,
                       DependencyHandler dependencyHandler) {
        this.plugin = plugin;
        this.interval = configFile.getInterval();
        this.gameMode = configFile.getGameMode();
        this.triviaQuestionBank = getTriviaQuestions();
        this.userPersonalisation = userPersonalisation;
        this.rewardsHandler = new RewardsHandler(plugin, dependencyHandler.getCMIDep());
    }

    public void startGame() {
        if (gameRunning) {
            return;
        }
        this.trivia = new Trivia(triviaQuestionBank, plugin, userPersonalisation);
        this.gameRunning = true;
        trivia.runTaskTimer(plugin, 0, interval * 20);
    }

    public void stopGame() {
        if (!gameRunning) {
            return;
        }
        trivia.cancel();
        this.trivia = null;
        this.gameRunning = false;
    }

    public void nextQuestion() {
        stopGame();
        startGame();
    }

    public Question getCurrentQuestion() {
        return trivia.getQuestion();
    }

    public boolean getQuestionStatus() {
        return trivia.isQuestionAnswered();
    }

    public void setQuestionStatus(boolean status) {
        trivia.setQuestionAnswered(status);
    }

    public long getTimeQuestionSent() {
        return trivia.getTimeQuestionSent();
    }

    public boolean getGameStatus() {
        return this.gameRunning;
    }

    public long getInterval() {
        return this.interval;
    }

    public RewardsHandler getRewardsHandler() {
        return this.rewardsHandler;
    }

    private List<Question> getTriviaQuestions() {
        File questionYml = new File(plugin.getDataFolder(), "QuestionBank.yml");
        List<Question> questionBank = new ArrayList<>();

        if (!questionYml.exists()) {
            try {
                questionYml.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileConfiguration questionFileConfig = YamlConfiguration.loadConfiguration(questionYml);
        ConfigurationSection questionSection = questionFileConfig.getConfigurationSection("questions");

        for (String key : questionSection.getKeys(false)) {
            ConfigurationSection questionSection2 = questionSection.getConfigurationSection(key);
            String question = questionSection2.getString("question");
            List<String> answers = questionSection2.getStringList("answer");
            questionBank.add(new Question(question, answers));
        }

        return questionBank;
    }
}
