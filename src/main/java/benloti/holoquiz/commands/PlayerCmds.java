package benloti.holoquiz.commands;

import benloti.holoquiz.database.UserPersonalisation;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.structs.PlayerData;
import benloti.holoquiz.leaderboard.Leaderboard;
import benloti.holoquiz.structs.PlayerSettings;
import net.md_5.bungee.api.ChatColor;
import benloti.holoquiz.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerCmds implements CommandExecutor {
    private static final String TABLE_BORDER = "&9=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";

    public static final String NOTIFY_HOLOQUIZ_STARTED = "&bHoloQuiz &ahas started!";
    public static final String NOTIFY_HOLOQUIZ_STOPPED = "&bHoloQuiz &chas been stopped!";

    public static final String ERROR_NO_PERMS = "&cYou do not have permissions to do this!";
    public static final String ERROR_NO_PLAYER_FOUND = "&cNo such player found!";
    public static final String ERROR_HOLOQUIZ_IS_STOPPED = "&bYou can't do that, HoloQuiz &cis stopped!";
    public static final String ERROR_HOLOQUIZ_IS_ALREADY_RUNNING = "&bYou can't do that, HoloQuiz &cis already running!";
    public static final String ERROR_INCORRECT_COMMAND = "&bWhat are you doing peko";

    public static final String MSG_PLAYER_STATS_FORMAT = "&b|Answers: &6%s &b| Best Time: &6%ss &b| Average Time: &6%ss &b|";
    public static final String MSG_PLAYER_NAME_FORMAT = "&9=-=-=-=-=-=-=[ &bPlayer Stats for &a&l%s&9 ]=-=-=-=-=-=-=";
    public static final String MSG_ANSWER_LIST_FORMAT = "&2- &b%s";
    public static final String MSG_ANSWER_HEADER_FORMAT = "&bAnswers: ";
    public static final String MSG_QUESTION_STATUS_ANSWERED_FORMAT = "&bThe Question has been &aAnswered!";
    public static final String MSG_QUESTION_STATUS_UNANSWERED_FORMAT = "&bThe Question is &cNot Yet Answered!";
    public static final String MSG_NEXT_QUESTION_COUNTDOWN_FORMAT = "&bNext Question is in &6%s seconds";
    public static final String MSG_DISPLAY_QUESTION_FORMAT = "&bQuestion: &6%s";

    public static final String MSG_LEADERBOARD_HEADER_MOST_ANSWERS =
            "&9=-=-=-=-=-=-=[ &bLeaderboard for &aTop %s Most Answers&9 ]=-=-=-=-=-=-=";
    public static final String MSG_LEADERBOARD_HEADER_FASTEST_ANSWERS =
            "&9=-=-=-=-=-=-=[ &bLeaderboard for &aTop %s Fastest Times&9 ]=-=-=-=-=-=-=";
    public static final String MSG_LEADERBOARD_HEADER_AVERAGE_BEST_ANSWERS =
            "&9=-=-=-=-=-=-=[ &bLeaderboard for &aTop %s Fastest on Average&9 ]=-=-=-=-=-=-=";
    public static final String MSG_LEADERBOARD_BODY_MOST_ANSWERS_FORMAT =
            "&3%s. &e%s&3: &6%s &3Best Time: &2%s &3| Average Time: &2%s";
    public static final String MSG_LEADERBOARD_BODY_FASTEST_ANSWERS_FORMAT =
            "&3%s. &e%s&3: &2%s &eBest Time: &6%s &3| Average Time: &2%s";
    public static final String MSG_LEADERBOARD_BODY_AVERAGE_BEST_ANSWERS_FORMAT =
            "&3%s. &e%s&3: &2%s &3Best Time: &2%s &3| &eAverage Time: &6%s";

    public static final String MSG_HOLOQUIZ_MESSAGES_ENABLED = "&bHoloQuiz will now be&a shown to you%s!";
    public static final String MSG_HOLOQUIZ_MESSAGES_DISABLED = "&bHoloQuiz will&c no longer be shown to you%s!";

    private final GameManager gameManager;
    private final DatabaseManager databaseManager;
    private final Leaderboard leaderboard;
    private final boolean easterEggs;
    private final UserPersonalisation userPersonalisation;

    public PlayerCmds(GameManager gameManager, DatabaseManager databaseManager, Leaderboard leaderboard, ConfigFile configFile) {
        this.databaseManager = databaseManager;
        this.gameManager = gameManager;
        this.leaderboard = leaderboard;
        this.easterEggs = configFile.isEasterEggsEnabled();
        this.userPersonalisation = databaseManager.getUserPersonalisation();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command theCommand, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = ((Player) sender).getPlayer();
        assert player != null;

        if (args.length == 0) {
            Bukkit.getLogger().info("args is null");
            player.sendMessage("You need help fam?");
            return false;
        }

        if (runAdminCommand(player, args)) {
            return true;
        }

        if (runUserCommand(player, args)) {
            return true;
        }

        if(easterEggs) {
            return runEasterEggCommands(player, args);
        }
        return false;
    }

    private boolean runAdminCommand(Player player, String[] args) {
        if (!player.hasPermission("HoloQuiz.admin")) {
            //formatInformationForPlayer(ERROR_NO_PERMS, player); //Yes, this is incorrectly set up lmaoo
            return false;
        }

        if (args[0].equals("info")) {
            String[] information = obtainQuestionInfo(true);
            formatInformationForPlayer(information, player);
            return true;
        }

        if (args[0].equals("next")) {
            if (!gameManager.getGameStatus()) {
                formatInformationForPlayer(ERROR_HOLOQUIZ_IS_STOPPED, player);
                return true;
            }

            gameManager.nextQuestion();
            return true;
        }

        if (args[0].equals("stop")) {
            if (!gameManager.getGameStatus()) {
                formatInformationForPlayer(ERROR_HOLOQUIZ_IS_STOPPED, player);
                return true;
            }

            gameManager.stopGame();
            formatInformationForPlayer(NOTIFY_HOLOQUIZ_STOPPED, player);
            return true;
        }

        if (args[0].equals("start")) {
            if(gameManager.getGameStatus()) {
                formatInformationForPlayer(ERROR_HOLOQUIZ_IS_ALREADY_RUNNING, player);
                return true;
            }

            gameManager.startGame();
            formatInformationForPlayer(NOTIFY_HOLOQUIZ_STARTED, player);
            return true;
        }

        return false;
    }

    private boolean runUserCommand(Player player, String[] args) {
        if (args[0].equals("help")) {
            player.sendMessage("HELP ME PLS");
            return true;
        }

        if (args[0].equals("info")) {
            String[] information = obtainQuestionInfo(false);
            formatInformationForPlayer(information, player);
            return true;
        }

        if (args[0].equals("stats")) {
            if (args.length == 1) {
                displayPlayerStats(player, player.getName());
            } else {
                displayPlayerStats(player, args[1]);
            }
            return true;
        }

        if(args[0].equals("toggle")) {
            toggleHoloquizNotification(player);
            return true;
        }

        if (args[0].equals("top") && args.length > 1) {
            switch (args[1]) {
            default:
                formatInformationForPlayer(ERROR_INCORRECT_COMMAND, player);
                return false;
            case "best":
                String [] topPlayers = displayTopPlayers(leaderboard.getFastest(),
                        MSG_LEADERBOARD_HEADER_FASTEST_ANSWERS, MSG_LEADERBOARD_BODY_FASTEST_ANSWERS_FORMAT);
                formatInformationForPlayer(topPlayers, player);
                return true;
            case "average":
                topPlayers = displayTopPlayers(leaderboard.getAverageBest(),
                        MSG_LEADERBOARD_HEADER_AVERAGE_BEST_ANSWERS, MSG_LEADERBOARD_BODY_AVERAGE_BEST_ANSWERS_FORMAT);
                formatInformationForPlayer(topPlayers, player);
                return true;
            case "answers":
                topPlayers = displayTopPlayers(leaderboard.getMostAnswers(),
                        MSG_LEADERBOARD_HEADER_MOST_ANSWERS, MSG_LEADERBOARD_BODY_MOST_ANSWERS_FORMAT);
                formatInformationForPlayer(topPlayers, player);
                return true;
            }
        }
        return false;
    }

    private boolean runEasterEggCommands(Player player, String[] args) {
        if (args[0].equals("pekofy")) {
            player.sendMessage("Peko Peko Peko!!!");
            return true;
        }
        return false;
    }

    private void toggleHoloquizNotification(Player player) {
        String playerUUID = player.getUniqueId().toString();
        String holoQuizNotificationFormatted;
        PlayerSettings playerSettings = userPersonalisation.getPlayerSettings(playerUUID);

        if(playerSettings == null) {
            userPersonalisation.setNotificationSetting(playerUUID, false);
            holoQuizNotificationFormatted = String.format(MSG_HOLOQUIZ_MESSAGES_DISABLED, "");
            formatInformationForPlayer(holoQuizNotificationFormatted, player);
            return;
        }
        boolean newNotificationSetting = !playerSettings.isNotificationEnabled();
        String playerSuffix = playerSettings.getSuffix();
        userPersonalisation.setNotificationSetting(playerUUID, newNotificationSetting);

        if(newNotificationSetting) {
            holoQuizNotificationFormatted = String.format(MSG_HOLOQUIZ_MESSAGES_ENABLED, playerSuffix);
        } else {
            holoQuizNotificationFormatted = String.format(MSG_HOLOQUIZ_MESSAGES_DISABLED, playerSuffix);
        }
        formatInformationForPlayer(holoQuizNotificationFormatted, player);
    }

    private String[] obtainQuestionInfo(boolean adminInfoRequired) {
        if (!gameManager.getGameStatus()) {
            return new String[]{ERROR_HOLOQUIZ_IS_STOPPED};
        }

        String currentQuestion = gameManager.getCurrentQuestion().getQuestion();
        long currentTime = System.currentTimeMillis();
        //Simple calculations for later
        long timeQuestionSent = gameManager.getTimeQuestionSent();
        long questionInterval = gameManager.getInterval();
        int timeLeft = (int) (timeQuestionSent - currentTime + questionInterval * 1000);
        double timeLeftInSeconds = timeLeft / 1000.0;
        boolean questionStatus = gameManager.getQuestionStatus();

        String currentQuestionFormatted = String.format(MSG_DISPLAY_QUESTION_FORMAT, currentQuestion);
        String timeLeftFormatted = String.format(MSG_NEXT_QUESTION_COUNTDOWN_FORMAT, timeLeftInSeconds);
        String questionStatusFormatted;
        if (questionStatus) {
            questionStatusFormatted = MSG_QUESTION_STATUS_ANSWERED_FORMAT;
        } else {
            questionStatusFormatted = MSG_QUESTION_STATUS_UNANSWERED_FORMAT;
        }

        String[] basicInfo = {TABLE_BORDER, questionStatusFormatted, timeLeftFormatted,
                currentQuestionFormatted, TABLE_BORDER};
        if (!adminInfoRequired) {
            return basicInfo;
        }

        //ADMIN ONLY!!
        ArrayList<String> information = new ArrayList<>();

        basicInfo[4] = MSG_ANSWER_HEADER_FORMAT;
        Collections.addAll(information, basicInfo);

        List<String> answersList = gameManager.getCurrentQuestion().getAnswers();
        for (String s : answersList) {
            String formattedAnswer = String.format(MSG_ANSWER_LIST_FORMAT, s);
            information.add(formattedAnswer);
        }

        int size = information.size();
        String [] finalInfoArray = new String[size];
        for(int i = 0; i < size; i++) {
            finalInfoArray[i] = information.get(i);
        }
        return finalInfoArray;
    }

    private void displayPlayerStats(Player player, String playerName) {
        PlayerData playerData = databaseManager.loadPlayerData(playerName);
        if (playerData == null) {
            formatInformationForPlayer(ERROR_NO_PLAYER_FOUND, player);
            return;
        }

        String playerStats = String.format(MSG_PLAYER_STATS_FORMAT, playerData.getQuestionsAnswered(),
                playerData.getBestTimeInSeconds3DP(), playerData.getAverageTimeInSeconds3DP());
        String playerNameBorder = String.format(MSG_PLAYER_NAME_FORMAT, playerData.getPlayerName());
        String[] information = {playerNameBorder, playerStats};
        formatInformationForPlayer(information, player);
    }

    private String[] displayTopPlayers(ArrayList<PlayerData> topPlayers, String headerFormat, String bodyFormat) {
        int size = leaderboard.getAmountOfPlayersToShow();
        if(size > topPlayers.size()) {
            size = topPlayers.size();
        }
        String [] info = new String[size + 1];
        info[0] = String.format(headerFormat, size);
        for(int i = 0; i < size; i ++) {
            PlayerData currentPlayerData = topPlayers.get(i);
            String playerName = currentPlayerData.getPlayerName();;
            String bestTime = currentPlayerData.getBestTimeInSeconds3DP();
            String averageTime = currentPlayerData.getAverageTimeInSeconds3DP();
            int totalAnswers = currentPlayerData.getQuestionsAnswered();
            info[i+1] = String.format(bodyFormat,i+1, playerName,totalAnswers,bestTime,averageTime);
        }
        return info;
    }

    private void formatInformationForPlayer(String[] message, Player player) {
        int i = 0;
        for (String s : message) {
            message[i] = ChatColor.translateAlternateColorCodes('&', s);
            i += 1;
        }
        player.sendMessage(message);
    }

    private void formatInformationForPlayer(String message, Player player) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        player.sendMessage(message);
    }

}
