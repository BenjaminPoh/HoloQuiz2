package benloti.holoquiz2.handlers;

import benloti.holoquiz2.HoloQuiz2;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class TestHandler implements Listener {
    public TestHandler(HoloQuiz2 plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    //only in this shibal import that a lower priority run before the higher ones
    public void onTorchPlaced2(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if(block.getType() == Material.TORCH) {
            block.setType(Material.WARPED_HYPHAE);
        }
        Bukkit.getLogger().info("yeetus");
    }

    @EventHandler
    public void onTorchPlaced(BlockPlaceEvent testEvent) {
        Block block = testEvent.getBlock();

        if (block.getType() != Material.TORCH) {
            return;
        }
        Bukkit.getLogger().info("DINGDINGDING");
    }
}
