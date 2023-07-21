package benloti.holoquiz2.handlers;

import benloti.holoquiz2.HoloQuiz2;
import benloti.holoquiz2.leaderboard.Leaderboard;
import benloti.holoquiz2.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerActivityHandler implements Listener {

    private final HoloQuiz2 plugin;
    private final Leaderboard leaderboard;
    private final DatabaseManager database;

    public PlayerActivityHandler(HoloQuiz2 plugin, DatabaseManager database, Leaderboard leaderboard) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        this.database = database;
        this.leaderboard = leaderboard;
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent theEvent) {
        Player newPlayer = theEvent.getPlayer();
        newPlayer.getName();
    }
}