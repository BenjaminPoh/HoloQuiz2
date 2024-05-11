package benloti.holoquiz.games;

import benloti.holoquiz.HoloQuiz;
import benloti.holoquiz.files.ConfigFile;
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

    private static final String INVENTORY_FULL_MESSAGE =
            "&bYour Inventory was full! Rewards has been sent to Storage. Do &a/holoquiz collect &bto get them!";

    private final GameManager gameManager;
    private final HoloQuiz plugin;
    private final DatabaseManager database;
    private final RewardsHandler rewardsHandler;
    private final UserInterface userInterface;
    private final ConfigFile configFile;

    public QuizAnswerHandler(HoloQuiz plugin, GameManager gameManager, DatabaseManager database,
                             UserInterface userInterface, ConfigFile configFile) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.gameManager = gameManager;
        this.plugin = plugin;
        this.database = database;
        this.rewardsHandler = gameManager.getRewardsHandler();
        this.userInterface = userInterface;
        this.configFile = configFile;
    }

    @EventHandler
    public void checkAnswerSent(AsyncPlayerChatEvent theEvent) {
        if(!gameManager.getGameStatus() || gameManager.getQuestionStatus()) {
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
        if(cheatHandler(timeTaken, player)) {
            return;
        }
        gameManager.setQuestionStatus(true);

        Question answeredQuestion = gameManager.getCurrentQuestion();
        String gameMode = gameManager.getGameModeIdentifier();
        //The actual tasks
        boolean fullInvFlagOne = false;
        if(secretAnswerTriggered) {
            sendSecretAnnouncement(player, timeTaken, answeredQuestion);
            fullInvFlagOne = rewardsHandler.giveSecretRewards(player, timeTaken);
        } else {
            sendNormalAnnouncement(answer, player, timeTaken, answeredQuestion);
        }
        //displayActionBar(player); //Not what I want, but the bug is now a feature
        boolean fullInvFlagTwo = rewardsHandler.giveNormalRewards(player, timeTaken);
        displayTitle(player);
        new BukkitRunnable() {
            public void run() {
                makeFireworks(player);
            }
        }.runTask(plugin);
        if(fullInvFlagTwo || fullInvFlagOne) {
            String fullInvMessage = userInterface.formatColours(INVENTORY_FULL_MESSAGE);
            userInterface.attachSuffixAndSend(player, fullInvMessage);
        }

        //Update database
        database.updateAfterCorrectAnswer(player, timeAnswered, timeTaken, gameMode);

        //Log it
        String logInfo = String.format(CORRECT_ANSWER_LOG, player.getName(), timeTaken);
        Bukkit.getLogger().info(logInfo);
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

    private void displayActionBar(Player player) {
        String message = "&2Congratulations!\n&3You have answered correctly!";
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

    private boolean cheatHandler(int timeTaken, Player player) {
        //Return true if you need to skip the person caught cheating
        if(!configFile.isCheatsDetectorEnabled()) {
            return false;
        }
        if(timeTaken < configFile.getMinTimeRequired()) {
            for(String peko : configFile.getCheatingCommands()) {
                String command = userInterface.attachPlayerName(peko, player);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    Bukkit.getLogger().info(command);
                });
            }
            return !configFile.isCountAsCorrect();
        }
        return false;
    }
}
