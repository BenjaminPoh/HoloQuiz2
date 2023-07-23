package benloti.holoquiz.leaderboard;

import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.structs.PlayerData;

import java.util.ArrayList;
import java.util.Comparator;

public class Leaderboard {

    private final MostAnswers mostAnswers;
    private final FastestTime fastestTime;
    private final AverageBestTime averageBestTime;
    private final int amountOfPlayersToShow;
    private final int minimumQuestionsRequired;

    public Leaderboard(ConfigFile configFile, DatabaseManager databaseManager) {
        this.amountOfPlayersToShow = configFile.getLeaderboardSize();
        this.minimumQuestionsRequired = configFile.getLeaderboardMinReq();
        this.mostAnswers = new MostAnswers();
        this.fastestTime = new FastestTime();
        this.averageBestTime = new AverageBestTime();
        initialiseLeaderboard(databaseManager);
    }

    public int getAmountOfPlayersToShow() {
        return amountOfPlayersToShow;
    }

    public void initialiseLeaderboard(DatabaseManager databaseManager) {
        ArrayList<PlayerData> allPlayerData = databaseManager.loadAllPlayerData();
        for(PlayerData peko : allPlayerData) {
            if(peko.getQuestionsAnswered() < minimumQuestionsRequired) {
                continue;
            }

            processStartUpInformation(peko, mostAnswers.getTopPlayers(), new MostAnswers.sortByMostAnswers());
            processStartUpInformation(peko, fastestTime.getTopPlayers(), new FastestTime.sortByBestTime());
            averageBestTime.addToTopPlayers(peko);
        }
        averageBestTime.getTopPlayers().sort(new AverageBestTime.sortByAverageTime());
    }

    private void processStartUpInformation(
            PlayerData playerData, ArrayList<PlayerData> topPlayers, Comparator<PlayerData> comparator) {
        topPlayers.add(playerData);
        topPlayers.sort(comparator);
        if(topPlayers.size() > amountOfPlayersToShow) {
            topPlayers.remove(amountOfPlayersToShow);
        }
    }

    public void updateLeaderBoard(PlayerData playerData) {
        if(playerData.getQuestionsAnswered() < minimumQuestionsRequired) {
            return;
        }

        mostAnswers.updateTopPlayers(playerData, amountOfPlayersToShow);
        fastestTime.updateTopPlayers(playerData, amountOfPlayersToShow);

        if(playerData.getQuestionsAnswered() == minimumQuestionsRequired) {
            averageBestTime.addToTopPlayers(playerData);
        }
        averageBestTime.updateTopPlayers(playerData);

    }

    public ArrayList<PlayerData> getFastest() {
        return fastestTime.getTopPlayers();
    }

    public ArrayList<PlayerData> getMostAnswers() {
        return mostAnswers.getTopPlayers();
    }

    public ArrayList<PlayerData> getAverageBest() {
        return averageBestTime.getTopPlayers();
    }
}

