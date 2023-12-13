package benloti.holoquiz.leaderboard;

import benloti.holoquiz.structs.PlayerData;

import java.util.ArrayList;
import java.util.Comparator;

public class MostAnswers {
    private final ArrayList<PlayerData> topPlayers;

    public MostAnswers() {
        topPlayers = new ArrayList<>();
    }
    
    public ArrayList<PlayerData> getTopPlayers () {
        return topPlayers;
    }

    public void updateTopPlayers(PlayerData playerData, int limit) {
        String playerName = playerData.getPlayerName();
        //Check if already in leaderboards
        for(int i = 0; i < topPlayers.size() ; i++) {
            PlayerData currentTop = topPlayers.get(i);
            if(currentTop.getPlayerName().equals(playerName)) {
                topPlayers.set(i, playerData);
                topPlayers.sort(new sortByMostAnswers());
                return;
            }
        }
        //Check if there is space to just insert
        if (topPlayers.size() < limit) {
            topPlayers.add(playerData);
            topPlayers.sort(new sortByMostAnswers());
            return;
        }
        //Check if able to replace last person in leaderboards
        int lastPlacePosition = topPlayers.size() - 1;
        int lastPlaceScore = topPlayers.get(lastPlacePosition).getQuestionsAnswered();
        if(lastPlaceScore < playerData.getQuestionsAnswered()) {
            topPlayers.set(lastPlacePosition, playerData);
        }
    }

    static class sortByMostAnswers implements Comparator<PlayerData> {
        public int compare (PlayerData player1, PlayerData player2) {
            int determinant = player2.getQuestionsAnswered() - player1.getQuestionsAnswered();
            if(determinant != 0) {
                return determinant;
            }
            return (player2.getBestTime() - player1.getBestTime());
        }
    }
}
