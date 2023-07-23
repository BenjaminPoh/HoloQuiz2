package benloti.holoquiz2;

import benloti.holoquiz2.commands.PlayerCmds;
import benloti.holoquiz2.files.ConfigFile;
import benloti.holoquiz2.leaderboard.Leaderboard;
import benloti.holoquiz2.database.DatabaseManager;
import benloti.holoquiz2.files.TimedTask;
import benloti.holoquiz2.handlers.PlayerActivityHandler;
import benloti.holoquiz2.handlers.QuizAnswerHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class HoloQuiz2 extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("Hello World Peko");

        if(!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        ConfigFile configFile = new ConfigFile(this);
        DatabaseManager database = new DatabaseManager(this);
        TimedTask triviaTask = new TimedTask(this, configFile);
        Leaderboard leaderboard = new Leaderboard(configFile, database);
        new QuizAnswerHandler(this, triviaTask, database, leaderboard);
        new PlayerActivityHandler(this, database, leaderboard);
        getCommand("HoloQuiz").setExecutor(new PlayerCmds(triviaTask, database, leaderboard, configFile));
        triviaTask.firstStart();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("Shutting Down Peko");
    }
}
