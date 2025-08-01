package benloti.holoquiz.games;

import benloti.holoquiz.HoloQuiz;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.files.ContestManager;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.structs.Question;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*; //Wtf blasphemy!
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class QuizAnswerHandler implements Listener {

    public static final String CORRECT_ANSWER_ANNOUNCEMENT = "&6%s&e wins after &6%s&e seconds! The answer was &6%s!";
    public static final String SECRET_ANSWER_ANNOUNCEMENT = "&6%s&e wins after &6%s&e seconds!";
    public static final String CORRECT_ANSWER_LOG = "[HoloQuiz] %s answered correctly in %s time.";

    private static final String DEBUG_LOG_RACE_CONDITION = "[HoloQuiz Debug Log] Race Condition occurred. Player %s answered in %s time, with %s processing time";

    private static final String INVENTORY_FULL_MESSAGE =
            "&bYour Inventory was full! Rewards has been sent to Storage. Do &a/holoquiz collect &bto get them!";
    private static final String SRTS_TRIGGERED_MESSAGE =
            "&bYour rewards has been sent to Storage! Do &a/holoquiz collect &bto get them!";

    private final HoloQuiz plugin;
    private final DatabaseManager database;
    private GameManager gameManager;
    private RewardsHandler rewardsHandler;
    private UserInterface userInterface;
    private ContestManager contestManager;
    private MinSDCheatDetector sdChecker;
    private MinTimeCheatDetector timeChecker;

    private int correctAnswerMsgLoc;

    public QuizAnswerHandler(HoloQuiz plugin, GameManager gameManager, DatabaseManager database,
                             UserInterface userInterface, ConfigFile configFile, ContestManager contestManager) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.gameManager = gameManager;
        this.plugin = plugin;
        this.database = database;
        this.rewardsHandler = gameManager.getRewardsHandler();
        this.userInterface = userInterface;
        this.contestManager = contestManager;
        this.sdChecker = configFile.getMinSDCheatDetector();
        this.timeChecker = configFile.getMinTimeCheatDetector();
        this.correctAnswerMsgLoc = configFile.getCorrectAnswerMessageLoc();
    }

    public void reload(GameManager gameManager, UserInterface userInterface, ConfigFile configFile, ContestManager contestManager) {
        this.gameManager = gameManager;
        this.userInterface = userInterface;
        this.contestManager = contestManager;
        this.rewardsHandler = gameManager.getRewardsHandler();
        this.sdChecker = configFile.getMinSDCheatDetector();
        this.timeChecker = configFile.getMinTimeCheatDetector();
        this.correctAnswerMsgLoc = configFile.getCorrectAnswerMessageLoc();
    }

    @EventHandler
    public void checkAnswerSent(AsyncPlayerChatEvent theEvent) {
        if(!gameManager.getGameStatus() || gameManager.isQuestionAnswered() || gameManager.isQuestionTimedOut()) {
            return;
        }
        String message = theEvent.getMessage();
        Player player = theEvent.getPlayer();
        List<String> answers = gameManager.getCurrentQuestion().getAnswers();
        List<String> secretAnswers = gameManager.getCurrentQuestion().getSecretAnswers();
        for(String possibleAnswer : answers) {
            if (message.equalsIgnoreCase(possibleAnswer)) {
                executeCorrectAnswerTasks(player, false, possibleAnswer);
                return;
            }
        }
        for(String possibleAnswer : secretAnswers) {
            if (message.equalsIgnoreCase(possibleAnswer)) {
                theEvent.setCancelled(true);
                executeCorrectAnswerTasks(player, true, possibleAnswer);
                return;
            }
        }
    }

    private void executeCorrectAnswerTasks(Player player, Boolean secretAnswerTriggered, String answer) {
        //Time sensitive tasks
        long timeAnswered = System.currentTimeMillis();
        long startTime = gameManager.getTimeQuestionSent();
        int timeTaken = (int)(timeAnswered - startTime);
        if(checkIfUnderPermissibleTime(timeTaken, player) || checkIfUnderPermissibleSD(timeTaken, player)) {
            return;
        }
        if(gameManager.isQuestionAnswered()) {
            long processingTime = System.currentTimeMillis() - timeAnswered;
            String log = String.format(DEBUG_LOG_RACE_CONDITION, player.getName(), timeTaken, processingTime);
            Bukkit.getLogger().info(log);
            return;
        }
        gameManager.setQuestionAnswered(true);
        //long processingTime = System.currentTimeMillis() - timeAnswered;
        //Bukkit.getLogger().info("[HoloQuiz Debug Log] Processing done in " + processingTime + "ms");

        Question answeredQuestion = gameManager.getCurrentQuestion();
        String gameMode = gameManager.getGameModeIdentifier();
        //The actual tasks
        //Update database
        database.updateAfterCorrectAnswer(player, timeAnswered, timeTaken, gameMode);

        //Give Rewards
        int statusCodeOne = -1;
        if(secretAnswerTriggered) {
            sendSecretAnnouncement(player, timeTaken, answeredQuestion);
            statusCodeOne = rewardsHandler.giveSecretRewards(player, timeTaken);
        } else {
            sendNormalAnnouncement(answer, player, timeTaken, answeredQuestion);
        }
        int statusCodeTwo = rewardsHandler.giveNormalRewards(player, timeTaken);

        //Send some Messages
        sendCorrectAnswerAnnouncement(player, correctAnswerMsgLoc);
        new BukkitRunnable() {
            public void run() {
                makeFireworks(player);
            }
        }.runTask(plugin);
        sendUserMessage(player, statusCodeOne, statusCodeTwo);

        //Log to Console
        String logInfo = String.format(CORRECT_ANSWER_LOG, player.getName(), timeTaken);
        Bukkit.getLogger().info(logInfo);

        //Update for ended contests if necessary
        contestManager.updateContestsStatus();
    }

    private void sendNormalAnnouncement(String possibleAnswer, Player answerer, long timeTaken, Question question) {
        String playerName = answerer.getName();
        double timeTakenInSeconds = timeTaken / 1000.0;
        String message = String.format(CORRECT_ANSWER_ANNOUNCEMENT, playerName, timeTakenInSeconds, possibleAnswer);
        if(question.getExtraMessage() != null) {
            message = message + question.getExtraMessage();
        }
        String announcement = userInterface.attachLabel(message);
        announcement = userInterface.formatColours(announcement);

        for(Player player : plugin.getServer().getOnlinePlayers()) {
           userInterface.attachSuffixAndSend(player, announcement);
        }
    }

    private void sendSecretAnnouncement(Player answerer, long timeTaken, Question question) {
        String playerName = answerer.getName();
        double timeTakenInSeconds = timeTaken / 1000.0;
        String message = String.format(SECRET_ANSWER_ANNOUNCEMENT, playerName, timeTakenInSeconds);
        String announcement = userInterface.attachLabel(message);
        announcement = userInterface.formatColours(announcement);
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            userInterface.attachSuffixAndSend(player, announcement);
        }
        String secretAnnouncement = question.getSecretMessage();
        secretAnnouncement = userInterface.formatColours(secretAnnouncement);
        userInterface.attachSuffixAndSend(answerer, secretAnnouncement);
        answerer.sendMessage();
    }

    private void makeFireworks(Player player) {
        Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        FireworkEffect.Builder builder = FireworkEffect.builder();

        builder.flicker(true).withColor(Color.AQUA);
        builder.withFade(Color.YELLOW);
        builder.trail(true);
        builder.with(FireworkEffect.Type.BURST);

        fireworkMeta.addEffect(builder.build());
        fireworkMeta.setPower(1);
        firework.setFireworkMeta(fireworkMeta);
    }

    private void sendCorrectAnswerAnnouncement(Player player, int status) {
        if(status == 0) {
            displayTitle(player);
        }
        else if(status == 1) {
            displayActionBar(player);
        }
    }

    private void displayActionBar(Player player) {
        String message = "&2Congratulations! &3You have answered correctly!";
        String announcement = ChatColor.translateAlternateColorCodes('&', message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(announcement));
    }

    private void displayTitle(Player player) {
        String message1 = "&2Congratulations!";
        String message2 = "&3You have answered correctly!";
        String announcement1 = ChatColor.translateAlternateColorCodes('&', message1);
        String announcement2 = ChatColor.translateAlternateColorCodes('&', message2);
        player.sendTitle(announcement1, announcement2, 10, 60, 10);
    }

    public boolean checkIfUnderPermissibleTime(int timeTaken, Player player) {
        //Return true if you need to skip the person caught cheating
        if (!timeChecker.isEnabled()) {
            return false;
        }
        if (timeTaken < timeChecker.getMinTimeRequired()) {
            for (String peko : timeChecker.getCheatingCommands()) {
                String command = userInterface.antiCheatCommandFormatter(peko, player.getName(), timeTaken / 1000.0);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                });
            }
            return !timeChecker.isCountAsCorrect();
        }
        return false;
    }

    private boolean checkIfUnderPermissibleSD(int timeTaken, Player player) {
        if(!sdChecker.isEnabled()) {
            return false;
        }
        List<Double> listOfTimes = database.fetchPrevTimes(sdChecker.getMinAnsUsed() - 1, player);
        if(listOfTimes.size() + 1 < sdChecker.getMinAnsUsed()) {
            return false;
        }
        listOfTimes.add(timeTaken / 1000.0);
        double stdDev = calculateStdDev(listOfTimes);
        if(stdDev < sdChecker.getMinSDReq()) {
            for (String peko : sdChecker.getCheatingCommands()) {
                String command = userInterface.antiCheatCommandFormatter(peko, player.getName(), stdDev);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                });
            }
            return !sdChecker.isCountAsCorrect();
        }
        return false;
    }

    private void sendUserMessage(Player player, int statusCodeOne, int statusCodeTwo) {
        if(statusCodeTwo == 2 || statusCodeOne == 2) {

            String fullInvMessage = userInterface.formatColours(SRTS_TRIGGERED_MESSAGE);
            userInterface.attachSuffixAndSend(player, fullInvMessage);
        }
        if(statusCodeTwo == 1 || statusCodeOne == 1) {

            String fullInvMessage = userInterface.formatColours(INVENTORY_FULL_MESSAGE);
            userInterface.attachSuffixAndSend(player, fullInvMessage);
        }
    }

    private double calculateStdDev(List<Double> timings) {
        int size = timings.size();

        double total = 0;
        for (double time : timings) {
            total += time;
        }
        double mean = total / size;

        double sumSquaredDiffs = 0.0;
        for (double time : timings) {
            double diff = time - mean;
            sumSquaredDiffs += diff * diff;
        }

        // Standard deviation = sqrt(variance)
        return Math.sqrt(sumSquaredDiffs / size);
    }
}
