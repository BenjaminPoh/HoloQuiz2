package benloti.holoquiz2;

import benloti.holoquiz2.archive.TestClass;
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

        getCommand("Peko").setExecutor(new TestClass());
        TimedTask testTask = new TimedTask(this);
        new QuizAnswerHandler(this, testTask, database);
        testTask.start();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("Shutting Down Peko");
    }
}
