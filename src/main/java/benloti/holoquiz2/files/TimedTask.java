package benloti.holoquiz2.files;

import benloti.holoquiz2.data.Question;
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
    private long interval = 10;
    private final JavaPlugin plugin;
    private long timeQuestionSent;
    private boolean questionAnswered;

    public TimedTask(JavaPlugin plugin) {
        Bukkit.getLogger().info("This should be seen peko");
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
        if(anotherFile == null) {
            Bukkit.getLogger().info("Your .yml formatting is wrong peko");
            //Yeah how do i terminate lmao
        }
        allQuestions = Question.loadFromConfig(anotherFile);
    }

    public Question showQuestion() {
        return this.question;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    private void setTimeQuestionSent(long time) {
        this.timeQuestionSent = time;
    }

    public long getTimeQuestionSent() {
        return this.timeQuestionSent;
    }

    public void setQuestionAnswered(boolean questionAnswered) {
        this.questionAnswered = questionAnswered;
    }

    public boolean isQuestionAnswered() {
        return questionAnswered;
    }

    @Override
    public void run() {
        int size = allQuestions.size();
        /*
        if(size == 0) {
            question = new Question("There is no question. Peko is the answer" , "peko");
            Bukkit.broadcastMessage(question.getQuestion());
        }
        */
        Random rand = new Random();
        int randomIndex = rand.nextInt(size);
        Question question = allQuestions.get(randomIndex);
        this.question = question;
        setQuestionAnswered(false);
        setTimeQuestionSent(System.currentTimeMillis());
        Bukkit.broadcastMessage(question.getQuestion());
    }

    public void start() {
        this.runTaskTimer(plugin, 0, interval * 20);
    }
}


