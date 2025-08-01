package benloti.holoquiz.files;

import benloti.holoquiz.database.UserPersonalisation;
import benloti.holoquiz.dependencies.CMIDep;
import benloti.holoquiz.structs.ContestInfo;
import benloti.holoquiz.structs.ContestWinner;
import benloti.holoquiz.structs.PlayerSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInterface {

    private final boolean isHexEnabled;
    private final CMIDep cmiDep;
    private final UserPersonalisation userPersonalisation;
    private final String label;

    public UserInterface(CMIDep cmiDep, UserPersonalisation userPersonalisation, String prefix) {
        this.cmiDep = cmiDep;
        this.isHexEnabled = (cmiDep != null);
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

    public String antiCheatCommandFormatter(String cmd, String playerName, double stat) {
        cmd = cmd.replace("[stat]" , Double.toString(stat));
        return attachPlayerName(cmd, playerName);
    }

    public String attachPlayerName(String message, String playerName) {
        if(message.contains("[player]")) {
            return message.replace("[player]", playerName);
        }
        return message;
    }

    public void sendMessages(Player player, ArrayList<String> msgList) {
        for(String msg : msgList) {
            String formattedMsg = formatColours(msg);
            attachSuffixAndSend(player, formattedMsg);
        }
    }

    public String attachContestStats(String message, ContestWinner contestWinner, ContestInfo contestInfo) {
        if(message.contains("[count]")) {
            String totalAnswers = String.valueOf(contestWinner.getContestWinnerData().getQuestionsAnswered());
            message = message.replace("[count]", totalAnswers);
        }
        if(message.contains("[best]")) {
            String bestTime = contestWinner.getContestWinnerData().getBestTimeInSeconds3DP();
            message = message.replace("[best]", bestTime);
        }
        if(message.contains("[avg]")) {
            String avgTime = contestWinner.getContestWinnerData().getAverageTimeInSeconds3DP();
            message = message.replace("[avg]", avgTime);
        }
        if(message.contains("[month]")) {
            String monthOfContest = contestInfo.getStartDate().getMonth().toString();
            String formattedMonth = monthOfContest.charAt(0) + monthOfContest.substring(1).toLowerCase();
            message = message.replace("[month]", formattedMonth);
        }
        if(message.contains("[year]")) {
            String yearOfContest = Integer.toString(contestInfo.getStartDate().getYear());
            message = message.replace("[year]", yearOfContest);
        }
        if(message.contains("[start]")) {
            String startDateOfContest = contestInfo.getStartDate().toString();
            message = message.replace("[start]", startDateOfContest);
        }
        if(message.contains("[end]")) {
            String endDateOfContest = contestInfo.getEndDate().toString();
            message = message.replace("[end]", endDateOfContest);
        }
        if(message.contains("[pos]")) {
            String position = String.valueOf(contestWinner.getPosition());
            message = message.replace("[pos]", position);
        }
        if(message.contains("[pos++]")) {
            String position = positionFormatter(contestWinner.getPosition());
            message = message.replace("[pos++]", position);
        }
        return message;
    }

    private String positionFormatter(int i) {
        if(i == 1) {
            return i + "st";
        }
        if(i == 2) {
            return i + "nd";
        }
        if(i == 3) {
            return i + "rd";
        }
        return i + "th";
    }
}
