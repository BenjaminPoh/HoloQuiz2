package benloti.holoquiz2.files;

import benloti.holoquiz2.structs.Question;
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
    private final int interval;
    private final JavaPlugin plugin;
    private long timeQuestionSent;
    private boolean questionAnswered;
    private boolean acceptingAnswers; //Override

    public TimedTask(JavaPlugin plugin, ConfigFile configFile) {
        this.plugin = plugin;
        File questionBank = new File(plugin.getDataFolder(), "QuestionBank.yml");
        if(!questionBank.exists()) {
            try {
                questionBank.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileConfiguration questionFile = YamlConfiguration.loadConfiguration(questionBank);
        ConfigurationSection anotherFile = questionFile.getConfigurationSection("questions");
        allQuestions = Question.loadFromConfig(anotherFile);
        this.interval = configFile.getInterval();
        this.acceptingAnswers = true;
    }

    public Question showQuestion() {
        return this.question;
    }

    public long getInterval() {
        return this.interval;
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

    public boolean isStopped() {
        return !acceptingAnswers;
    }

    @Override
    public void run() {
        if(isStopped()) {
            return;
        }

        int size = allQuestions.size();
        Random rand = new Random();
        int randomIndex = rand.nextInt(size);
        Question question = allQuestions.get(randomIndex);
        this.question = question;
        setQuestionAnswered(false);
        setTimeQuestionSent(System.currentTimeMillis());
        Bukkit.broadcastMessage(question.getQuestion());
    }

    public void firstStart() {
        this.runTaskTimer(plugin, 0, interval * 20);
    }

    public void start() {
        this.acceptingAnswers = true;
        nextQuestion();
    }

    public void stop() {
        this.acceptingAnswers = false;
    }

    public void nextQuestion() {
        this.run();
    }
}


