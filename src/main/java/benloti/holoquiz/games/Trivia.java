package benloti.holoquiz.games;

import benloti.holoquiz.database.UserPersonalisation;
import benloti.holoquiz.structs.PlayerSettings;
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
    private final UserPersonalisation userPersonalisation;

    public Trivia(List<Question> questionBank, JavaPlugin plugin, UserPersonalisation userPersonalisation) {
        this.allQuestions = questionBank;
        this.plugin = plugin;
        this.userPersonalisation = userPersonalisation;
    }

    @Override
    public void run() {
        int size = allQuestions.size();
        Random rand = new Random();
        int randomIndex = rand.nextInt(size);
        this.question = allQuestions.get(randomIndex);
        setQuestionAnswered(false);
        setTimeQuestionSent(System.currentTimeMillis());
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            sendQuestion(player);
        }
    }

    public void sendQuestion(Player player) {
        String playerUUID = player.getUniqueId().toString();
        PlayerSettings playerSettings = userPersonalisation.getPlayerSettings(playerUUID);
        if(playerSettings == null) {
            player.sendMessage(question.getQuestion());
            return;
        }
        if(playerSettings.isNotificationEnabled()) {
            String playerSuffix = playerSettings.getSuffix();
            player.sendMessage(question.getQuestion() + playerSuffix);
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


