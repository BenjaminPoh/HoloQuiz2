package benloti.holoquiz.games;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class PeriodicChecker extends BukkitRunnable {

    private final GameManager gameManager;

    public PeriodicChecker (GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        long timeNow = System.currentTimeMillis();
        long startTime = gameManager.getTimeQuestionSent();
        int timePassed = (int)(timeNow - startTime);
        if(timePassed > gameManager.getInterval() * 1000) {
            Bukkit.getLogger().info("[HoloQuiz] IntervalCheck triggered! Is server TPS not doing well?");
            gameManager.nextQuestion(0);
        }
    }
}
