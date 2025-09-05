package benloti.holoquiz.files;

import benloti.holoquiz.database.UserPersonalisation;
import benloti.holoquiz.dependencies.CMIDep;
import benloti.holoquiz.structs.PlayerSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInterface {

    private final CMIDep cmiDep;
    private final UserPersonalisation userPersonalisation;
    private final String label;

    public UserInterface(CMIDep cmiDep, UserPersonalisation userPersonalisation, String prefix) {
        this.cmiDep = cmiDep;
        this.userPersonalisation = userPersonalisation;
        this.label = prefix;
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
        if(cmiDep.isEnabled()) {
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
            String regexString = "(\\w|-|_)([.,!?])(\\s|$)+";
            Pattern regexPattern = Pattern.compile(regexString);
            Matcher regexMatcher = regexPattern.matcher(message);
            int guard = 0;
            while(regexMatcher.find() && guard < 100) {
                String original = regexMatcher.group(0);
                String newString = regexMatcher.group(1) + playerSuffix + regexMatcher.group(2) + regexMatcher.group(3);
                message = message.replace(original, newString);
                guard += 1;
            }
            if(guard == 100) {
                Bukkit.getLogger().info("[HoloQuiz] Suffix Regex broke. Yabe peko!");
            }
            player.sendMessage(message);
        }
    }
    
    public String attachLabel(String message) {
        return (label + message);
    }

    public void sendMessages(Player player, ArrayList<String> msgList) {
        for(String msg : msgList) {
            String formattedMsg = formatColours(msg);
            attachSuffixAndSend(player, formattedMsg);
        }
    }
}
