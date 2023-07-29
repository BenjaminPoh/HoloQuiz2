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

    public String[] formatColoursArray(String[] stringArray) {
        int size = stringArray.length;
        for(int i = 0; i < size; i++) {
            stringArray[i] = formatColours(stringArray[i]);
        }
        return stringArray;
    }
    
    public String formatColours(String unformattedString) {
        unformattedString = ChatColor.translateAlternateColorCodes('&', unformattedString);
        if(isHexEnabled) {
            unformattedString = cmiDep.translateHexColors(unformattedString);
        }
        return unformattedString;
    }
    
    public void attachSuffixAndSend(Player player, String message) {
        String playerUUID = player.getUniqueId().toString();
        PlayerSettings playerSettings = userPersonalisation.getPlayerSettings(playerUUID);
        if(playerSettings == null) {
            player.sendMessage(message);
            return;
        }
        if(playerSettings.isNotificationEnabled()) {
            String playerSuffix = playerSettings.getSuffix();
            message = message.replace("!", playerSuffix+"!");
            message = message.replace("?", playerSuffix+"?");
            message = message.replace(",", playerSuffix+",");
            message = message.replace(".", playerSuffix+".");
            player.sendMessage(message);
        }
    }

    public void attachFixedSuffixAndSend(Player player, String message) {
        String playerUUID = player.getUniqueId().toString();
        PlayerSettings playerSettings = userPersonalisation.getPlayerSettings(playerUUID);
        if(playerSettings == null) {
            player.sendMessage(message);
        }
        if(playerSettings.isNotificationEnabled()) {
            String playerSuffix = playerSettings.getSuffix();
            message = message.replace("[suffix]", playerSuffix);
            player.sendMessage(message);
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
