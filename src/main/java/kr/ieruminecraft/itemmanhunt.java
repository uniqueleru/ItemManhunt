package kr.ieruminecraft;

import kr.ieruminecraft.commands.ItemManhuntCommand;
import kr.ieruminecraft.config.ConfigManager;
import kr.ieruminecraft.game.GameManager;
import kr.ieruminecraft.gui.ItemConfigGUI;
import kr.ieruminecraft.listeners.GameListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class itemmanhunt extends JavaPlugin {

    private static itemmanhunt instance;
    private ConfigManager configManager;
    private GameManager gameManager;
    private ItemConfigGUI itemConfigGUI;

    @Override
    public void onEnable() {
        instance = this;

        // 설정 파일 초기화
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // 게임 매니저 초기화
        this.gameManager = new GameManager(this);
        
        // GUI 매니저 초기화
        this.itemConfigGUI = new ItemConfigGUI(this);
        
        // 명령어 등록
        getCommand("imh").setExecutor(new ItemManhuntCommand(this));
        
        // 이벤트 리스너 등록
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        
        getLogger().info("Item Manhunt 플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        if (gameManager != null && gameManager.isGameRunning()) {
            gameManager.stopGame();
        }
        
        getLogger().info("Item Manhunt 플러그인이 비활성화되었습니다.");
    }
    
    public static itemmanhunt getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public ItemConfigGUI getItemConfigGUI() {
        return itemConfigGUI;
    }
}