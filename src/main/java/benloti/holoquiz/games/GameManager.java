package benloti.holoquiz.games;

import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.files.ExternalFiles;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.structs.Question;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Random;

public class GameManager {
    private final JavaPlugin plugin;
    private final UserInterface userInterface;
    private final long interval;
    private boolean gameRunning;
    private final RewardsHandler rewardsHandler;

    //private String gameMode; //currently pointless because we only have 1 gameMode. will add more... 1 day.

    private Trivia trivia;
    private ArrayList<Question> triviaQuestionList;

    public GameManager(JavaPlugin plugin, UserInterface userInterface,
                       DependencyHandler dependencyHandler, ExternalFiles externalFiles) {
        this.plugin = plugin;
        this.interval = externalFiles.getConfigFile().getInterval();
        //this.gameMode = configFile.getGameMode();
        this.triviaQuestionList = externalFiles.getAllQuestions();
        this.userInterface = userInterface;
        this.rewardsHandler = new RewardsHandler(plugin, userInterface, dependencyHandler.getVaultDep(),
                externalFiles.getAllRewards());
    }

    public void startGame() {
        if (gameRunning) {
            return;
        }
        this.trivia = new Trivia(getRandomQuestion(), plugin, userInterface);
        this.gameRunning = true;
        trivia.runTaskTimer(plugin, 0, interval * 20);
    }

    private Question getRandomQuestion() {
        int size = triviaQuestionList.size();
        Random rand = new Random();
        int randomIndex = rand.nextInt(size);
        return triviaQuestionList.get(randomIndex);
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

    public void updateQuestionList(ArrayList<Question> questionList) {
        this.triviaQuestionList = questionList;
    }

    public RewardsHandler getRewardsHandler() {
        return this.rewardsHandler;
    }
}
