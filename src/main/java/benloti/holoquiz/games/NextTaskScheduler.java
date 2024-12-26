package benloti.holoquiz.games;

import org.bukkit.scheduler.BukkitRunnable;

public class NextTaskScheduler extends BukkitRunnable {
    private final GameManager gameManager;

    public NextTaskScheduler( GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        gameManager.triggerNextTask(false);
    }

}


