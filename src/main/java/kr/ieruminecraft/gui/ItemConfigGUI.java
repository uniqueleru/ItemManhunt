package kr.ieruminecraft.gui;

import kr.ieruminecraft.itemmanhunt;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;

import java.util.*;

public class ItemConfigGUI {

    private final itemmanhunt plugin;
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final String inventoryTitle = "§8목표 아이템 설정 - 페이지 ";
    private final int PAGE_SIZE = 45;
    private final List<Material> allItems;
    
    private final ItemStack prevPageItem;
    private final ItemStack nextPageItem;

    public ItemConfigGUI(itemmanhunt plugin) {
        this.plugin = plugin;
        
        // 사용 가능한 모든 아이템 목록 생성
        allItems = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.isItem() && !material.isAir() && material != Material.COMPASS) {
                allItems.add(material);
            }
        }
        
        // 페이지 이동 아이템 생성
        prevPageItem = createNavigationItem(Material.ARROW, 
                plugin.getConfigManager().getLangConfig().getString("messages.gui-prev-page", "이전 페이지"));
        
        nextPageItem = createNavigationItem(Material.ARROW, 
                plugin.getConfigManager().getLangConfig().getString("messages.gui-next-page", "다음 페이지"));
    }
    
    private ItemStack createNavigationItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(plugin.getConfigManager().colorize(name)));
        item.setItemMeta(meta);
        return item;
    }
    
    public void openGUI(Player player) {
        openGUI(player, 1);
    }
    
    public void openGUI(Player player, int page) {
        int totalPages = (int) Math.ceil((double) allItems.size() / PAGE_SIZE);
        
        if (page < 1) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }
        
        playerPages.put(player.getUniqueId(), page);
        
        Inventory inv = Bukkit.createInventory(player, 54, Component.text(inventoryTitle + page));
        
        // 현재 페이지에 표시할 아이템 계산
        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, allItems.size());
        
        // 아이템 채우기
        for (int i = startIndex; i < endIndex; i++) {
            Material material = allItems.get(i);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            // 아이템 이름 설정
            String formattedName = formatItemName(material.name());
            meta.displayName(Component.text("§f" + formattedName));
            
            // 아이템 로어 설정 (활성화/비활성화 상태)
            List<String> lore = new ArrayList<>();
            boolean enabled = plugin.getConfigManager().isItemEnabled(material);
            String statusMsg = enabled ? 
                    plugin.getConfigManager().getLangConfig().getString("messages.item-enabled", "활성화됨") : 
                    plugin.getConfigManager().getLangConfig().getString("messages.item-disabled", "비활성화됨");
            
            lore.add(plugin.getConfigManager().colorize(statusMsg));
            lore.add(plugin.getConfigManager().colorize("&7클릭하여 토글"));
            
            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(Component.text(line));
            }
            meta.lore(loreComponents);
            
            item.setItemMeta(meta);
            inv.setItem(i - startIndex, item);
        }
        
        // 네비게이션 아이템 추가
        if (page > 1) {
            inv.setItem(45, prevPageItem);
        }
        
        if (page < totalPages) {
            inv.setItem(53, nextPageItem);
        }
        
        player.openInventory(inv);
    }
    
    public void handleClick(Player player, Inventory inventory, int slot) {
        Component titleComponent = player.getOpenInventory().title();
        String title = titleComponent.toString();
        
        if (!title.contains(inventoryTitle)) {
            return;
        }
        
        // 페이지 번호 추출
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);
        
        // 이전 페이지 버튼
        if (slot == 45 && currentPage > 1) {
            openGUI(player, currentPage - 1);
            return;
        }
        
        // 다음 페이지 버튼
        if (slot == 53) {
            int totalPages = (int) Math.ceil((double) allItems.size() / PAGE_SIZE);
            if (currentPage < totalPages) {
                openGUI(player, currentPage + 1);
                return;
            }
        }
        
        // 아이템 클릭 처리
        if (slot >= 0 && slot < PAGE_SIZE) {
            int itemIndex = (currentPage - 1) * PAGE_SIZE + slot;
            
            if (itemIndex < allItems.size()) {
                Material material = allItems.get(itemIndex);
                
                // 아이템 활성화/비활성화 토글
                plugin.getConfigManager().toggleItemEnabled(material);
                
                // GUI 업데이트
                openGUI(player, currentPage);
            }
        }
    }
    
    public boolean isConfigGUI(Inventory inventory, Player player) {
        if (inventory == null || player == null) {
            return false;
        }
        Component titleComponent = player.getOpenInventory().title();
        String title = titleComponent.toString();
        return title.contains(inventoryTitle);
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