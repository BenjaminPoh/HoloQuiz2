package benloti.holoquiz2.handlers;

import benloti.holoquiz2.HoloQuiz2;
import benloti.holoquiz2.files.DatabaseManager;
import benloti.holoquiz2.files.TimedTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class QuizAnswerHandler implements Listener {

    private final TimedTask task;
    private final HoloQuiz2 plugin;
    private final DatabaseManager database;

    public QuizAnswerHandler(HoloQuiz2 plugin, TimedTask task, DatabaseManager database) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.task = task;
        this.plugin = plugin;
        this.database = database;
    }

    @EventHandler
    public void correctAnswerSent(AsyncPlayerChatEvent theEvent) {
        if(task.isStopped()) {
            return;
        }
        String message = theEvent.getMessage();
        Player player = theEvent.getPlayer();
        String playerName = player.getName();
        List<String> answers = task.showQuestion().getAnswers();
        for(String possibleAnswer : answers) {
            if (message.equalsIgnoreCase(possibleAnswer) && !task.isQuestionAnswered()) {
                //Time sensitive tasks
                long timeAnswered = System.currentTimeMillis();
                task.setQuestionAnswered(true);

                //Simple calculations for later
                long startTime = task.getTimeQuestionSent();
                int timeTaken = (int)(timeAnswered - startTime);

                //The actual tasks
                sendAnnouncement(possibleAnswer, playerName, timeTaken);
                //displayActionBar(player); //Not what I want, but the bug is now a feature
                //giveReward(player, timeAnswered);
                displayTitle(player);
                new BukkitRunnable() {
                    public void run() {
                        makeFireworks(player);
                    }
                }.runTask(plugin);

                //Update database
                int playerHoloQuizID = database.obtainPlayerID(player.getUniqueId().toString(), player.getName());
                database.updateLogsRecord(playerHoloQuizID, timeAnswered, timeTaken);
                database.updateStatsRecord(playerHoloQuizID, timeTaken);
            }
        }
    }

    private void sendAnnouncement(String possibleAnswer, String playerName, long timeTaken) {
        double timeTakenInSeconds = timeTaken / 1000.0;
        String message = "&6" + playerName + "&e wins after &6" + timeTakenInSeconds +
                "&e seconds! The answer was &6" + possibleAnswer;
        String announcement = ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.broadcastMessage(announcement);
    }

    private void makeFireworks(Player player) {
        Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        FireworkEffect.Builder builder = FireworkEffect.builder();

        builder.flicker(true).withColor(Color.AQUA);
        builder.withFade(Color.YELLOW);
        builder.trail(true);
        builder.with(FireworkEffect.Type.BURST);

        fireworkMeta.addEffect(builder.build());
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

    private void giveReward(Player player, long timeTaken) {
        String playerName = player.getName();
        ItemStack item;
        if(timeTaken <= 2000) {
            item = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
        } else if (timeTaken <= 5000) {
            item = new ItemStack(Material.GOLDEN_APPLE, 1);
        } else {
            item = new ItemStack(Material.DIAMOND, 1);
        }

        ItemMeta meta = item.getItemMeta();
        List<String> loreList = new ArrayList<>();
        if(timeTaken <= 1000) {
            loreList.add("&bA carrot from Pekoland, for &a" + playerName);
            loreList.add("&bAH HA HA HA HA");
        } else if (timeTaken <= 2000) {
            loreList.add("&5Special Prize");
            loreList.add("&5for &a[player]");
            loreList.add("");
            loreList.add("&bSpeed is your middle name");
        } else if (timeTaken <= 5000) {
            loreList.add("9Holoquiz Reward");
            loreList.add("&9For &a" + playerName);
         } else {
            loreList.add("&bHoloquiz Reward");
            loreList.add("&bFor &a" + playerName);
        }
        List<String> coloredLoreList = new ArrayList<>();
        for (String s : loreList) {
            coloredLoreList.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        assert meta != null;


        meta.setLore(coloredLoreList);

        item.setItemMeta(meta);
        Inventory inv = player.getInventory();
        inv.addItem(item);
    }

}
