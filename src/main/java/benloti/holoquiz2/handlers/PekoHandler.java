package benloti.holoquiz2.handlers;

import benloti.holoquiz2.HoloQuiz2;
import benloti.holoquiz2.data.PlayerPekoCount;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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

    private static Map<String, PlayerPekoCount> totalPekoCount = new HashMap<>();

    public static PlayerPekoCount getTotalPekoCount(Player p) {
        String playerName = p.getUniqueId().toString();
        if (!totalPekoCount.containsKey(playerName)) {
            PlayerPekoCount count = new PlayerPekoCount();
            totalPekoCount.put(playerName, count);
        }
        return totalPekoCount.get(playerName);
    }

    public static void setTotalPekoCount(Player p, PlayerPekoCount peko) {
        totalPekoCount.put(p.getUniqueId().toString(), peko);
    }

    @EventHandler
    public void sentPekoToChat(AsyncPlayerChatEvent theMessage) {
        if (theMessage.getMessage().equals("peko")) {
            givePekoCarrot(theMessage);
            Player player = theMessage.getPlayer();
            PlayerPekoCount playerPekoCount = getTotalPekoCount(player);
            int numberOfPekos = playerPekoCount.getPekoCount();
            playerPekoCount.increasePekoCount();
            setTotalPekoCount(player, playerPekoCount);

        }
    }

    @EventHandler
    public void checkPlayerJoin(PlayerJoinEvent joinEvent) {
        Player player = joinEvent.getPlayer();
        PlayerPekoCount playerPekoCount = getTotalPekoCount(player);
    }

    private static void givePekoCarrot(AsyncPlayerChatEvent theMessage) {
        Player player = theMessage.getPlayer();
        String playerName = player.getName();

        ItemStack item = new ItemStack(Material.CARROT, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        String name = "&6Pekopekopeko";
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> loreList = new ArrayList<String>();
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
