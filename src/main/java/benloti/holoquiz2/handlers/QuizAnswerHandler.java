package benloti.holoquiz2.handlers;

import benloti.holoquiz2.HoloQuiz2;
import benloti.holoquiz2.files.TimedTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.Time;

public class QuizAnswerHandler implements Listener {

    private final TimedTask task;

    public QuizAnswerHandler(HoloQuiz2 plugin, TimedTask task) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.task = task;
    }

    @EventHandler
    public void correctAnswerSent(AsyncPlayerChatEvent theEvent) {
        String message = theEvent.getMessage();
        String playerName = theEvent.getPlayer().getName();
        if (message.equals(task.showQuestion().getAnswers())) {
            String announcement = playerName + " has gotten the answer!";
            Bukkit.broadcastMessage(announcement);
        }
    }
}
