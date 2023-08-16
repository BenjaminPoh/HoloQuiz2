package benloti.holoquiz.games;

import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.structs.Question;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class Trivia extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final UserInterface userInterface;

    private final Question question;
    private long timeQuestionSent;
    private boolean questionAnswered;

    public Trivia(Question question, JavaPlugin plugin, UserInterface userInterface) {
        this.question = question;
        this.plugin = plugin;
        this.userInterface = userInterface;
    }

    @Override
    public void run() {
        Bukkit.getLogger().info("Question Sent: " + question.getQuestion());
        String formattedQuestion = userInterface.attachLabel(question.getQuestion());
        formattedQuestion = userInterface.formatColours(formattedQuestion);
        setQuestionAnswered(false);
        setTimeQuestionSent(System.currentTimeMillis());
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            userInterface.attachSuffixAndSend(player, formattedQuestion);
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


