package benloti.holoquiz2;

import benloti.holoquiz2.commands.TestClass;
import benloti.holoquiz2.data.TestPlayerData;
import benloti.holoquiz2.files.TimedTask;
import benloti.holoquiz2.handlers.PekoHandler;
import benloti.holoquiz2.handlers.TestHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class HoloQuiz2 extends JavaPlugin {

    private TestPlayerData testPlayerData;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("Hello World Peko");

        if(!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        this.testPlayerData = new TestPlayerData(this);

        getCommand("Peko").setExecutor(new TestClass());
        new TestHandler(this);
        new PekoHandler(this);
        TimedTask testTask = new TimedTask(this);
        testTask.start();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("Shutting Down Peko");
    }
}
