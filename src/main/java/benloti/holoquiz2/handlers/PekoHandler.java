package benloti.holoquiz2.handlers;

import benloti.holoquiz2.HoloQuiz2;
import benloti.holoquiz2.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PekoHandler implements Listener {
    public PekoHandler(HoloQuiz2 plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private static Map<String, PlayerData> fullPlayerData = new HashMap<>();

    public static PlayerData getPlayerData(Player p) {
        String playerName = p.getUniqueId().toString();
        if (!fullPlayerData.containsKey(playerName)) {
            PlayerData playerData = new PlayerData();
            fullPlayerData.put(playerName, playerData);
        }
        return fullPlayerData.get(playerName);
    }

    public static void updatePlayerData(Player p, PlayerData peko) {
        fullPlayerData.put(p.getUniqueId().toString(), peko);
    }

    @EventHandler
    public void sentPekoToChat(AsyncPlayerChatEvent theMessage) {
        if (theMessage.getMessage().equals("peko")) {
            givePekoCarrot(theMessage);
            reportPekoCount(theMessage);
        }

    }

    private static void reportPekoCount(AsyncPlayerChatEvent theMessage) {
        Player player = theMessage.getPlayer();
        PlayerData playerData = getPlayerData(player);
        playerData.increasePekoCount();
        int numberOfPekos = playerData.getPekoCount();
        updatePlayerData(player, playerData);
        String message = "You have sent " + numberOfPekos + " pekos to chat!";
        player.sendMessage(message);
    }

    /*
    @EventHandler
    public void checkPlayerJoin(PlayerJoinEvent joinEvent) {
        Player player = joinEvent.getPlayer();
        PlayerData playerData = getPlayerData(player);
    }
    */



    private static void givePekoCarrot(AsyncPlayerChatEvent theMessage) {
        Player player = theMessage.getPlayer();
        String playerName = player.getName();

        ItemStack item = new ItemStack(Material.CARROT, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        String name = "&6Pekopekopeko";
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> loreList = new ArrayList<>();
        loreList.add("&bA carrot from Pekoland, for &a" + playerName);
        loreList.add("&bAH HA HA HA HA");
        List<String> coloredLoreList = new ArrayList<>();
        for (String s : loreList) {
            coloredLoreList.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        meta.setLore(coloredLoreList);

        item.setItemMeta(meta);
        Inventory inv = player.getInventory();
        inv.addItem(item);
    }
}
