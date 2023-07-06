package benloti.holoquiz2.handlers;

import benloti.holoquiz2.HoloQuiz2;
import benloti.holoquiz2.files.TimedTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.awt.*;
import java.sql.Time;
import java.util.List;

public class QuizAnswerHandler implements Listener {

    private final TimedTask task;

    public QuizAnswerHandler(HoloQuiz2 plugin, TimedTask task) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.task = task;
    }

    @EventHandler
    public void correctAnswerSent(AsyncPlayerChatEvent theEvent) {
        String message = theEvent.getMessage();
        Player player = theEvent.getPlayer();
        String playerName = player.getName();
        List<String> answers = task.showQuestion().getAnswers();
        for(String possibleAnswer : answers) {
            if (message.equalsIgnoreCase(possibleAnswer)) {
                sendAnnouncement(possibleAnswer, playerName);
                //makeFireworks(player); //Broken, how
                //displayActionBar(player); //Not what I want, but the bug is now a feature
                displayTitle(player);
            }
        }
    }

    private void sendAnnouncement(String possibleAnswer, String playerName) {
        String message = "&6" + playerName + "&e wins! The answer was &6" + possibleAnswer;
        String announcement = ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.broadcastMessage(announcement);
    }

    private void makeFireworks(Player player) {
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        FireworkEffect.Builder builder = FireworkEffect.builder();

        fireworkMeta.addEffect(builder.flicker(true).withColor(Color.BLUE).build());
        fireworkMeta.addEffect(builder.withFade(Color.YELLOW).build());
        fireworkMeta.addEffect(builder.trail(true).build());
        fireworkMeta.addEffect(builder.with(FireworkEffect.Type.BURST).build());
        fireworkMeta.setPower(1);
        firework.setFireworkMeta(fireworkMeta);
    }

    private void displayActionBar(Player player) {
        String message = "&2Congratulations!\n&3You have answered correctly!";
        String announcement = ChatColor.translateAlternateColorCodes('&', message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(announcement));
    }

    private void displayTitle(Player player) {
        String message1 = "&2Congratulations!";
        String message2 = "&3You have answered correctly!";
        String announcement1 = ChatColor.translateAlternateColorCodes('&', message1);
        String announcement2 = ChatColor.translateAlternateColorCodes('&', message2);
        player.sendTitle(announcement1, announcement2, 10, 60, 10);
    }

}
