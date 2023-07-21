package benloti.holoquiz2.leaderboard;

import benloti.holoquiz2.data.PlayerData;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;

public class Leaderboard {

    private final ArrayList<PlayerData> mostAnswers;
    private final ArrayList<PlayerData> fastestAnswers;
    private final ArrayList<PlayerData> averageBestAnswers;
    private final int playersShown;
    private final int minimumQuestionsRequired;

    public Leaderboard(int limit, int min) {
        this.playersShown = limit;
        this.minimumQuestionsRequired = min;
        mostAnswers = new ArrayList<>();
        fastestAnswers = new ArrayList<>();
        averageBestAnswers = new ArrayList<>();
    }

    public void startUpAddToData (PlayerData playerData) {
        if(playerData.getQuestionsAnswered() < minimumQuestionsRequired) {
            return;
        }

        processStartUpInformation(playerData, mostAnswers, new sortByMostAnswers());
        processStartUpInformation(playerData, fastestAnswers, new sortByBestTime());
        processStartUpInformation(playerData, averageBestAnswers, new sortByAverageTime());
    }

    public void updateLeaderBoard(PlayerData playerData) {
        updateMostAnswers(playerData);
        updateFastestAnswers(playerData);
        updateAverageBestAnswers(playerData);
    }

    private void updateMostAnswers(PlayerData playerData) {
        String playerName = playerData.getPlayerName();
        for(int i = 0; i < mostAnswers.size() ; i++) {
            PlayerData currentTop = mostAnswers.get(i);
            if(currentTop.getPlayerName().equals(playerName)) {
                mostAnswers.set(i, playerData);
                return;
            }
        }

        int lastPlacePosition = mostAnswers.size() - 1;
        int lastPlaceScore = mostAnswers.get(lastPlacePosition).getQuestionsAnswered();
        if(lastPlaceScore < playerData.getQuestionsAnswered()) {
            mostAnswers.set(lastPlacePosition, playerData);
        }
    }

    private void updateFastestAnswers(PlayerData playerData) {
        String playerName = playerData.getPlayerName();
        for(int i = 0; i < fastestAnswers.size() && i < playersShown ; i++) {
            PlayerData currentTop = fastestAnswers.get(i);
            if(currentTop.getPlayerName().equals(playerName)) {
                fastestAnswers.set(i, playerData);
                mostAnswers.sort(new sortByBestTime());
                return;
            }
        }

        int lastPlacePosition = fastestAnswers.size() - 1;
        int lastPlaceScore = fastestAnswers.get(lastPlacePosition).getBestTime();
        if(lastPlaceScore > playerData.getBestTime()) {
            mostAnswers.set(lastPlacePosition, playerData);
            mostAnswers.sort(new sortByBestTime());
        }
    }

    private void updateAverageBestAnswers(PlayerData playerData) {
        if(playerData.getQuestionsAnswered() < minimumQuestionsRequired) {
            return;
        }

        String playerName = playerData.getPlayerName();
        for(int i = 0; i < averageBestAnswers.size(); i++) {
            PlayerData currentTop = averageBestAnswers.get(i);
            if(currentTop.getPlayerName().equals(playerName)) {
                averageBestAnswers.set(i, playerData);
                averageBestAnswers.sort(new sortByAverageTime());
                return;
            }
        }
        Bukkit.getLogger().log(Level.SEVERE, "Player not found in DS for Average Answer Time");
    }

    public void addNewAverageBestAnswers(PlayerData playerData) {
        if(playerData.getQuestionsAnswered() < minimumQuestionsRequired) {
            return;
        }
        averageBestAnswers.add(playerData);
        averageBestAnswers.sort(new sortByAverageTime());
    }

    public void removeNewAverageBestAnswers(String playerName, int questionsAnswered) {
        if(questionsAnswered < minimumQuestionsRequired) {
            return;
        }

        for (int i = 0; i < averageBestAnswers.size(); i++) {
            PlayerData currentPlayer = averageBestAnswers.get(i);
            if (currentPlayer.getPlayerName().equals(playerName)) {
                averageBestAnswers.remove(i);
                return;
            }
            Bukkit.getLogger().log(Level.SEVERE, "Player not found in DS for Average Answer Time");
        }
    }

    private void processStartUpInformation(
            PlayerData playerData, ArrayList<PlayerData> topPlayers, Comparator<PlayerData> comparator) {
        topPlayers.add(playerData);
        topPlayers.sort(comparator);
        if(topPlayers.size() > playersShown) {
            topPlayers.remove(playersShown);
        }
    }
    
   static class sortByBestTime implements Comparator<PlayerData> {
        public int compare (PlayerData player1, PlayerData player2) {
            int determinant = player2.getBestTime() - player1.getBestTime(); //Note inversion!
            if(determinant != 0) {
                return determinant;
            }
            return (player1.getQuestionsAnswered() - player2.getQuestionsAnswered());
       }
   }

    static class sortByMostAnswers implements Comparator<PlayerData> {
        public int compare (PlayerData player1, PlayerData player2) {
            int determinant = player1.getQuestionsAnswered() - player2.getQuestionsAnswered();
            if(determinant != 0) {
                return determinant;
            }
            return (player1.getBestTime() - player2.getBestTime());
        }
    }

    static class sortByAverageTime implements Comparator<PlayerData> {
        public int compare (PlayerData player1, PlayerData player2) {
            double determinant = player2.getBestTime() - player1.getBestTime(); //Note inversion!
            if(determinant > 0) {
                return 1;
            }
            if (determinant < 0) {
                return -1;
            }
            return (player1.getQuestionsAnswered() - player2.getQuestionsAnswered());
        }
    }
}
