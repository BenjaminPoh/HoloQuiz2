package benloti.holoquiz.games;

import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.files.ExternalFiles;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.structs.Question;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class GameManager {
    private final JavaPlugin plugin;
    private final UserInterface userInterface;
    private final long interval;
    private final long intervalCheck;
    private final int questionCooldown;
    private final LinkedList<Integer> questionCooldownList;
    private final HashSet<Integer> questionCooldownMap;
    private final RewardsHandler rewardsHandler;
    private final MathQuestionGenerator mathQuestionGenerator;
    
    private boolean gameRunning;
    private String gameMode;
    private QuestionHandler questionHandler;
    private PeriodicChecker periodicChecker;

    private ArrayList<Question> triviaQuestionList;

    public GameManager(JavaPlugin plugin, ConfigFile configFile, UserInterface userInterface,
                       DependencyHandler dependencyHandler, ExternalFiles externalFiles, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.interval = externalFiles.getConfigFile().getInterval();
        this.intervalCheck = externalFiles.getConfigFile().getIntervalCheck();
        this.gameMode = configFile.getGameMode();
        this.triviaQuestionList = externalFiles.getAllQuestions();
        this.mathQuestionGenerator = new MathQuestionGenerator(configFile);
        this.userInterface = userInterface;
        this.rewardsHandler = new RewardsHandler(plugin, userInterface, dependencyHandler.getVaultDep(),databaseManager,
                externalFiles.getAllNormalRewards(), externalFiles.getAllSecretRewards());

        if(configFile.getQuestionCooldownLength() >= triviaQuestionList.size()) {
            this.questionCooldown = 0;
        } else {
            this.questionCooldown = configFile.getQuestionCooldownLength();
        }
        this.questionCooldownList = new LinkedList<>();
        this.questionCooldownMap = new HashSet<>();
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
        if(intervalCheck > 0) {
            this.periodicChecker = new PeriodicChecker(this);
            periodicChecker.runTaskTimer(plugin, 0, intervalCheck * 20);
        }
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
        if(intervalCheck > 0) {
            periodicChecker.cancel();
        }
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
        return gameMode.substring(0, 1);
    }

    private Question getRandomTriviaQuestion() {
        int size = triviaQuestionList.size();
        Random rand = new Random();
        int randomIndex = rand.nextInt(size);
        Bukkit.getLogger().info("Index rolled: " + randomIndex + " | Queue: " + questionCooldownList.toString());
        randomIndex = obtainQuestionNotOnCooldown(randomIndex, size);
        return triviaQuestionList.get(randomIndex);
    }

    private int obtainQuestionNotOnCooldown(int current, int max) {
        if(questionCooldown <= 0) {
            return current;
        }
        while(questionCooldownMap.contains(current)) {
            current += 1;
            if(current == max) {
                current = 0;
            }
        }
        questionCooldownMap.add(current);
        questionCooldownList.add(current);
        if(questionCooldownMap.size() > questionCooldown) {
            int oldQuestionIndex = questionCooldownList.removeFirst();
            questionCooldownMap.remove(oldQuestionIndex);
        }
        return current;
    }

    private Question getRandomMathQuestion() {
        String question = mathQuestionGenerator.getMathQuestion();
        double answer = mathQuestionGenerator.solver(question);
        return mathQuestionGenerator.parser(mathQuestionGenerator.getMathQuestionColour(), question, answer);
    }
}
