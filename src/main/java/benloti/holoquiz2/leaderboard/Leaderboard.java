package benloti.holoquiz2.leaderboard;

import benloti.holoquiz2.database.DatabaseManager;
import benloti.holoquiz2.structs.PlayerData;

import java.util.ArrayList;
import java.util.Comparator;

public class Leaderboard {

    private final MostAnswers mostAnswers;
    private final FastestTime fastestTime;
    private final AverageBestTime averageBestTime;
    private final int amountOfPlayersToShow;
    private final int minimumQuestionsRequired;

    public Leaderboard(int limit, int min, DatabaseManager databaseManager) {
        this.amountOfPlayersToShow = limit;
        this.minimumQuestionsRequired = min;
        mostAnswers = new MostAnswers();
        fastestTime = new FastestTime();
        averageBestTime = new AverageBestTime();
        initialiseLeaderboard(databaseManager);
    }

    public void initialiseLeaderboard(DatabaseManager databaseManager) {
        ArrayList<PlayerData> allPlayerData = databaseManager.loadAllPlayerData();
        for(PlayerData peko : allPlayerData) {
            processStartUpInformation(peko, mostAnswers.getTopPlayers(), new MostAnswers.sortByMostAnswers());
            processStartUpInformation(peko, fastestTime.getTopPlayers(), new FastestTime.sortByBestTime());
            processStartUpInformation(peko, averageBestTime.getTopPlayers(), new AverageBestTime.sortByAverageTime());
        }
    }

    private void processStartUpInformation(
            PlayerData playerData, ArrayList<PlayerData> topPlayers, Comparator<PlayerData> comparator) {
        topPlayers.add(playerData);
        topPlayers.sort(comparator);
        if (topPlayers.size() > amountOfPlayersToShow) {
            topPlayers.remove(amountOfPlayersToShow);
        }
    }

    public void updateLeaderBoard(PlayerData playerData) {
        mostAnswers.updateTopPlayers(playerData);
        fastestTime.updateTopPlayers(playerData);
        averageBestTime.updateTopPlayers(playerData, minimumQuestionsRequired);
    }

    public void playerJoinUpdate(PlayerData playerData) {
        if(playerData.getQuestionsAnswered() < minimumQuestionsRequired) {
            return;
        }
        averageBestTime.addNewAverageBestAnswers(playerData);
    }

    public void playerLeftUpdate(PlayerData playerData) {
        if(playerData.getQuestionsAnswered() < minimumQuestionsRequired) {
            return;
        }
        averageBestTime.removeNewAverageBestAnswers(playerData.getPlayerName());
    }
}

