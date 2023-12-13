package benloti.holoquiz.leaderboard;

import benloti.holoquiz.structs.PlayerData;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;

public class AverageBestTime {
    private ArrayList<PlayerData> topPlayers;
    private final boolean leaderboardOptimisation;
    private double lowerLimit;

    public AverageBestTime(boolean optimisation) {
        topPlayers = new ArrayList<>();
        this.leaderboardOptimisation = optimisation;
    }

    public ArrayList<PlayerData> getTopPlayers() {
        return topPlayers;
    }

    public void addToTopPlayers(PlayerData playerData) {
        topPlayers.add(playerData);
    }

    public void updateTopPlayers(PlayerData playerData) {
        String playerName = playerData.getPlayerName();
        if (!leaderboardOptimisation) {
           unoptimisedUpdateTopPlayers(playerData, playerName);
           return;
        }
        optimisedUpdateTopPlayers(playerData, playerName);
    }

    private void optimisedUpdateTopPlayers(PlayerData playerData, String playerName) {
        int index = -1;
        boolean qualifiedForLeaderboards = (playerData.getAverageTime() < lowerLimit);

        for(int i = 0; i < topPlayers.size() ; i++) {
            if (topPlayers.get(i).getPlayerName().equals(playerName)) {
                index = i;
            }
        }

        if(qualifiedForLeaderboards && index != -1) {
            topPlayers.set(index, playerData);
            topPlayers.sort(new sortByAverageTime());
            return;
        }
        if(qualifiedForLeaderboards) {
            topPlayers.add(playerData);
            return;
        }
        if(index != -1) {
            topPlayers.remove(index);
        }
    }

    private void unoptimisedUpdateTopPlayers(PlayerData playerData, String playerName) {
        for (int i = 0; i < topPlayers.size(); i++) {
            PlayerData currentTop = topPlayers.get(i);
            if (currentTop.getPlayerName().equals(playerName)) {
                topPlayers.set(i, playerData);
                topPlayers.sort(new sortByAverageTime());
                return;
            }
        }
        Bukkit.getLogger().log(Level.SEVERE, "[HoloQuiz] Player not found in DS for Average Answer Time");
    }

    public boolean isLeaderboardOptimised() {
        return leaderboardOptimisation;
    }

    public void executeOptimisation(int limit) {
        if(topPlayers.size() <= limit) {
            return;
        }
        ArrayList<PlayerData> optimisedList = new ArrayList<>();
        Comparator<PlayerData> comparator = new sortByAverageTime();
        for (PlayerData peko : topPlayers) {
            //Check if optimisedList is full. If not, just add and sort.
            if (optimisedList.size() < limit) {
                optimisedList.add(peko);
                optimisedList.sort(comparator);
                continue;
            }
            //Check if PlayerData fits into the list, and if it goes in properly.
            assert optimisedList.size() == limit;
            if (peko.getAverageTime() > optimisedList.get(limit - 1).getAverageTime()) {
                continue;
            }
            optimisedList.add(peko);
            optimisedList.sort(comparator);
            optimisedList.remove(limit);
        }
        this.lowerLimit = 2 * optimisedList.get(limit - 1).getAverageTime() - optimisedList.get(0).getAverageTime();
        optimisedList.clear();
        for (PlayerData peko : topPlayers) {
            if (peko.getAverageTime() < lowerLimit) {
                optimisedList.add(peko);
            }
        }
        this.topPlayers = optimisedList;
        Bukkit.getLogger().info("[HoloQuiz] Optimisation Activated - Size is: " + topPlayers.size());
    }


    static class sortByAverageTime implements Comparator<PlayerData> {
        public int compare(PlayerData player1, PlayerData player2) {
            double determinant = player1.getAverageTime() - player2.getAverageTime(); //Note inversion!
            if (determinant > 0) {
                return 1;
            }
            if (determinant < 0) {
                return -1;
            }
            return (player2.getQuestionsAnswered() - player1.getQuestionsAnswered());
        }
    }
}

