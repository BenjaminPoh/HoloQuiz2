package benloti.holoquiz2.files;

import benloti.holoquiz2.HoloQuiz2;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class TimedTask extends BukkitRunnable {
    private String message = "This is a default message sent every 5s peko";
    private final List<String> allQuestions;
    private long interval = 5;
    //private int index = -1;
    private final JavaPlugin plugin;

    public TimedTask(JavaPlugin plugin) {
        this.plugin = plugin;
        File configFile = new File(plugin.getDataFolder(), "QuestionBank.yml");
        if(!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration questionFile = YamlConfiguration.loadConfiguration(configFile);
        allQuestions = questionFile.getStringList("messages");
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        int size = allQuestions.size();
        if(size == 0) {
            setMessage(message);
            Bukkit.broadcastMessage(message);
            return;
        }
        Random rand = new Random();
        int randomIndex = rand.nextInt(size);
        setMessage(allQuestions.get(randomIndex));
        Bukkit.broadcastMessage(message);
    }

    public void start() {
        this.runTaskTimer(plugin, 0, interval * 20);
    }
}


