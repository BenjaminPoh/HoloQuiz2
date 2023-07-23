package benloti.holoquiz.leaderboard;

import benloti.holoquiz.structs.PlayerData;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;

public class AverageBestTime {
    private final ArrayList<PlayerData> topPlayers;

    public AverageBestTime() {
        topPlayers = new ArrayList<>();
    }

    public ArrayList<PlayerData> getTopPlayers() {
        return topPlayers;
    }

    public void addToTopPlayers(PlayerData playerData) {
        topPlayers.add(playerData);
    }

    public void updateTopPlayers(PlayerData playerData) {
        String playerName = playerData.getPlayerName();
        for(int i = 0; i < topPlayers.size(); i++) {
            PlayerData currentTop = topPlayers.get(i);
            if(currentTop.getPlayerName().equals(playerName)) {
                topPlayers.set(i, playerData);
                topPlayers.sort(new sortByAverageTime());
                return;
            }
        }
        Bukkit.getLogger().log(Level.SEVERE, "Player not found in DS for Average Answer Time");
    }


    static class sortByAverageTime implements Comparator<PlayerData> {
        public int compare (PlayerData player1, PlayerData player2) {
            double determinant = player1.getAverageTime() - player2.getAverageTime(); //Note inversion!
            if(determinant > 0) {
                return 1;
            }
            if (determinant < 0) {
                return -1;
            }
            return (player2.getQuestionsAnswered() - player1.getQuestionsAnswered());
        }
    }
}

