package benloti.holoquiz;

import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.commands.PlayerCmds;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.leaderboard.Leaderboard;
import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.games.QuizAnswerHandler;
import org.bukkit.Bukkit;

import org.bukkit.plugin.java.JavaPlugin;

public final class HoloQuiz extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("Hello World Peko");

        if(!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        ConfigFile configFile = new ConfigFile(this);
        DependencyHandler dependencyHandler = new DependencyHandler(this);
        DatabaseManager database = new DatabaseManager(this);
        GameManager gameManager = new GameManager(this, configFile);
        Leaderboard leaderboard = new Leaderboard(configFile, database);
        new QuizAnswerHandler(this, gameManager, database, leaderboard, dependencyHandler);
        getCommand("HoloQuiz").setExecutor(new PlayerCmds(gameManager, database, leaderboard, configFile));
        gameManager.startGame();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("Shutting Down Peko");
    }
}
