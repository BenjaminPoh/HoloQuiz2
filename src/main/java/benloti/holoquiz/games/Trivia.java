package benloti.holoquiz.games;

import benloti.holoquiz.structs.Question;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class Trivia extends BukkitRunnable {
    private Question question;
    private final List<Question> allQuestions;
    private long timeQuestionSent;
    private boolean questionAnswered;
    private final JavaPlugin plugin;

    public Trivia(List<Question> questionBank, JavaPlugin plugin) {
        this.allQuestions = questionBank;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int size = allQuestions.size();
        Random rand = new Random();
        int randomIndex = rand.nextInt(size);
        Question question = allQuestions.get(randomIndex);
        this.question = question;
        setQuestionAnswered(false);
        setTimeQuestionSent(System.currentTimeMillis());
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(question.getQuestion());
        }
    }

    public Question getQuestion() {
        return this.question;
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
}


