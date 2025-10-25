package benloti.holoquiz.games;

import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.files.ExternalFiles;
import benloti.holoquiz.files.Logger;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.structs.Question;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class GameManager {
    private static final String MESSAGE_REVEAL_ANSWER = "&bNo one got the answer! The answer was &a%s&b.";
    private static final String LOG_MESSAGE_QUESTION_SENT = "Question Sent: %s";
    private static final String DEV_ERROR_INVALID_MODE = "Invalid mode %s. There is no way you ever see this.";

    private final JavaPlugin plugin;
    private final UserInterface userInterface;
    private final long interval;
    private final long intervalCheck;
    private final int questionCooldown;
    private final long revealAnswerDelay;
    private final boolean revealAnswerFlag;
    private final boolean inaGoesWAH;
    private final int mathWeightageForMixed;
    private final int triviaWeightageForMixed;

    private final LinkedList<Integer> questionCooldownList;
    private final HashSet<Integer> questionCooldownMap;
    private final RewardsHandler rewardsHandler;
    private final MathQuestionGenerator mathQuestionGenerator;

    private final String gameMode;
    private final Random rngesus;
    private NextTaskScheduler nextTaskScheduler;
    private PeriodicChecker periodicChecker;
    private boolean gameRunning;
    private Question currentQuestion;
    private String currentQuestionType;

    private long timeQuestionSent;
    private boolean questionAnswered;
    private boolean timedOut;
    private long nextTaskTime;

    private ArrayList<Question> triviaQuestionList;

    public GameManager(JavaPlugin plugin, ConfigFile configFile, UserInterface userInterface,
                       DependencyHandler dependencyHandler, ExternalFiles externalFiles, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.inaGoesWAH = configFile.isInaWahEnabled();
        this.interval = configFile.getInterval();
        this.intervalCheck = configFile.getIntervalCheck();
        this.revealAnswerDelay = configFile.getRevealAnswerDelay();
        this.gameMode = configFile.getGameMode();
        this.mathWeightageForMixed = configFile.getMathWeightage();
        this.triviaWeightageForMixed = configFile.getTriviaWeightage();
        this.triviaQuestionList = externalFiles.getAllQuestions();
        this.mathQuestionGenerator = new MathQuestionGenerator(configFile);
        this.userInterface = userInterface;
        this.rewardsHandler = new RewardsHandler(plugin, userInterface, dependencyHandler.getVaultDep(),databaseManager,
                externalFiles, configFile);

        this.revealAnswerFlag = (this.revealAnswerDelay == -1);
        this.rngesus = new Random();

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
        this.nextTaskScheduler = new NextTaskScheduler(this);
        this.gameRunning = true;
        sendQuestion();
        this.nextTaskScheduler.runTaskLater(plugin,  getInterval() * 20);
        this.nextTaskTime = this.timeQuestionSent + getInterval()*1000;

        if(intervalCheck > 0) {
            this.periodicChecker = new PeriodicChecker(this);
            periodicChecker.runTaskTimer(plugin, 0, intervalCheck * 20);
        }
    }

    public void triggerNextTask(boolean cancelScheduledTask) {
        if(cancelScheduledTask) {
            nextTaskScheduler.cancel();
        }
        this.nextTaskScheduler = new NextTaskScheduler(this);

        //If chosen to not reveal answer, send next question immediately.
        if(revealAnswerFlag) {
            sendQuestion();
            this.nextTaskScheduler.runTaskLater(plugin,  getInterval() * 20);
            this.nextTaskTime = this.timeQuestionSent + getInterval()*1000;
            return;
        }

        //If question is not answered, send the answer
        if(!isQuestionAnswered() && !isQuestionTimedOut()) {
            revealAnswer();
            setQuestionTimedOut(true);
            this.nextTaskScheduler.runTaskLater(plugin,  getRevealAnswerDelay() * 20);
            this.nextTaskTime = System.currentTimeMillis() + getRevealAnswerDelay()*1000;
            return;
        }

        //Schedule the time taken to send next question.
        sendQuestion();
        this.nextTaskScheduler.runTaskLater(plugin,  getInterval() * 20);
        this.nextTaskTime = this.timeQuestionSent + getInterval()*1000;
    }

    private void sendQuestion() {
        Question question = getRandomQuestion();
        boolean playInaWAH = false;
        Logger.getLogger().info_high(String.format(LOG_MESSAGE_QUESTION_SENT, question.getQuestion()));
        String formattedQuestion = userInterface.attachLabel(question.getQuestion());
        formattedQuestion = userInterface.formatColours(formattedQuestion);
        if(this.inaGoesWAH) {
            playInaWAH = formattedQuestion.contains("Wha") || formattedQuestion.contains("wha");
            formattedQuestion = formattedQuestion.replace("Wha", "WAH");
            formattedQuestion = formattedQuestion.replace("wha", "WAH");
        }
        setQuestionAnswered(false);
        setQuestionTimedOut(false);
        long currentTime = System.currentTimeMillis();
        setTimeQuestionSent(currentTime);
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            userInterface.attachSuffixAndSend(player, formattedQuestion);
            if(playInaWAH) {
                player.playSound(player.getLocation(), "minecraft:custom.ina.wah", 1.0f, 1.0f);
            }
        }
    }

    private void revealAnswer() {
        String answer = getCurrentQuestion().getAnswers().get(0);
        String announcement = String.format(MESSAGE_REVEAL_ANSWER, answer);
        String formattedAnnouncement = userInterface.formatColours(userInterface.attachLabel(announcement));
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            userInterface.attachSuffixAndSend(player, formattedAnnouncement);
        }
    }

    public Question getRandomQuestion() {
        switch (gameMode) {
            case "Math":
                this.currentQuestion = getRandomMathQuestion();
                return this.currentQuestion;
            case "Trivia":
                this.currentQuestion = getRandomTriviaQuestion();
                return this.currentQuestion;
            case "Mixed":
                this.currentQuestion = getRandomMixedQuestion();
                return this.currentQuestion;
        }
        Logger.getLogger().devError(String.format(DEV_ERROR_INVALID_MODE, gameMode));
        return null;
    }

    public void stopGame() {
        if (!gameRunning) {
            return;
        }
        nextTaskScheduler.cancel();
        this.nextTaskScheduler = null;
        if(intervalCheck > 0) {
            periodicChecker.cancel();
            this.periodicChecker = null;
        }
        this.gameRunning = false;
    }

    public void nextQuestion() {
        stopGame();
        startGame();
    }

    //Actual Helper Functions
    public Question getRandomMixedQuestion() {
        int totalWeight = this.mathWeightageForMixed + this.triviaWeightageForMixed;
        int randomValue = this.rngesus.nextInt(totalWeight) + 1;

        if (randomValue <= mathWeightageForMixed) {
            return getRandomMathQuestion();
        }
        return getRandomTriviaQuestion();

    }

    private Question getRandomTriviaQuestion() {
        int size = triviaQuestionList.size();
        int randomIndex = this.rngesus.nextInt(size);
        //Logger.getLogger().debug("Qn Rolled: " + randomIndex + " | Queue: " + questionCooldownList.toString());
        randomIndex = obtainQuestionNotOnCooldown(randomIndex, size);
        this.currentQuestionType = "T";
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
        this.currentQuestionType = "M";
        return mathQuestionGenerator.parser(mathQuestionGenerator.getMathQuestionColour(), question, answer);
    }

    //Getters and Setters
    public String getCurrentQuestionType() {
        return this.currentQuestionType;
    }

    public Question getCurrentQuestion() {
        return this.currentQuestion;
    }

    public boolean isQuestionAnswered() {
        return this.questionAnswered;
    }

    public void setQuestionAnswered(boolean status) {
        this.questionAnswered = status;
    }

    public long getTimeQuestionSent() {
        return this.timeQuestionSent;
    }

    public void setTimeQuestionSent(long time) {
        this.timeQuestionSent = time;
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

    public long getRevealAnswerDelay() {
        return this.revealAnswerDelay;
    }

    public long getNextTaskTime() {
        return this.nextTaskTime;
    }

    public boolean isQuestionTimedOut() {
        return timedOut;
    }

    public void setQuestionTimedOut(boolean status) {
        this.timedOut = status;
    }
}
