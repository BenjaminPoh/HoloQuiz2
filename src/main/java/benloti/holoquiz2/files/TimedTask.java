package benloti.holoquiz2.files;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TimedTask extends BukkitRunnable {
    private String message = "This is sent every 5s peko";
    private long interval = 5;
    //private int index = -1;
    private final JavaPlugin plugin;

    public TimedTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public void start() {
        this.runTaskTimer(plugin, 0, interval * 20);
    }
}


