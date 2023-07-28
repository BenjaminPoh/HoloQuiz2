package benloti.holoquiz.commands;

import benloti.holoquiz.database.UserPersonalisation;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.structs.PlayerData;
import benloti.holoquiz.leaderboard.Leaderboard;
import benloti.holoquiz.structs.PlayerSettings;
import benloti.holoquiz.database.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerCmds implements CommandExecutor {
    public static final String ERROR_NO_SUCH_COMMAND = "No Such Command! Are you trying to find Easter Eggs?";
    private static final String TABLE_BORDER = "&9=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";

    public static final String NOTIFY_HOLOQUIZ_STARTED = "&bHoloQuiz &ahas started!";
    public static final String NOTIFY_HOLOQUIZ_STOPPED = "&bHoloQuiz &chas been stopped!";

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

    public static final String HELP_TABLE = "" +
            "&9=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=\n" +
            "&a/HoloQuiz info: &bShows information on the current question\n" +
            "&a/HoloQuiz top <best/average/answers>: &bShows the best of the best!\n" +
            "&a/HoloQuiz stats [player]: &bShows your own / someone else's statistics\n" +
            "&a/HoloQuiz toggle: &bToggles messages from HoloQuiz\n" +
            "&a/HoloQuiz normal: &bSwitches off &6/HoloQuiz Pekofy\n" +
            "&9=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";

    public static final String EASTER_EGG_FBK_GLASSES = "First, you can have glasses-wearing girls take them off and " +
            "suddenly become beautiful, or have girls wearing glasses flashing those cute grins, " +
            "or have girls stealing the protagonist's glasses and putting them on like, \"Haha, got your glasses!\" " +
            "That's just way too cute! Also, boys with glasses! I really like when their glasses have that suspicious " +
            "looking gleam, and it's amazing how it can look really cool or just be a joke. " +
            "I really like how it can fulfill all those abstract needs. Being able to switch up the styles and " +
            "colors of glasses based on your mood is a lot of fun too! It's actually so much fun! " +
            "You have those half rim glasses, or the thick frame glasses, everything! " +
            "It's like you're enjoying all these kinds of glasses at a buffet. " +
            "I really want Luna to try some on or Marine to try some on to replace her eyepatch. " +
            "We really need glasses to become a thing in hololive and start selling them for HoloComi. " +
            "Don't. You. Think. We. Really. Need. To. Officially. Give. Everyone. Glasses?";

    private final GameManager gameManager;
    private final DatabaseManager databaseManager;
    private final Leaderboard leaderboard;
    private final boolean easterEggs;
    private final UserPersonalisation userPersonalisation;
    private final UserInterface userInterface;

    public PlayerCmds(GameManager gameManager, DatabaseManager databaseManager, Leaderboard leaderboard,
                      ConfigFile configFile, UserInterface userInterface) {
        this.databaseManager = databaseManager;
        this.gameManager = gameManager;
        this.leaderboard = leaderboard;
        this.easterEggs = configFile.isEasterEggsEnabled();
        this.userPersonalisation = databaseManager.getUserPersonalisation();
        this.userInterface = userInterface;
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
            formatInformationForPlayer(HELP_TABLE, player);
            return true;
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
        String error_message = userInterface.formatColours(ERROR_NO_SUCH_COMMAND);
        userInterface.attachSuffixAndSend(player, error_message);
        return false;
    }

    private boolean runAdminCommand(Player player, String[] args) {
        if (!player.hasPermission("HoloQuiz.admin")) {
            //formatInformationForPlayer(ERROR_NO_PERMS, player); //Yes, this is incorrectly set up lmaoo
            return false;
        }

        if (args[0].equalsIgnoreCase("info")) {
            String[] information = obtainQuestionInfo(true);
            formatInformationForPlayer(information, player);
            return true;
        }

        if (args[0].equalsIgnoreCase("next")) {
            if (!gameManager.getGameStatus()) {
                formatInformationForPlayer(ERROR_HOLOQUIZ_IS_STOPPED, player);
                return true;
            }

            gameManager.nextQuestion();
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            if (!gameManager.getGameStatus()) {
                formatInformationForPlayer(ERROR_HOLOQUIZ_IS_STOPPED, player);
                return true;
            }

            gameManager.stopGame();
            formatInformationForPlayer(NOTIFY_HOLOQUIZ_STOPPED, player);
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
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
        if (args[0].equalsIgnoreCase("help")) {
            formatInformationForPlayer(HELP_TABLE, player);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            String[] information = obtainQuestionInfo(false);
            formatInformationForPlayer(information, player);
            return true;
        }

        if (args[0].equalsIgnoreCase("stats")) {
            if (args.length == 1) {
                displayPlayerStats(player, player.getName());
            } else {
                displayPlayerStats(player, args[1]);
            }
            return true;
        }

        if(args[0].equalsIgnoreCase("toggle")) {
            toggleHoloQuizNotification(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("top") && args.length > 1) {
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
        if (args[0].equalsIgnoreCase("pekofy")) {
            player.sendMessage("Peko Peko Peko!!!");
            userPersonalisation.setSuffix(player.getUniqueId().toString(), " peko");
            return true;
        }
        if (args[0].equalsIgnoreCase("nanora")) {
            player.sendMessage("Nanoranora?");
            userPersonalisation.setSuffix(player.getUniqueId().toString(), " nanora");
            return true;
        }
        if (args[0].equalsIgnoreCase("shuba")) {
            player.sendMessage("Oozora Shuba!");
            userPersonalisation.setSuffix(player.getUniqueId().toString(), " shuba");
            return true;
        }
        if (args[0].equalsIgnoreCase("normal")) {
            player.sendMessage("HoloQuiz is now normal!");
            userPersonalisation.setSuffix(player.getUniqueId().toString(), "");
            return true;
        }
        if (args[0].equalsIgnoreCase("GlassesAreReallyVersatile")) {
            player.sendMessage(EASTER_EGG_FBK_GLASSES);
        }
        if (args[0].equalsIgnoreCase("PekoPasta")) {
            player.sendMessage("So as a joke...");
        }
        return false;
    }

    private void toggleHoloQuizNotification(Player player) {
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
            String playerName = currentPlayerData.getPlayerName();
            String bestTime = currentPlayerData.getBestTimeInSeconds3DP();
            String averageTime = currentPlayerData.getAverageTimeInSeconds3DP();
            int totalAnswers = currentPlayerData.getQuestionsAnswered();
            info[i+1] = String.format(bodyFormat,i+1, playerName,totalAnswers,bestTime,averageTime);
        }
        return info;
    }

    private void formatInformationForPlayer(String[] message, Player player) {
        message = userInterface.formatColoursArray(message);
        player.sendMessage(message);
    }

    private void formatInformationForPlayer(String message, Player player) {
        message = userInterface.formatColours(message);
        player.sendMessage(message);
    }
}
