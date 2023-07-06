package benloti.holoquiz2.files;

import benloti.holoquiz2.data.Question;
import benloti.holoquiz2.handlers.QuizAnswerHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class TimedTask extends BukkitRunnable {
    Question question;
    private final List<Question> allQuestions;
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
        ConfigurationSection anotherFile = questionFile.getConfigurationSection("questions");
        assert anotherFile != null;
        allQuestions = Question.loadFromConfig(anotherFile);
    }

    public Question showQuestion() {
        return this.question;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        int size = allQuestions.size();
        if(size == 0) {
            question = new Question("There is no question. Peko is the answer" , "peko");
            Bukkit.broadcastMessage(question.getQuestion());
        }
        Random rand = new Random();
        int randomIndex = rand.nextInt(size);
        Question question = allQuestions.get(randomIndex);
        Bukkit.broadcastMessage(question.getQuestion());

    }

    public void start() {
        this.runTaskTimer(plugin, 0, interval * 20);
    }
}


