package benloti.holoquiz2.archive;

import org.bukkit.entity.Player;

import java.util.Map;

public class AllPlayerData {

    private static Map<String, PlayerData> fullPlayerData;

    public AllPlayerData(Map<String, PlayerData> data) {
        fullPlayerData = data;
    }

    public static PlayerData getPlayerData(Player p) {
        String playerName = p.getUniqueId().toString();
        if (!fullPlayerData.containsKey(playerName)) {
            PlayerData playerData = new PlayerData();
            fullPlayerData.put(playerName, playerData);
        }
        return fullPlayerData.get(playerName);
    }

    public static void updatePlayerData(Player p, PlayerData peko) {
        fullPlayerData.put(p.getUniqueId().toString(), peko);
    }


}
