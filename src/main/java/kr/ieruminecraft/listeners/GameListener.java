package kr.ieruminecraft.listeners;

import kr.ieruminecraft.itemmanhunt;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class GameListener implements Listener {

    private final itemmanhunt plugin;

    public GameListener(itemmanhunt plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 게임 중일 때 참가하는 플레이어는 추격자로 지정
        if (plugin.getGameManager().isGameRunning() && 
                !plugin.getGameManager().isRunner(player)) {
            
            // 추격 시작 이후 참가한 경우, 나침반 지급
            if (plugin.getGameManager().isChaseStarted()) {
                ItemStack compass = new ItemStack(Material.COMPASS);
                player.getInventory().addItem(compass);
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 도망자가 나가면 추격자 승리
        if (plugin.getGameManager().isGameRunning() && 
                plugin.getGameManager().isRunner(player)) {
            plugin.getGameManager().hunterWin();
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // 도망자가 죽으면 추격자 승리
        if (plugin.getGameManager().isGameRunning() && 
                plugin.getGameManager().isRunner(player)) {
            plugin.getGameManager().hunterWin();
        }
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // 게임 중 추격자 부활 시 나침반 다시 지급
        if (plugin.getGameManager().isGameRunning() && 
                plugin.getGameManager().isChaseStarted() && 
                plugin.getGameManager().isHunter(player)) {
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack compass = new ItemStack(Material.COMPASS);
                    player.getInventory().addItem(compass);
                }
            }.runTaskLater(plugin, 5L);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // GUI 클릭 처리
        if (plugin.getItemConfigGUI().isConfigGUI(event.getInventory(), player)) {
            event.setCancelled(true);
            plugin.getItemConfigGUI().handleClick(player, event.getInventory(), event.getRawSlot());
        }
    }
    
    @EventHandler
    public void onItemCollect(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (!plugin.getGameManager().isGameRunning() || 
                !plugin.getGameManager().isRunner(player)) {
            return;
        }
        
        // 아이템 획득 확인
        if (event.getCurrentItem() != null && 
                event.getCurrentItem().getType() == plugin.getGameManager().getTargetItem()) {
            plugin.getGameManager().checkWinCondition(player, event.getCurrentItem());
        }
    }
    
    @EventHandler
    public void onPlayerInventoryUpdate(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (!plugin.getGameManager().isGameRunning() || 
                !plugin.getGameManager().isRunner(player)) {
            return;
        }
        
        // 인벤토리 변경 후 목표 아이템 확인 (다음 틱에 수행)
        new BukkitRunnable() {
            @Override
            public void run() {
                checkPlayerInventory(player);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    private void checkPlayerInventory(Player player) {
        if (!plugin.getGameManager().isGameRunning() || 
                !plugin.getGameManager().isRunner(player)) {
            return;
        }
        
        Material targetItem = plugin.getGameManager().getTargetItem();
        PlayerInventory inventory = player.getInventory();
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == targetItem) {
                plugin.getGameManager().checkWinCondition(player, item);
                return;
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        // 도망 시간 동안 모든 플레이어 무적
        if (plugin.getGameManager().isGameRunning() && 
                !plugin.getGameManager().isChaseStarted()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || 
                !(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();
        
        // 추격자끼리 PVP 방지
        if (plugin.getGameManager().isGameRunning() && 
                plugin.getGameManager().isHunter(victim) && 
                plugin.getGameManager().isHunter(attacker)) {
            event.setCancelled(true);
        }
    }
}