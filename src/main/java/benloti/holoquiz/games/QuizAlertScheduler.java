package benloti.holoquiz.games;
import benloti.holoquiz.files.Logger;
import benloti.holoquiz.files.MessageFormatter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.scheduler.BukkitRunnable;

public class QuizAlertScheduler extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final GameManager gameManager;

    private boolean isScheduled;

    public QuizAlertScheduler(JavaPlugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.isScheduled = false;
    }

    /* Dev Notes
     * Should only be triggered on 3 cases
     * 1) When Answers are not revealed, it is sent X seconds before the next question if X < Interval
     * 2) When Answers are revealed AND not answered, it is sent X seconds before the next question if X < RevealAnswer
     * 3) When Answers are revealed AND answered, it is sent X seconds before the next question if X < timeToNextTask
     */
    public void scheduleAlert(long ticks) {
        if (ticks <= 0) {
            return;
        }
        Logger.getLogger().debug("Alert Scheduled for " + ticks + " ticks");
        this.runTaskLater(plugin, ticks);
        this.isScheduled = true;
    }

    public void cancelExecution() {
        if(!this.isScheduled) {
            return;
        }
        this.cancel();
        this.isScheduled = false;
    }

    @Override
    public void run() {
        if(!gameManager.isQuestionAnswered() && gameManager.isRevealAnswerFlag() && !gameManager.isQuestionTimedOut()) {
            return;
        }
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            MessageFormatter.getSender().sendAlertToPlayers(player, gameManager.getAlertMessage());
        }
    }

}


