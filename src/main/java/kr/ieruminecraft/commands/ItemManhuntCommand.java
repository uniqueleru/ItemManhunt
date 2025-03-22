package kr.ieruminecraft.commands;

import kr.ieruminecraft.itemmanhunt;
import kr.ieruminecraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemManhuntCommand implements CommandExecutor, TabCompleter {

    private final itemmanhunt plugin;
    private final List<String> subCommands = Arrays.asList("help", "setrunner", "start", "stop", "reload", "config");

    public ItemManhuntCommand(itemmanhunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // 도움말 표시
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                break;
                
            case "setrunner":
                if (!sender.hasPermission("itemmanhunt.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-player"));
                    return true;
                }
                
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-player"));
                    return true;
                }
                
                plugin.getGameManager().setRunner(target);
                MessageUtils.broadcastMessage(plugin.getConfigManager().getMessage("runner-set")
                        .replace("{player}", target.getName()));
                break;
                
            case "start":
                if (!sender.hasPermission("itemmanhunt.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                
                if (plugin.getGameManager().isGameRunning()) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("game-already-running"));
                    return true;
                }
                
                plugin.getGameManager().startGame();
                break;
                
            case "stop":
                if (!sender.hasPermission("itemmanhunt.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                
                if (!plugin.getGameManager().isGameRunning()) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("game-not-running"));
                    return true;
                }
                
                plugin.getGameManager().stopGame();
                break;
                
            case "reload":
                if (!sender.hasPermission("itemmanhunt.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                
                plugin.getConfigManager().reloadConfig();
                sender.sendMessage(plugin.getConfigManager().getMessage("reload"));
                break;
                
            case "config":
                if (!sender.hasPermission("itemmanhunt.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                
                if (!(sender instanceof Player)) {
                    sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
                    return true;
                }
                
                Player player = (Player) sender;
                plugin.getItemConfigGUI().openGUI(player);
                break;
                
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        List<String> helpMessages = plugin.getConfigManager().getMessageList("help");
        for (String message : helpMessages) {
            sender.sendMessage(message);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(subCmd -> subCmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setrunner")) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}