package benloti.holoquiz.games;

import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.files.ExternalFiles;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.structs.Question;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Random;

public class GameManager {
    private final JavaPlugin plugin;
    private final UserInterface userInterface;
    private final long interval;
    private final RewardsHandler rewardsHandler;
    private final MathQuestionGenerator mathQuestionGenerator;

    private boolean gameRunning;
    private String gameMode;
    private QuestionHandler questionHandler;

    private ArrayList<Question> triviaQuestionList;

    public GameManager(JavaPlugin plugin, ConfigFile configFile, UserInterface userInterface,
                       DependencyHandler dependencyHandler, ExternalFiles externalFiles) {
        this.plugin = plugin;
        this.interval = externalFiles.getConfigFile().getInterval();
        this.gameMode = configFile.getGameMode();
        this.triviaQuestionList = externalFiles.getAllQuestions();
        this.mathQuestionGenerator = new MathQuestionGenerator(configFile);
        this.userInterface = userInterface;
        this.rewardsHandler = new RewardsHandler(plugin, userInterface, dependencyHandler.getVaultDep(),
                externalFiles.getAllRewards());
    }

    public void startGame() {
        if (gameRunning) {
            return;
        }
        this.questionHandler = new QuestionHandler(plugin, userInterface, this);
        this.gameRunning = true;
        //Note to future self: runTaskTimer is the one that loops the task on interval.
        //If you are thinking to move the randomisation function here, it won't work.
        questionHandler.runTaskTimer(plugin, 0, interval * 20);
    }

    public Question getRandomQuestion() {
        if(gameMode.equals("Math")) {
            return getRandomMathQuestion();
        }
        if(gameMode.equals("Trivia")) {
            return getRandomTriviaQuestion();
        }
        Bukkit.getLogger().info("[HoloQuiz] Error: There is no way you ever see this message.");
        return null;
    }

    public void stopGame() {
        if (!gameRunning) {
            return;
        }
        questionHandler.cancel();
        this.questionHandler = null;
        this.gameRunning = false;
    }

    public void nextQuestion() {
        stopGame();
        startGame();
    }

    public Question getCurrentQuestion() {
        return questionHandler.getQuestion();
    }

    public boolean getQuestionStatus() {
        return questionHandler.isQuestionAnswered();
    }

    public void setQuestionStatus(boolean status) {
        questionHandler.setQuestionAnswered(status);
    }

    public long getTimeQuestionSent() {
        return questionHandler.getTimeQuestionSent();
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

    public String getGameModeIdentifier() {
        if(gameMode.equals("Math")) {
            return "M";
        }
        if(gameMode.equals("Trivia")) {
            return "T";
        }
        Bukkit.getLogger().info("[HoloQuiz] Error: There is no way you ever see this message.");
        return null;
    }

    private Question getRandomTriviaQuestion() {
        int size = triviaQuestionList.size();
        Random rand = new Random();
        int randomIndex = rand.nextInt(size);
        return triviaQuestionList.get(randomIndex);
    }

    private Question getRandomMathQuestion() {
        String question = mathQuestionGenerator.getMathQuestion();
        double answer = mathQuestionGenerator.solver(question);
        return mathQuestionGenerator.parser(question, answer);
    }

}
