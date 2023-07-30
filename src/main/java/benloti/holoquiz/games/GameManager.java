package benloti.holoquiz.games;

import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.structs.Question;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class GameManager {
    private final JavaPlugin plugin;
    private final UserInterface userInterface;
    private final long interval;
    private boolean gameRunning;
    private final RewardsHandler rewardsHandler;
    private final QuestionHandler questionHandler;

    private Trivia trivia;
    private String gameMode; //currently pointless because we only have 1 gameMode. will add more... 1 day.

    public GameManager(JavaPlugin plugin, ConfigFile configFile, UserInterface userInterface,
                       DependencyHandler dependencyHandler) {
        this.plugin = plugin;
        this.interval = configFile.getInterval();
        this.gameMode = configFile.getGameMode();
        this.questionHandler = getTriviaQuestions();
        this.userInterface = userInterface;
        this.rewardsHandler = new RewardsHandler(plugin, userInterface, dependencyHandler.getVaultDep());
    }

    public void startGame() {
        if (gameRunning) {
            return;
        }
        this.trivia = new Trivia(questionHandler, plugin, userInterface);
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

    private QuestionHandler getTriviaQuestions() {
        File questionYml = new File(plugin.getDataFolder(), "QuestionBank.yml");

        if (!questionYml.exists()) {
            try {
                questionYml.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileConfiguration questionFileConfig = YamlConfiguration.loadConfiguration(questionYml);
        return new QuestionHandler(questionFileConfig);
    }
}
