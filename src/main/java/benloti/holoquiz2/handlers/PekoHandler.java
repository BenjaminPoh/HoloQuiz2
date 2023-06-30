package benloti.holoquiz2.handlers;

import benloti.holoquiz2.HoloQuiz2;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PekoHandler implements Listener {
    public PekoHandler(HoloQuiz2 plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void sentPekoToChat(AsyncPlayerChatEvent theMessage) {
        if (theMessage.getMessage().equals("peko")) {
            Player player = theMessage.getPlayer();

            ItemStack item = new ItemStack(Material.CARROT, 1);

            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName("PekoPekoPeko");
            //meta.set
            item.setItemMeta(meta);

            Inventory inv = player.getInventory();
            inv.addItem(item);
        }
    }
}
