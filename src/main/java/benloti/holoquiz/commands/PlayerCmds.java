package benloti.holoquiz.commands;

import benloti.holoquiz.HoloQuiz;
import benloti.holoquiz.database.UserPersonalisation;
import benloti.holoquiz.files.ContestManager;
import benloti.holoquiz.files.ExternalFiles;
import benloti.holoquiz.files.MessageFormatter;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.structs.PlayerData;
import benloti.holoquiz.structs.PlayerSettings;
import benloti.holoquiz.database.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerCmds implements CommandExecutor {
    public static final String ERROR_NO_SUCH_COMMAND = "No Such Command! Are you trying to find Easter Eggs?";
    public static final String TABLE_BORDER = "&9=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";

    public static final String NOTIFY_HOLOQUIZ_STARTED = "&bHoloQuiz &ahas started!";
    public static final String NOTIFY_HOLOQUIZ_STOPPED = "&bHoloQuiz &chas been stopped!";
    public static final String NOTIFY_RELOADING = "&bHoloQuiz &cis reloading...";
    public static final String NOTIFY_RELOADED = "&bHoloQuiz &ahas reloaded!";
    public static final String NOTIFY_STORAGE_CLEARED = "&aAll rewards collected!";
    public static final String NOTIFY_STORAGE_NOT_CLEARED = "&cMore rewards await you!";
    public static final String NOTIFY_STORAGE_EMPTY = "&4You have no rewards stored!";
    public static final String NOTIFY_BLOCKED_WORLD = "&4You are not allowed to claim rewards in this World!";
    public static final String NOTIFY_UNKNOWN_ERROR = "&4You should not see this message. How did you get here?";
    public static final String NOTIFY_NO_CONTEST = "&4There does not seem to be any contests running!";

    public static final String ERROR_QUESTION_FILE_BROKEN = "&cQuestion File broken! Aborting reload!";
    public static final String ERROR_CONFIG_FILE_BROKEN = "&cA file is broken! Aborting reload!";
    public static final String ERROR_NO_PLAYER_FOUND = "&cNo such player found!";
    public static final String ERROR_HOLOQUIZ_IS_STOPPED = "&bYou can't do that, HoloQuiz &cis stopped!";
    public static final String ERROR_HOLOQUIZ_IS_ALREADY_RUNNING = "&bYou can't do that, HoloQuiz &cis already running!";
    public static final String ERROR_INCORRECT_COMMAND = "&bWhat are you doing peko";

    public static final String MSG_PLAYER_STATS_FORMAT = "&b|Answers: &6%s &b| Best Time: &6%ss &b| Average Time: &6%ss &b|";
    public static final String MSG_PLAYER_NAME_FORMAT = "&9=-=-=-=-=-=-=[ &bPlayer Stats for &a&l%s&9 ]=-=-=-=-=-=-=";
    public static final String MSG_ANSWER_LIST_FORMAT = "&2- &b%s";
    public static final String MSG_ANSWER_HEADER_FORMAT = "&bAnswers: ";
    public static final String MSG_QUESTION_STATUS_ANSWERED_FORMAT = "&bThe Question has been &aAnswered!";
    public static final String MSG_QUESTION_STATUS_TIMEOUT_FORMAT = "&bThe Question has &cTimed Out!";
    public static final String MSG_QUESTION_STATUS_UNANSWERED_FORMAT = "&bThe Question is &cNot Yet Answered!";
    public static final String MSG_NEXT_QUESTION_COUNTDOWN_FORMAT = "&bNext Question is in &6%s seconds";
    public static final String MSG_ANSWER_REVEAL_COUNTDOWN_FORMAT = "&bAnswer will be revealed is in &6%s seconds";
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

    public static final String HELP_TABLE = "&9=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=\n" +
            "&a/HoloQuiz info: &bShows information on the current question\n" +
            "&a/HoloQuiz top <best/average/answers>: &bShows the best of the best!\n" +
            "&a/HoloQuiz stats [player]: &bShows your own / someone else's statistics\n" +
            "&a/HoloQuiz toggle: &bToggles messages from HoloQuiz\n" +
            "&a/HoloQuiz collect: &bCollect rewards stored in Storage\n" +
            "&a/HoloQuiz contest: &bView the current placements for the HoloQuiz Contests\n" +
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

    private GameManager gameManager;
    private final DatabaseManager databaseManager;
    private boolean easterEggs;
    private final UserPersonalisation userPersonalisation;
    private ExternalFiles externalFiles;
    private ContestManager contestManager;
    private final HoloQuiz holoQuiz;

    public PlayerCmds(GameManager gameManager, DatabaseManager databaseManager, ExternalFiles externalFiles,
                      ContestManager contestManager, HoloQuiz plugin) {
        this.databaseManager = databaseManager;
        this.gameManager = gameManager;
        this.easterEggs = externalFiles.getConfigFile().isEasterEggsEnabled();
        this.userPersonalisation = databaseManager.getUserPersonalisation();
        this.externalFiles = externalFiles;
        this.contestManager = contestManager;
        this.holoQuiz = plugin;
    }

    public void reload(GameManager gameManager, ExternalFiles externalFiles, ContestManager contestManager) {
        this.gameManager = gameManager;
        this.easterEggs = externalFiles.getConfigFile().isEasterEggsEnabled();
        this.externalFiles = externalFiles;
        this.contestManager = contestManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command theCommand, String alias, String[] args) {
        boolean isPlayer = sender instanceof Player;
        if (args.length == 0) {
            formatInformationForPlayer(HELP_TABLE, sender);
            return true;
        }

        if (runAdminCommand(sender, args, isPlayer)) {
            return true;
        }

        if (!isPlayer) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = ((Player) sender).getPlayer();
        assert player != null;

        if (runUserCommand(player, args)) {
            return true;
        }

        if (easterEggs) {
            return runEasterEggCommands(player, args);
        }
        return false;
    }

    //Available commands: info, next, stop, start, ReloadQns, repairDB
    private boolean runAdminCommand(CommandSender sender, String[] args, boolean isPlayer) {
        if (isPlayer && !sender.hasPermission("HoloQuiz.admin")) {
            return false;
        }

        if (args[0].equalsIgnoreCase("info")) {
            String[] information = obtainQuestionInfo(true);
            formatInformationForPlayer(information, sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("next")) {
            if (!gameManager.getGameStatus()) {
                formatInformationForPlayer(ERROR_HOLOQUIZ_IS_STOPPED, sender);
                return true;
            }

            gameManager.nextQuestion();
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            if (!gameManager.getGameStatus()) {
                formatInformationForPlayer(ERROR_HOLOQUIZ_IS_STOPPED, sender);
                return true;
            }

            gameManager.stopGame();
            formatInformationForPlayer(NOTIFY_HOLOQUIZ_STOPPED, sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (gameManager.getGameStatus()) {
                formatInformationForPlayer(ERROR_HOLOQUIZ_IS_ALREADY_RUNNING, sender);
                return true;
            }

            gameManager.startGame();
            formatInformationForPlayer(NOTIFY_HOLOQUIZ_STARTED, sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reloadQns")) {
            updateQuestionBank(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reloadStats")) {
            formatInformationForPlayer(NOTIFY_RELOADING, sender);
            int size = databaseManager.reloadDatabase();
            formatInformationForPlayer("Reloaded HoloQuiz stats for " + size + " players!", sender);
            formatInformationForPlayer(NOTIFY_RELOADED, sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reloadConfigFile(sender);
            return true;
        }

        return false;
    }

    // Available commands: help, info, stats, toggle, collect, top
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

        if (args[0].equalsIgnoreCase("toggle")) {
            toggleHoloQuizNotification(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("collect")) {
            String message;
            int statusCode = databaseManager.getRewardsFromStorage(player);
            if (statusCode == 1) {
                message = NOTIFY_STORAGE_NOT_CLEARED;
            } else if (statusCode == 0) {
                message = NOTIFY_STORAGE_CLEARED;
            } else if (statusCode == -2) {
                message = NOTIFY_STORAGE_EMPTY;
            } else if (statusCode == 2){
                message = NOTIFY_BLOCKED_WORLD;
            } else {
                message = NOTIFY_UNKNOWN_ERROR;
            }
            MessageFormatter.getSender().sendToPlayer(player, message, false, true, true);
            return true;
        }

        if(args[0].equalsIgnoreCase("contest") || args[0].equalsIgnoreCase("contests")) {
            if(contestManager.getTotalEnabledSubcontests() == 0) {
                MessageFormatter.getSender().sendToPlayer(player, NOTIFY_NO_CONTEST, false, true, true);
                return true;
            }
            String playerName = player.getName();
            String playerUUID = player.getUniqueId().toString();
            Inventory createdGUI = contestManager.fetchPlayerContestStatus(playerName, playerUUID).getGUI();
            player.openInventory(createdGUI);
            return true;
        }

        if (args[0].equalsIgnoreCase("top") && args.length > 1) {
            int size = externalFiles.getConfigFile().getLeaderboardSize();
            int minReq = externalFiles.getConfigFile().getLeaderboardMinReq();
            ArrayList<PlayerData> topPlayersList;
            String[] topPlayers;
            switch (args[1]) {
            case "best":
                topPlayersList = databaseManager.loadLeaderboard(size, minReq, "best", true);
                topPlayers = displayTopPlayers(topPlayersList,
                        MSG_LEADERBOARD_HEADER_FASTEST_ANSWERS, MSG_LEADERBOARD_BODY_FASTEST_ANSWERS_FORMAT);
                formatInformationForPlayer(topPlayers, player);
                return true;
            case "average":
                topPlayersList = databaseManager.loadLeaderboard(size, minReq, "average", true);
                topPlayers = displayTopPlayers(topPlayersList,
                        MSG_LEADERBOARD_HEADER_AVERAGE_BEST_ANSWERS, MSG_LEADERBOARD_BODY_AVERAGE_BEST_ANSWERS_FORMAT);
                formatInformationForPlayer(topPlayers, player);
                return true;
            case "answers":
                topPlayersList = databaseManager.loadLeaderboard(size, minReq, "answers", false);
                topPlayers = displayTopPlayers(topPlayersList,
                        MSG_LEADERBOARD_HEADER_MOST_ANSWERS, MSG_LEADERBOARD_BODY_MOST_ANSWERS_FORMAT);
                formatInformationForPlayer(topPlayers, player);
                return true;
            default:
                formatInformationForPlayer(ERROR_INCORRECT_COMMAND, player);
                return false;
            }
        }
        return false;
    }

    // Available suffixes: pekofy, nanora, shuba, degozaru, normal
    // Available Easter Eggs: GlassesAreReallyVersatile, pekopasta
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
        if (args[0].equalsIgnoreCase("degozaru")) {
            player.sendMessage("De Gozaru!");
            userPersonalisation.setSuffix(player.getUniqueId().toString(), " de gozaru");
            return true;
        }
        if (args[0].equalsIgnoreCase("normal")) {
            player.sendMessage("HoloQuiz is now normal!");
            userPersonalisation.setSuffix(player.getUniqueId().toString(), "");
            return true;
        }
        if (args[0].equalsIgnoreCase("GlassesAreReallyVersatile")) {
            player.sendMessage(EASTER_EGG_FBK_GLASSES);
            return true;
        }
        if (args[0].equalsIgnoreCase("PekoPasta")) {
            player.sendMessage("So as a joke...");
            return true;
        }
        MessageFormatter.getSender().sendToPlayer(player, ERROR_NO_SUCH_COMMAND, false, true, true);
        return false;
    }

    private void toggleHoloQuizNotification(Player player) {
        String playerUUID = player.getUniqueId().toString();
        String holoQuizNotificationFormatted;
        PlayerSettings playerSettings = userPersonalisation.getPlayerSettings(playerUUID);

        if (playerSettings == null) {
            userPersonalisation.setNotificationSetting(playerUUID, false);
            holoQuizNotificationFormatted = String.format(MSG_HOLOQUIZ_MESSAGES_DISABLED, "");
            formatInformationForPlayer(holoQuizNotificationFormatted, player);
            return;
        }
        boolean newNotificationSetting = !playerSettings.isNotificationEnabled();
        String playerSuffix = playerSettings.getSuffix();
        userPersonalisation.setNotificationSetting(playerUUID, newNotificationSetting);

        if (newNotificationSetting) {
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
        String currentQuestionFormatted = String.format(MSG_DISPLAY_QUESTION_FORMAT, currentQuestion);

        long currentTime = System.currentTimeMillis();
        long timeQuestionSent = gameManager.getNextTaskTime();
        String timeLeftFormatted;
        String questionStatusFormatted;
        long timeLeft = timeQuestionSent - currentTime;
        double timeLeftInSeconds = timeLeft / 1000.0;

        //I call this the Bukit Timah Hill Coding Style
        if (gameManager.isQuestionAnswered()) {
            timeLeftFormatted = String.format(MSG_NEXT_QUESTION_COUNTDOWN_FORMAT, timeLeftInSeconds);
            questionStatusFormatted = MSG_QUESTION_STATUS_ANSWERED_FORMAT;
        } else {
            if(gameManager.isQuestionTimedOut()) {
                timeLeftFormatted = String.format(MSG_NEXT_QUESTION_COUNTDOWN_FORMAT, timeLeftInSeconds);
                questionStatusFormatted = MSG_QUESTION_STATUS_TIMEOUT_FORMAT;
            } else {
                if(gameManager.getRevealAnswerDelay() > 0) {
                    timeLeftFormatted = String.format(MSG_ANSWER_REVEAL_COUNTDOWN_FORMAT, timeLeftInSeconds);
                } else {
                    timeLeftFormatted = String.format(MSG_NEXT_QUESTION_COUNTDOWN_FORMAT, timeLeftInSeconds);
                }
                questionStatusFormatted = MSG_QUESTION_STATUS_UNANSWERED_FORMAT;
            }
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
        String[] finalInfoArray = new String[size];
        for (int i = 0; i < size; i++) {
            finalInfoArray[i] = information.get(i);
        }
        return finalInfoArray;
    }

    private void updateQuestionBank(CommandSender player) {
        formatInformationForPlayer(NOTIFY_RELOADING, player);
        if (externalFiles.reloadQuestions()) {
            gameManager.updateQuestionList(externalFiles.getAllQuestions());
            formatInformationForPlayer(NOTIFY_RELOADED, player);
        } else {
            formatInformationForPlayer(ERROR_QUESTION_FILE_BROKEN, player);
        }
    }

    private void reloadConfigFile(CommandSender sender) {
        formatInformationForPlayer(NOTIFY_RELOADING, sender);
        gameManager.stopGame();
        if(holoQuiz.reloadHoloQuiz()) {
            formatInformationForPlayer(NOTIFY_RELOADED, sender);
        } else {
            formatInformationForPlayer(ERROR_CONFIG_FILE_BROKEN, sender);
        }
    }

    //Warning: Only for player-query purposes and not for database Maintenance.
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
        int size = externalFiles.getConfigFile().getLeaderboardSize();
        if (size > topPlayers.size()) {
            size = topPlayers.size();
        }
        String[] info = new String[size + 1];
        info[0] = String.format(headerFormat, size);
        for (int i = 0; i < size; i++) {
            PlayerData currentPlayerData = topPlayers.get(i);
            String playerName = currentPlayerData.getPlayerName();
            String bestTime = currentPlayerData.getBestTimeInSeconds3DP();
            String averageTime = currentPlayerData.getAverageTimeInSeconds3DP();
            int totalAnswers = currentPlayerData.getQuestionsAnswered();
            info[i + 1] = String.format(bodyFormat, i + 1, playerName, totalAnswers, bestTime, averageTime);
        }
        return info;
    }

    private void formatInformationForPlayer(String[] message, CommandSender sender) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            MessageFormatter.getSender().sendToPlayer(player, message, false, true, true);
        } else {
            MessageFormatter.getSender().sendToConsole(sender, message);
        }
    }

    private void formatInformationForPlayer(String message, CommandSender sender) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            MessageFormatter.getSender().sendToPlayer(player, message, false, true, true);
        } else {
            MessageFormatter.getSender().sendToConsole(sender, message);
        }
    }
}

//No switches?
//⠀⠀⠀⢘⣾⣾⣿⣾⣽⣯⣼⣿⣿⣴⣽⣿⣽⣭⣿⣿⣿⣿⣿⣧
//⠀⠀⠀⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
//⠀⠀⠠⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
//⠀⠀⣰⣯⣾⣿⣿⡼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿
//⠀⠀⠛⠛⠋⠁⣠⡼⡙⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠁
//⠀⠀⠀⠤⣶⣾⣿⣿⣿⣦⡈⠉⠉⠉⠙⠻⣿⣿⣿⣿⣿⠿⠁⠀
//⠀⠀⠀⠀⠈⠟⠻⢛⣿⣿⣿⣷⣶⣦⣄⠀⠸⣿⣿⣿⠗⠀⠀⠀
//⠀⠀⠀⠀⠀⣼⠀⠄⣿⡿⠋⣉⠈⠙⢿⣿⣦⣿⠏⡠⠂⠀⠀⠀
//⠀⠀⠀⠀⢰⡌⠀⢠⠏⠇⢸⡇⠐⠀⡄⣿⣿⣃⠈⠀⠀⠀⠀⠀
//⠀⠀⠀⠀⠈⣻⣿⢫⢻⡆⡀⠁⠀⢈⣾⣿⠏⠀⠀⠀⠀⠀⠀⠀
//⠀⠀⠀⠀⢀⣿⣻⣷⣾⣿⣿⣷⢾⣽⢭⣍⠀⠀⠀⠀⠀⠀⠀⠀
//⠀⠀⠀⠀⣼⣿⣿⣿⣿⡿⠈⣹⣾⣿⡞⠐⠁⠀⠀⠀⠁⠀⠀⠀
//⠀⠀⠀⠨⣟⣿⢟⣯⣶⣿⣆⣘⣿⡟⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀
//⠀⠀⠀⠀⠀⡆⠀⠐⠶⠮⡹⣸⡟⠁⠀⠀