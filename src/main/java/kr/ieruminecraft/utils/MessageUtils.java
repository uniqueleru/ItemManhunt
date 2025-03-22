package kr.ieruminecraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import java.time.Duration;

public class MessageUtils {

    public static void broadcastMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }
    
    public static void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Title titleObj = Title.title(
            Component.text(title),
            Component.text(subtitle),
            Title.Times.times(Duration.ofMillis(fadeIn * 50), Duration.ofMillis(stay * 50), Duration.ofMillis(fadeOut * 50))
        );
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(titleObj);
        }
    }
}