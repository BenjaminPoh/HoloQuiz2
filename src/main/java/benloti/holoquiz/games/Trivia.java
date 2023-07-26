package benloti.holoquiz.games;

import benloti.holoquiz.files.UserInterface;
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
    private final UserInterface userInterface;

    public Trivia(List<Question> questionBank, JavaPlugin plugin, UserInterface userInterface) {
        this.allQuestions = questionBank;
        this.plugin = plugin;
        this.userInterface = userInterface;
    }

    @Override
    public void run() {
        int size = allQuestions.size();
        Random rand = new Random();
        int randomIndex = rand.nextInt(size);
        this.question = allQuestions.get(randomIndex);
        String formattedQuestion = userInterface.attachLabel(question.getQuestion());
        formattedQuestion = userInterface.formatColours(formattedQuestion);
        setQuestionAnswered(false);
        setTimeQuestionSent(System.currentTimeMillis());
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            userInterface.sendMessageToPlayer(player, formattedQuestion);
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


