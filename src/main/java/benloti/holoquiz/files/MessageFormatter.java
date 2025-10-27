package benloti.holoquiz.files;

import benloti.holoquiz.database.UserPersonalisation;
import benloti.holoquiz.dependencies.CMIDep;
import benloti.holoquiz.structs.PlayerSettings;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageFormatter {

    private static MessageFormatter instance;
    private CMIDep cmiDep;
    private UserPersonalisation userPersonalisation;
    private String label;

    private Pattern suffixPattern;

    public static void createSender(CMIDep cmiDep, UserPersonalisation userPersonalisation, String prefix) {
        instance = new MessageFormatter();
        instance.cmiDep = cmiDep;
        instance.userPersonalisation = userPersonalisation;
        instance.label = prefix;
        String regexString = "(\\w|-|_)([.,!?])(\\s|$)+";
        instance.suffixPattern = Pattern.compile(regexString);
    }

    public static MessageFormatter getSender() {
        if (instance == null) {
            instance = new MessageFormatter();
        }
        return instance;
    }

    public static void updateSender(CMIDep cmiDep, UserPersonalisation userPersonalisation, String prefix) {
        instance = getSender();
        instance.cmiDep = cmiDep;
        instance.userPersonalisation = userPersonalisation;
        instance.label = prefix;
    }

    public String[] formatColours(String[] stringArray) {
        int size = stringArray.length;
        for (int i = 0; i < size; i++) {
            stringArray[i] = formatColours(stringArray[i]);
        }
        return stringArray;
    }

    public String formatColours(String unformattedString) {
        unformattedString = ChatColor.translateAlternateColorCodes('&', unformattedString);
        if (cmiDep.isEnabled()) {
            unformattedString = cmiDep.translateHexColors(unformattedString);
        }
        return unformattedString;
    }

    public void sendToPlayer(Player player, ArrayList<String> messages, boolean addPrefix, boolean addCustomSuffix, boolean forceSend) {
        String playerUUID = player.getUniqueId().toString();
        PlayerSettings playerSettings = userPersonalisation.getPlayerSettings(playerUUID);
        if(playerSettings == null) {
            playerSettings = new PlayerSettings("", true);
        }
        if (!playerSettings.isNotificationEnabled() && !forceSend) {
            return;
        }

        for(String peko: messages) {
            player.sendMessage(formatMessage(playerSettings, peko, addPrefix, addCustomSuffix));
        }
    }

    public void sendToPlayer(Player player, String[] messages, boolean addPrefix, boolean addCustomSuffix, boolean forceSend) {
        String playerUUID = player.getUniqueId().toString();
        PlayerSettings playerSettings = userPersonalisation.getPlayerSettings(playerUUID);
        if(playerSettings == null) {
            playerSettings = new PlayerSettings("", true);
        }
        if (!playerSettings.isNotificationEnabled() && !forceSend) {
            return;
        }

        for(String peko: messages) {
            player.sendMessage(formatMessage(playerSettings, peko, addPrefix, addCustomSuffix));
        }
    }

    public void sendToPlayer(Player player, String message, boolean addPrefix, boolean addCustomSuffix, boolean forceSend) {
        String playerUUID = player.getUniqueId().toString();
        PlayerSettings playerSettings = userPersonalisation.getPlayerSettings(playerUUID);
        if(playerSettings == null) {
            playerSettings = new PlayerSettings("", true);
        }
        if (!playerSettings.isNotificationEnabled() && !forceSend) {
            return;
        }

        player.sendMessage(formatMessage(playerSettings, message, addPrefix, addCustomSuffix));
    }

    public void sendToConsole(CommandSender sender, String[] message) {
        sender.sendMessage(formatColours(message));
    }

    public void sendToConsole(CommandSender sender, String message) {
        sender.sendMessage(formatColours(message));
    }

    public String inaGoesWAH(String message) {
        message = message.replace("Wha", "WAH");
        return message.replace("wha", "WAH");
    }

    private String formatMessage(PlayerSettings playerSettings, String message, boolean addPrefix, boolean addCustomSuffix) {
        //Add Prefix if necessary
        if (addPrefix) {
            message = label + message;
        }
        //Add Suffix if necessary
        if (playerSettings == null) {
            return message;
        }
        if (!playerSettings.getSuffix().isEmpty() && addCustomSuffix) {
            String playerSuffix = playerSettings.getSuffix();
            Matcher regexMatcher = suffixPattern.matcher(message);
            int guard = 0;
            while (regexMatcher.find() && guard < 100) {
                String original = regexMatcher.group(0);
                String newString = regexMatcher.group(1) + playerSuffix + regexMatcher.group(2) + regexMatcher.group(3);
                message = message.replace(original, newString);
                guard += 1;
            }
            if (guard == 100) {
                Logger.getLogger().devError("Yabe peko! Suffix Regex broke. Message: " + message);
            }
        }
        return formatColours(message);
    }

}