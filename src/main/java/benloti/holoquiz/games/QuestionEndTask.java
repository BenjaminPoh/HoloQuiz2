package benloti.holoquiz.games;

import benloti.holoquiz.files.UserInterface;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class QuestionEndTask extends BukkitRunnable {
    public static final String ANSWER_REVEAL_ANNOUNCEMENT = "&6Times up! The answer is: &e%s&6.";

    private final JavaPlugin plugin;
    private final UserInterface userInterface;
    private final GameManager gameManager;

    public QuestionEndTask(JavaPlugin plugin, UserInterface userInterface, GameManager gameManager) {
        this.gameManager = gameManager;
        this.plugin = plugin;
        this.userInterface = userInterface;
    }

    @Override
    public void run() {
        if(gameManager.getQuestionStatus()) {
            gameManager.nextQuestion(0);
            return;
        }
        //Disable answering, send answer.
        gameManager.setQuestionStatus(true);
        String possibleAnswer = gameManager.getCurrentQuestion().getAnswers().get(0);
        String formattedAnswer = String.format(ANSWER_REVEAL_ANNOUNCEMENT, possibleAnswer);
        formattedAnswer = userInterface.attachLabel(formattedAnswer);
        formattedAnswer = userInterface.formatColours(formattedAnswer);
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            userInterface.attachSuffixAndSend(player, formattedAnswer);
        }

        //Next Question
        gameManager.nextQuestion(gameManager.getRevealAnswerDelay() * 20);
    }
}
