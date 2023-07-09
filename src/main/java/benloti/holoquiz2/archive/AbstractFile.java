//Shamelessly robbed from ReadySetPawn

package benloti.holoquiz2.archive;

import benloti.holoquiz2.HoloQuiz2;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class AbstractFile {

    protected HoloQuiz2 main;
    private File file;
    protected FileConfiguration config;

    public AbstractFile(HoloQuiz2 main, String fileName) {
        this.main = main;
        this.file = new File(main.getDataFolder(), fileName);
        if(!file.exists()) {
            try {
               file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        Bukkit.getLogger().info("File Loaded"); //To specify what is
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
