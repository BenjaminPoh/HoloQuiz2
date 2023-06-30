package benloti.holoquiz2;

import benloti.holoquiz2.commands.TestClass;
import benloti.holoquiz2.handlers.PekoHandler;
import benloti.holoquiz2.handlers.TestHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class HoloQuiz2 extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("Hello World v2");

        getCommand("Peko").setExecutor(new TestClass());

        new TestHandler(this);
        new PekoHandler(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("bye");
    }
}
