package benloti.holoquiz2.commands;

import benloti.holoquiz2.data.PlayerData;
import net.md_5.bungee.api.ChatColor;
import benloti.holoquiz2.files.DatabaseManager;
import benloti.holoquiz2.files.TimedTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCmds implements CommandExecutor {
    private static final String TABLE_BORDER = "&9=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";
    private final TimedTask timedTask;
    private final DatabaseManager databaseManager;

    public PlayerCmds(TimedTask timedTask, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.timedTask = timedTask;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command theCommand, String alias, String [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = ((Player) sender).getPlayer();
        assert player != null;

        if (args.length == 0) {
            Bukkit.getLogger().info("args is null");
            player.sendMessage("HELP ME PLS");
            return false;
        }

        if (args[0].equals("help")) {
            player.sendMessage("HELP ME PLS");
            return true;
        }

        if (args[0].equals("info")) {
            displayQuestionInfo(player);
            return true;
        }

        if (args[0].equals("stats") && args.length == 1) {
            displayPlayerStats(player, player.getName());
            return true;
        }

        if (args[0].equals("stats") && args.length == 2) {
            displayPlayerStats(player, args[1]);
            return true;
        }

        if (args[0].equals("top")) {
            //show the very best
            return true;
        }

        if (args[0].equals("peko")) {
            player.sendMessage("Peko Peko Peko!!!");
            return true;
        }
        return true;
    }

    private void displayQuestionInfo(Player player) {
        String currentQuestion = timedTask.showQuestion().getQuestion();
        long currentTime = System.currentTimeMillis();
        //Simple calculations for later
        long timeQuestionSent = timedTask.getTimeQuestionSent();
        long questionInterval = timedTask.getInterval();
        int timeLeft = (int)(timeQuestionSent - currentTime + questionInterval * 1000);
        double timeLeftInSeconds = timeLeft / 1000.0;
        boolean questionStatus = timedTask.isQuestionAnswered();

        String currentQuestionFormatted = "&bQuestion: &6" + currentQuestion;
        String timeLeftFormatted = "&bNext Question is in &6" + timeLeftInSeconds + " seconds";
        String questionStatusFormatted;
        if(questionStatus) {
            questionStatusFormatted = "&bThe Question has been &aAnswered!";
        } else {
            questionStatusFormatted = "&bThe Question is &cNot Yet Answered!";
        }
        String[] information = {TABLE_BORDER, currentQuestionFormatted, timeLeftFormatted,
                questionStatusFormatted, TABLE_BORDER};

        int i = 0;
        for(String s: information) {
            information[i] = ChatColor.translateAlternateColorCodes('&', s);
            i += 1;
        }

        player.sendMessage(information);
    }

    private void displayPlayerStats(Player player, String playerName) {
        PlayerData playerData = databaseManager.loadPlayerData(playerName);
        if(playerData == null) {
            String errorMessage = ChatColor.translateAlternateColorCodes('&', "&cNo such player found!");
            player.sendMessage(errorMessage);
            return;
        }


        String playerStats = String.format(
                "&b|Answers: &6%s &b| Best Time: &6%ss &b| Average Time: &6%ss &b|", playerData.getQuestionsAnswered(),
                playerData.getBestTimeInSeconds3DP(),playerData.getAverageTimeInSeconds3DP());
        String playerNameBorder = String.format("&9=-=-=-=-=-=-=[ &bPlayer Stats for &a&l%s&9 ]=-=-=-=-=-=-=",
                playerData.getPlayerName());
        String[] informationFormatted = {ChatColor.translateAlternateColorCodes('&', playerNameBorder),
                ChatColor.translateAlternateColorCodes('&', playerStats)};

        player.sendMessage(informationFormatted);
    }

}
