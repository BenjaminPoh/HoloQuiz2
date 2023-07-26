package benloti.holoquiz;

import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.commands.PlayerCmds;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.leaderboard.Leaderboard;
import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.games.QuizAnswerHandler;
import org.bukkit.Bukkit;

import org.bukkit.plugin.java.JavaPlugin;

public final class HoloQuiz extends JavaPlugin {

    private ConfigFile configFile;
    private DependencyHandler dependencyHandler;
    private DatabaseManager database;
    private GameManager gameManager;
    private Leaderboard leaderboard;
    private UserInterface userInterface;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if(!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        this.configFile = new ConfigFile(this);
        this.dependencyHandler = new DependencyHandler(this);
        this.database = new DatabaseManager(this);
        this.userInterface = new UserInterface(dependencyHandler.getCMIDep(), database.getUserPersonalisation());
        this.gameManager = new GameManager(this, configFile, userInterface);
        this.leaderboard = new Leaderboard(configFile, database);
        new QuizAnswerHandler(this, gameManager, database, leaderboard, dependencyHandler, userInterface, configFile);
        getCommand("HoloQuiz").setExecutor(new PlayerCmds(gameManager, database, leaderboard, configFile));
        gameManager.startGame();
    }

    @Override
    public void onDisable() {
        database.getUserPersonalisation().savePlayerSettings();
        Bukkit.getLogger().info("Shutting Down Peko");
    }
}
