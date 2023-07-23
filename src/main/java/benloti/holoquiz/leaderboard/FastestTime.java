package benloti.holoquiz.leaderboard;

import benloti.holoquiz.structs.PlayerData;

import java.util.ArrayList;
import java.util.Comparator;

public class FastestTime {
    private final ArrayList<PlayerData> topPlayers;

    public FastestTime() {
        topPlayers = new ArrayList<>();
    }

    public ArrayList<PlayerData> getTopPlayers() {
        return topPlayers;
    }

    public void updateTopPlayers(PlayerData playerData, int limit) {
        String playerName = playerData.getPlayerName();
        //Check if already in leaderboards
        for (int i = 0; i < topPlayers.size(); i++) {
            PlayerData currentTop = topPlayers.get(i);
            if (currentTop.getPlayerName().equals(playerName)) {
                topPlayers.set(i, playerData);
                topPlayers.sort(new sortByBestTime());
                return;
            }
        }
        //Check if there is space to just insert
        if (topPlayers.size() < limit) {
            topPlayers.add(playerData);
            topPlayers.sort(new sortByBestTime());
            return;
        }
        //Check if overtook the slowest person on leaderboards
        int lastPlacePosition = topPlayers.size() - 1;
        int lastPlaceScore = topPlayers.get(lastPlacePosition).getBestTime();
        if (lastPlaceScore > playerData.getBestTime()) {
            topPlayers.set(lastPlacePosition, playerData);
            topPlayers.sort(new sortByBestTime());
        }
    }

    static class sortByBestTime implements Comparator<PlayerData> {
        public int compare(PlayerData player1, PlayerData player2) {
            int determinant = player1.getBestTime() - player2.getBestTime(); //Note inversion!
            if (determinant != 0) {
                return determinant;
            }
            return (player2.getQuestionsAnswered() - player1.getQuestionsAnswered());
        }
    }
}