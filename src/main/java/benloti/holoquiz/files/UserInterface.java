package benloti.holoquiz.files;

import benloti.holoquiz.database.UserPersonalisation;
import benloti.holoquiz.dependencies.CMIDep;
import benloti.holoquiz.structs.PlayerSettings;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class UserInterface {

    private final boolean isHexEnabled;
    private final CMIDep cmiDep;
    private final UserPersonalisation userPersonalisation;

    private static final String HOLOQUIZ_LABEL = "&7[{#13A7DE>}HoloQuiz{#21C7FF<}&7] ";

    public UserInterface(CMIDep cmiDep, UserPersonalisation userPersonalisation) {
        this.cmiDep = cmiDep;
        this.isHexEnabled = (cmiDep != null);
        this.userPersonalisation = userPersonalisation;
    }

    public String formatColours(String unformattedString) {
        unformattedString = ChatColor.translateAlternateColorCodes('&', unformattedString);
        if(isHexEnabled) {
            unformattedString = cmiDep.translateHexColors(unformattedString);
        }
        return unformattedString;
    }

    public void sendMessageToPlayer(Player player, String formattedQuestion) {
        String playerUUID = player.getUniqueId().toString();
        PlayerSettings playerSettings = userPersonalisation.getPlayerSettings(playerUUID);
        if(playerSettings == null) {
            player.sendMessage(formattedQuestion);
            return;
        }
        if(playerSettings.isNotificationEnabled()) {
            String playerSuffix = playerSettings.getSuffix();
            player.sendMessage(formattedQuestion + playerSuffix);
        }
    }
    
    public String attachLabel(String message) {
        return (HOLOQUIZ_LABEL + message);
    }
    
    public String attachPlayerName(String message, Player player) {
        if(message.contains("[player]")) {
            return message.replace("[player]", player.getName());
        }
        return message;
    }


}
