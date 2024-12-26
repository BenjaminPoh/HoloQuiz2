package benloti.holoquiz.games;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class PeriodicChecker extends BukkitRunnable {

    private static final String LOG_MESSAGE = "[HoloQuiz] LOG: Question was delayed by %f secs. Is server TPS not doing well?";

    private final GameManager gameManager;

    public PeriodicChecker (GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        long timeNow = System.currentTimeMillis();
        long timeLimit = gameManager.getNextTaskTime();
        if(timeLimit < timeNow) {
            double delay = (timeNow - timeLimit) / 1000.0;
            gameManager.triggerNextTask(true);
            if(delay > 0.1) {
                Bukkit.getLogger().info(String.format(LOG_MESSAGE, delay));
            }
        }
    }
}
