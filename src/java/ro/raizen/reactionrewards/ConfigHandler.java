package ro.raizen.reactionrewards;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ConfigHandler {
    private ReactionRewards   plugin;
    private final String      fileName;
    private File              configFile;
    private FileConfiguration fileConfiguration;

    public ConfigHandler(ReactionRewards plugin, String fileName) {
        this.plugin   = plugin;
        this.fileName = fileName;

        File dataFolder = plugin.getDataFolder();

        if (dataFolder == null) {
            throw new IllegalStateException();
        }

        this.configFile = new File(plugin.getDataFolder(), fileName);
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource(fileName);

        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);

            fileConfiguration.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            this.reloadConfig();
        }

        return fileConfiguration;
    }

    public void saveConfig() {
        if ((fileConfiguration == null) || (configFile == null)) {
            return;
        } else {
            try {
                getConfig().save(configFile);
            } catch (IOException e) {
                plugin.log.severe("Could not save config to " + configFile);
            }
        }
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            this.plugin.saveResource(fileName, false);
        }
    }
}