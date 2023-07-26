package benloti.holoquiz.games;

import benloti.holoquiz.HoloQuiz;
import benloti.holoquiz.database.UserPersonalisation;
import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.dependencies.VaultDep;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.leaderboard.Leaderboard;
import benloti.holoquiz.structs.PlayerData;
import benloti.holoquiz.database.DatabaseManager;
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
    private final VaultDep economy;
    private final GameManager gameManager;
    private final HoloQuiz plugin;
    private final DatabaseManager database;
    private final Leaderboard leaderboard;
    private final RewardsHandler rewardsHandler;
    private final UserInterface userInterface;
    private final ConfigFile configFile;

    public QuizAnswerHandler(HoloQuiz plugin, GameManager gameManager, DatabaseManager database,
                             Leaderboard leaderboard, DependencyHandler dependencyHandler, UserInterface userInterface,
                             ConfigFile configFile) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.gameManager = gameManager;
        this.plugin = plugin;
        this.database = database;
        this.leaderboard = leaderboard;
        this.economy = dependencyHandler.getVaultDep();
        this.rewardsHandler = gameManager.getRewardsHandler();
        this.userInterface = userInterface;
        this.configFile = configFile;
    }

    @EventHandler
    public void correctAnswerSent(AsyncPlayerChatEvent theEvent) {
        if(!gameManager.getGameStatus() || gameManager.getQuestionStatus()) {
            return;
        }
        String message = theEvent.getMessage();
        Player player = theEvent.getPlayer();
        List<String> answers = gameManager.getCurrentQuestion().getAnswers();
        for(String possibleAnswer : answers) {
            if (message.equalsIgnoreCase(possibleAnswer)) {
                //Time sensitive tasks
                long timeAnswered = System.currentTimeMillis();
                long startTime = gameManager.getTimeQuestionSent();
                int timeTaken = (int)(timeAnswered - startTime);
                if(cheatHandler(timeTaken, player)) {
                    return;
                }
                gameManager.setQuestionStatus(true);

                //The actual tasks
                sendAnnouncement(possibleAnswer, player, timeTaken);
                //displayActionBar(player); //Not what I want, but the bug is now a feature
                //addBalance(player,timeTaken);
                rewardsHandler.giveRewards(player, timeTaken);
                displayTitle(player);
                new BukkitRunnable() {
                    public void run() {
                        makeFireworks(player);
                    }
                }.runTask(plugin);

                //Update database
                PlayerData playerData = database.updateAfterCorrectAnswer(player, timeAnswered,timeTaken);

                //update leaderboards
                leaderboard.updateLeaderBoard(playerData);
                return;
            }
        }
    }

    private void sendAnnouncement(String possibleAnswer, Player answerer, long timeTaken) {
        String playerName = answerer.getName();
        double timeTakenInSeconds = timeTaken / 1000.0;
        String message = String.format(CORRECT_ANSWER_ANNOUNCEMENT, playerName, timeTakenInSeconds, possibleAnswer);
        String announcement = userInterface.attachLabel(message);
        announcement = userInterface.formatColours(announcement);

        for(Player player : plugin.getServer().getOnlinePlayers()) {
           userInterface.sendMessageToPlayer(player, announcement);
        }
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

    private void addBalance(Player player, double amount) {
        if(economy == null) {
            return;
        }
        economy.addBalance(player.getName(), amount);
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
