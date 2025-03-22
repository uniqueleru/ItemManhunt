package kr.ieruminecraft.game;

import kr.ieruminecraft.itemmanhunt;
import kr.ieruminecraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import java.time.Duration;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GameManager {

    private final itemmanhunt plugin;
    private boolean gameRunning = false;
    private UUID runnerUUID;
    private Material targetItem;
    private BukkitTask compassTask;
    private BukkitTask headStartTask;
    private boolean chaseStarted = false;

    public GameManager(itemmanhunt plugin) {
        this.plugin = plugin;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }
    
    public boolean isChaseStarted() {
        return chaseStarted;
    }
    
    public Player getRunner() {
        if (runnerUUID == null) return null;
        return Bukkit.getPlayer(runnerUUID);
    }
    
    public void setRunner(Player runner) {
        if (runner == null) {
            this.runnerUUID = null;
        } else {
            this.runnerUUID = runner.getUniqueId();
        }
    }
    
    public Material getTargetItem() {
        return targetItem;
    }
    
    public boolean isRunner(Player player) {
        if (runnerUUID == null || player == null) return false;
        return player.getUniqueId().equals(runnerUUID);
    }
    
    public boolean isHunter(Player player) {
        return gameRunning && !isRunner(player);
    }
    
    public void startGame() {
        if (gameRunning) {
            return;
        }
        
        if (runnerUUID == null) {
            MessageUtils.broadcastMessage(plugin.getConfigManager().getMessage("runner-not-set"));
            return;
        }
        
        Player runner = Bukkit.getPlayer(runnerUUID);
        if (runner == null || !runner.isOnline()) {
            MessageUtils.broadcastMessage(plugin.getConfigManager().getMessage("no-player"));
            return;
        }
        
        if (Bukkit.getOnlinePlayers().size() < 2) {
            MessageUtils.broadcastMessage(plugin.getConfigManager().getMessage("not-enough-players"));
            return;
        }
        
        List<String> enabledItems = plugin.getConfigManager().getEnabledItems();
        if (enabledItems.isEmpty()) {
            MessageUtils.broadcastMessage(plugin.getConfigManager().getMessage("no-target-items"));
            return;
        }
        
        // 목표 아이템 선택
        String randomItemName = enabledItems.get(new Random().nextInt(enabledItems.size()));
        targetItem = Material.valueOf(randomItemName);
        
        // 게임 시작
        gameRunning = true;
        chaseStarted = false;
        
        // 모든 플레이어 초기화
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setLevel(0);
            player.setExp(0);
        }
        
        // 게임 시작 타이틀 표시
        String title = plugin.getConfigManager().getTitle("game-start");
        String subtitle = plugin.getConfigManager().getSubtitle("game-start")
                .replace("{item}", formatItemName(targetItem.name()));
        
        Title gameTitle = Title.title(
            Component.text(title),
            Component.text(subtitle),
            Title.Times.times(Duration.ofMillis(10 * 50), Duration.ofMillis(70 * 50), Duration.ofMillis(20 * 50))
        );
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(gameTitle);
        }
        
        // 게임 시작 소리 효과
        String soundName = plugin.getConfigManager().getSound("game-start");
        if (!soundName.isEmpty()) {
            try {
                // Use the registry to get the sound
                for (Player player : Bukkit.getOnlinePlayers()) {
                    var sound = Registry.SOUND_EVENT.get(NamespacedKey.minecraft(soundName.toLowerCase()));
                    if (sound != null) {
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    }
                }
            } catch (Exception e) {
                // Fall back to the Adventure API if the sound doesn't exist
                try {
                    net.kyori.adventure.sound.Sound sound = net.kyori.adventure.sound.Sound.sound(
                        Key.key("minecraft:" + soundName.toLowerCase()),
                        net.kyori.adventure.sound.Sound.Source.MASTER,
                        1.0f,
                        1.0f
                    );
                    
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(sound);
                    }
                } catch (Exception ex) {
                    plugin.getLogger().warning("잘못된 사운드 이름: " + soundName);
                }
            }
        }
        
        // 도망자에게 시간 주기
        int headStartTime = plugin.getConfigManager().getHeadStartTime();
        
        // 추격자들 움직임 제한
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isRunner(player)) {
                player.setWalkSpeed(0);
                player.setFlySpeed(0);
            }
        }
        
        // 도망자 시간 카운트다운
        headStartTask = new BukkitRunnable() {
            int timeLeft = headStartTime;
            
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    startChase();
                    this.cancel();
                    return;
                }
                
                // 카운트다운 타이틀 표시
                String headStartTitle = plugin.getConfigManager().getTitle("head-start");
                String headStartSubtitle = plugin.getConfigManager().getSubtitle("head-start")
                        .replace("{time}", String.valueOf(timeLeft));
                
                Title countdownTitle = Title.title(
                    Component.text(headStartTitle),
                    Component.text(headStartSubtitle),
                    Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(21 * 50), Duration.ofMillis(0))
                );
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.showTitle(countdownTitle);
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void startChase() {
        chaseStarted = true;
        
        // 추격자들 움직임 허용
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isRunner(player)) {
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
                
                // 추적 나침반 지급
                ItemStack compass = new ItemStack(Material.COMPASS);
                CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
                compassMeta.displayName(Component.text("§c도망자 추적기"));
                compass.setItemMeta(compassMeta);
                
                player.getInventory().addItem(compass);
            }
        }
        
        // 추격 시작 타이틀 표시
        String chaseTitle = plugin.getConfigManager().getTitle("chase-start");
        String chaseSubtitle = plugin.getConfigManager().getSubtitle("chase-start");
        
        Title chaseStartTitle = Title.title(
            Component.text(chaseTitle),
            Component.text(chaseSubtitle),
            Title.Times.times(Duration.ofMillis(10 * 50), Duration.ofMillis(70 * 50), Duration.ofMillis(20 * 50))
        );
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(chaseStartTitle);
        }
        
        // 나침반 업데이트 태스크 시작
        int updateInterval = plugin.getConfigManager().getCompassUpdateInterval();
        compassTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameRunning || !chaseStarted) {
                    this.cancel();
                    return;
                }
                
                Player runner = getRunner();
                if (runner == null || !runner.isOnline()) {
                    this.cancel();
                    return;
                }
                
                // 모든 추격자 나침반 업데이트
                for (Player hunter : Bukkit.getOnlinePlayers()) {
                    if (isHunter(hunter) && hunter.getInventory().contains(Material.COMPASS)) {
                        // 나침반을 도망자 위치로 설정
                        for (ItemStack item : hunter.getInventory().getContents()) {
                            if (item != null && item.getType() == Material.COMPASS) {
                                CompassMeta meta = (CompassMeta) item.getItemMeta();
                                meta.setLodestone(runner.getLocation());
                                meta.setLodestoneTracked(false);
                                item.setItemMeta(meta);
                            }
                        }
                        
                        // 액션바에 거리 표시
                        int distance = (int) hunter.getLocation().distance(runner.getLocation());
                        String actionBar = plugin.getConfigManager().getActionBar("distance")
                                .replace("{distance}", String.valueOf(distance));
                        
                        hunter.sendActionBar(Component.text(actionBar));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, updateInterval);
    }
    
    public void stopGame() {
        if (!gameRunning) {
            return;
        }
        
        // 태스크 취소
        if (compassTask != null) {
            compassTask.cancel();
            compassTask = null;
        }
        
        if (headStartTask != null) {
            headStartTask.cancel();
            headStartTask = null;
        }
        
        // 게임 상태 초기화
        gameRunning = false;
        chaseStarted = false;
        targetItem = null;
        
        // 플레이어 상태 복구
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
        }
    }
    
    public void checkWinCondition(Player player, ItemStack item) {
        if (!gameRunning || !chaseStarted || targetItem == null) {
            return;
        }
        
        // 도망자가 목표 아이템을 획득했는지 확인
        if (isRunner(player) && item.getType() == targetItem) {
            // 도망자 승리
            runnerWin();
        }
    }
    
    public void runnerWin() {
        if (!gameRunning) {
            return;
        }
        
        Player runner = getRunner();
        if (runner == null) {
            stopGame();
            return;
        }
        
        // 승리 메시지 및 효과
        String title = plugin.getConfigManager().getTitle("runner-win");
        String subtitle = plugin.getConfigManager().getSubtitle("runner-win")
                .replace("{player}", runner.getName())
                .replace("{item}", formatItemName(targetItem.name()));
        
        Title runnerWinTitle = Title.title(
            Component.text(title),
            Component.text(subtitle),
            Title.Times.times(Duration.ofMillis(10 * 50), Duration.ofMillis(70 * 50), Duration.ofMillis(20 * 50))
        );
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(runnerWinTitle);
        }
        
        // 승리 소리 효과
        String soundName = plugin.getConfigManager().getSound("target-item-found");
        if (!soundName.isEmpty()) {
            try {
                // Use the registry to get the sound
                for (Player player : Bukkit.getOnlinePlayers()) {
                    var sound = Registry.SOUND_EVENT.get(NamespacedKey.minecraft(soundName.toLowerCase()));
                    if (sound != null) {
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    }
                }
            } catch (Exception e) {
                // Fall back to the Adventure API if the sound doesn't exist
                try {
                    net.kyori.adventure.sound.Sound sound = net.kyori.adventure.sound.Sound.sound(
                        Key.key("minecraft:" + soundName.toLowerCase()),
                        net.kyori.adventure.sound.Sound.Source.MASTER,
                        1.0f,
                        1.0f
                    );
                    
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(sound);
                    }
                } catch (Exception ex) {
                    plugin.getLogger().warning("잘못된 사운드 이름: " + soundName);
                }
            }
        }
        
        stopGame();
    }
    
    public void hunterWin() {
        if (!gameRunning) {
            return;
        }
        
        // 승리 메시지 및 효과
        String title = plugin.getConfigManager().getTitle("hunter-win");
        String subtitle = plugin.getConfigManager().getSubtitle("hunter-win");
        
        Title hunterWinTitle = Title.title(
            Component.text(title),
            Component.text(subtitle),
            Title.Times.times(Duration.ofMillis(10 * 50), Duration.ofMillis(70 * 50), Duration.ofMillis(20 * 50))
        );
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(hunterWinTitle);
        }
        
        // 승리 소리 효과
        String soundName = plugin.getConfigManager().getSound("game-end");
        if (!soundName.isEmpty()) {
            try {
                // Use the registry to get the sound
                for (Player player : Bukkit.getOnlinePlayers()) {
                    var sound = Registry.SOUND_EVENT.get(NamespacedKey.minecraft(soundName.toLowerCase()));
                    if (sound != null) {
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    }
                }
            } catch (Exception e) {
                // Fall back to the Adventure API if the sound doesn't exist
                try {
                    net.kyori.adventure.sound.Sound sound = net.kyori.adventure.sound.Sound.sound(
                        Key.key("minecraft:" + soundName.toLowerCase()),
                        net.kyori.adventure.sound.Sound.Source.MASTER,
                        1.0f,
                        1.0f
                    );
                    
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(sound);
                    }
                } catch (Exception ex) {
                    plugin.getLogger().warning("잘못된 사운드 이름: " + soundName);
                }
            }
        }
        
        stopGame();
    }
    
    private String formatItemName(String name) {
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                result.append(word.substring(0, 1).toUpperCase());
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }
        
        return result.toString().trim();
    }
}