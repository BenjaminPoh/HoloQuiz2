package benloti.holoquiz.files;

import benloti.holoquiz.database.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final DatabaseManager dbMan;
    private final UserInterface userInterface;

    public static final String NOTIFY_STORAGE_CLEARED = "&aHere are your HoloQuiz rewards!";
    public static final String NOTIFY_STORAGE_NOT_CLEARED = "&cYou have HoloQuiz rewards waiting to be collected!";

    public PlayerJoinListener(DatabaseManager dbMan, UserInterface userInterface) {
        this.dbMan = dbMan;
        this.userInterface = userInterface;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int statusCode = dbMan.getRewardsFromStorage(player);
        if(statusCode == -2) {
            // Empty Storage
            return;
        }

        String message;
        if (statusCode == 0) {
            message = NOTIFY_STORAGE_CLEARED;
        } else {
            message = NOTIFY_STORAGE_NOT_CLEARED;
        }
        message = userInterface.formatColours(message);
        userInterface.attachSuffixAndSend(player, message);
    }
}
