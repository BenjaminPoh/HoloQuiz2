package benloti.holoquiz.files;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InvClickListener implements Listener {

    private final ContestManager contestManager;

    public InvClickListener(ContestManager contestManager) {
        this.contestManager = contestManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("HoloQuiz Contests")) {
            return;
        }
        event.setCancelled(true); // Prevent the player from taking the items
    }

    @EventHandler
    public void closeInventory(InventoryCloseEvent event) {
        if (!event.getView().getTitle().contains("HoloQuiz Contests")) {
            return;
        }
        contestManager.updateClosedContestGUI(event.getPlayer().getName());

    }
}
