package kr.ieruminecraft.config;

import kr.ieruminecraft.itemmanhunt;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {

    private final itemmanhunt plugin;
    private FileConfiguration config;
    private FileConfiguration langConfig;
    private File configFile;
    private File langFile;

    public ConfigManager(itemmanhunt plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.langFile = new File(plugin.getDataFolder(), "lang.yml");
    }

    public void loadConfig() {
        try {
            createCustomConfig();
            this.config = YamlConfiguration.loadConfiguration(configFile);
            this.langConfig = YamlConfiguration.loadConfiguration(langFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "설정 파일을 로드하는 중 오류가 발생했습니다.", e);
        }
    }

    private void createCustomConfig() throws IOException {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource("config.yml", false);
        }
        
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            plugin.saveResource("lang.yml", false);
        }
    }
    
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "설정 파일을 저장하는 중 오류가 발생했습니다.", e);
        }
    }
    
    public void reloadConfig() {
        loadConfig();
    }
    
    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }
    
    public FileConfiguration getLangConfig() {
        if (langConfig == null) {
            loadConfig();
        }
        return langConfig;
    }
    
    public String getMessage(String path) {
        String message = langConfig.getString("messages." + path);
        if (message == null) {
            return "메시지를 찾을 수 없습니다: " + path;
        }
        
        String prefix = langConfig.getString("prefix", "&8[&cItem Manhunt&8] &7");
        return colorize(prefix + message);
    }
    
    public List<String> getMessageList(String path) {
        List<String> messages = langConfig.getStringList("messages." + path);
        List<String> coloredMessages = new ArrayList<>();
        
        for (String message : messages) {
            coloredMessages.add(colorize(message));
        }
        
        return coloredMessages;
    }
    
    public String getTitle(String path) {
        return colorize(langConfig.getString("titles." + path + ".title", ""));
    }
    
    public String getSubtitle(String path) {
        return colorize(langConfig.getString("titles." + path + ".subtitle", ""));
    }
    
    public String getActionBar(String path) {
        return colorize(langConfig.getString("action-bars." + path, ""));
    }
    
    public String colorize(String message) {
        return message.replace("&", "§");
    }
    
    public int getHeadStartTime() {
        return config.getInt("head-start-time", 60);
    }
    
    public int getCompassUpdateInterval() {
        return config.getInt("compass-update-interval", 20);
    }
    
    public String getSound(String path) {
        return config.getString("sounds." + path, "");
    }
    
    public List<String> getEnabledItems() {
        return config.getStringList("enabled-items");
    }
    
    public void setEnabledItems(List<String> items) {
        config.set("enabled-items", items);
        saveConfig();
    }
    
    public boolean isItemEnabled(Material material) {
        List<String> enabledItems = getEnabledItems();
        return enabledItems.contains(material.name());
    }
    
    public void toggleItemEnabled(Material material) {
        List<String> enabledItems = getEnabledItems();
        
        if (enabledItems.contains(material.name())) {
            enabledItems.remove(material.name());
        } else {
            enabledItems.add(material.name());
        }
        
        setEnabledItems(enabledItems);
    }
}