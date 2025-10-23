package benloti.holoquiz.files;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {
    private static final String ERROR_MISSING_CONFIG = "Missing Key %s for Path %s in File %s!" ;
    private static final String ERROR_INVALID_MATERIAL = "Failed to load item with the name - %s";

    private String currentFile;

    public ConfigLoader() {
        this.currentFile = "";
    }

    public void setCurrentFile(String name) {
        this.currentFile = name;
    }

    public int getInt(FileConfiguration configs, String key, int defaultValue) {
        if(!configs.contains(key)) {
            handleMissingKey(configs.getCurrentPath(), key);
            return defaultValue;
        }
        return configs.getInt(key);
    }

    public String getString(FileConfiguration configs, String key, String defaultValue) {
        if(!configs.contains(key)) {
            handleMissingKey(configs.getCurrentPath(), key);
            return defaultValue;
        }
        return configs.getString(key);
    }

    public boolean getBoolean(FileConfiguration configs, String key, boolean defaultValue) {
        if(!configs.contains(key)) {
            handleMissingKey(configs.getCurrentPath(), key);
            return defaultValue;
        }
        return configs.getBoolean(key);
    }

    public int getInt(ConfigurationSection configs, String key, int defaultValue) {
        if(!configs.contains(key)) {
            handleMissingKey(configs.getCurrentPath(), key);
            return defaultValue;
        }
        return configs.getInt(key);
    }

    public String getString(ConfigurationSection configs, String key, String defaultValue) {
        if(!configs.contains(key)) {
            handleMissingKey(configs.getCurrentPath(), key);
            return defaultValue;
        }
        return configs.getString(key);
    }

    public boolean getBoolean(ConfigurationSection configs, String key, boolean defaultValue) {
        if(!configs.contains(key)) {
            handleMissingKey(configs.getCurrentPath(), key);
            return defaultValue;
        }
        return configs.getBoolean(key);
    }

    public long getLong(ConfigurationSection configs, String key, long defaultValue) {
        if(!configs.contains(key)) {
            handleMissingKey(configs.getCurrentPath(), key);
            return defaultValue;
        }
        return configs.getLong(key);
    }

    public double getDouble(ConfigurationSection configs, String key, double defaultValue) {
        if(!configs.contains(key)) {
            handleMissingKey(configs.getCurrentPath(), key);
            return defaultValue;
        }
        return configs.getDouble(key);
    }

    public List<String> getStringList(ConfigurationSection configs, String key) {
        if(!configs.contains(key)) {
            handleMissingKey(configs.getCurrentPath(), key);
            return new ArrayList<>();
        }
        return configs.getStringList(key);
    }

    public ConfigurationSection getSection(ConfigurationSection config, String key) {
        ConfigurationSection section = config.getConfigurationSection(key);
        if(section == null) {
            handleMissingKey(config.getCurrentPath(), key);
        }
        return section;
    }

    public Material getMaterial(ConfigurationSection section, String key, String defaultValue) {
        String itemType = section.getString(key, defaultValue);
        Material itemMaterial = Material.matchMaterial(itemType);
        if (itemMaterial == null) {
            itemMaterial = Material.CARROT;
            Logger.getLogger().error(String.format(ERROR_INVALID_MATERIAL,itemType));
        }
        return itemMaterial;
    }

    public double getDoubleOptional(ConfigurationSection configs, String key, double defaultValue) {
        return configs.getDouble(key, defaultValue);
    }

    public List<String> getStringListOptional(ConfigurationSection configs, String key) {
        return configs.getStringList(key); //Returns an empty list if key is not found
    }

    public String getStringOptional(ConfigurationSection configs, String key, String defaultValue) {
        return configs.getString(key, defaultValue);
    }


    private void handleMissingKey(String path, String key) {
        String logMessage = String.format(ERROR_MISSING_CONFIG, key, path, currentFile);
        Logger.getLogger().error(logMessage);
    }
}
