package benloti.holoquiz.archive;

import benloti.holoquiz.HoloQuiz;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class TestHandler implements Listener {
    public TestHandler(HoloQuiz plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    //only in this shibal import that a lower priority run before the higher ones
    public void onTorchPlaced2(BlockPlaceEvent event) {
        if(!event.getPlayer().hasPermission("Thetestpermission")) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();
        if(block.getType() == Material.REDSTONE_TORCH) {
            block.setType(Material.WARPED_HYPHAE);
            Bukkit.getLogger().info("mahboi placed a redstone torch, now its a hyphae");
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onTorchPlaced(BlockPlaceEvent testEvent) {
        Block block = testEvent.getBlock();

        if (block.getType() != Material.TORCH) {
            return;
        }
        Bukkit.getLogger().info("Mah boi placed a torch");
    }
}
