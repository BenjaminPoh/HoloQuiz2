package benloti.holoquiz2;

import benloti.holoquiz2.commands.PlayerCmds;
import benloti.holoquiz2.files.DatabaseManager;
import benloti.holoquiz2.files.TimedTask;
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

        DatabaseManager database = new DatabaseManager(this);
        TimedTask triviaTask = new TimedTask(this);
        new QuizAnswerHandler(this, triviaTask, database);
        getCommand("HoloQuiz").setExecutor(new PlayerCmds(triviaTask, database));

        triviaTask.start();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("Shutting Down Peko");
    }
}
