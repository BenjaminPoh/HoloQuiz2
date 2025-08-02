package benloti.holoquiz.database;

import benloti.holoquiz.games.RewardsHandler;
import benloti.holoquiz.structs.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*; //Blasphemy
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class DatabaseManager {
    private static final String DB_NAME = "HoloQuiz.db";
    private static final String ERROR_MSG_DB_FILE = "[HoloQuiz] Database File is bugged!";

    private static final String SQL_STATEMENT_FETCH_ALL_LOGS =
            "SELECT * FROM answers_logs";
    private static final String SQL_STATEMENT_UPDATE_STATS =
            "UPDATE holoquiz_stats SET best = ?, answers = ?, total = ?, average = ? WHERE user_id = ?";
    public static final String ERROR_HOLOQUIZ_ID_NOT_FOUND = "[HoloQuiz] Error: Player doesn't exist. You should NOT see this.";

    private Connection connection;
    private RewardsHandler rewardsHandler;
    private final JavaPlugin plugin;
    private final File dataFile;
    private final HoloQuizStats holoQuizStats;
    private final AnswersLogs answersLogs;
    private final UserInfo userInfo;
    private final UserPersonalisation userPersonalisation;
    private final Contests contests;
    private final Storage storage;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = checkFile();
        this.connection = getConnection();

        this.holoQuizStats = new HoloQuizStats(connection, this);
        this.answersLogs = new AnswersLogs(connection, this);
        this.userInfo = new UserInfo(connection);
        this.userPersonalisation = new UserPersonalisation(connection);
        this.contests = new Contests(connection);
        this.storage = new Storage(connection);
    }

    public File checkFile() {
        File dataFile = new File(plugin.getDataFolder(), DB_NAME);
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().info(ERROR_MSG_DB_FILE);
                e.printStackTrace();
            }
        }
        return dataFile;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Bukkit.getLogger().info("[HoloQuiz] Making new SQL connection!");
                // Establish a database connection
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile);
                if (connection == null || connection.isClosed()) {
                    Bukkit.getLogger().log(Level.SEVERE, ERROR_MSG_DB_FILE);
                    return null;
                }
                Bukkit.getLogger().info("[HoloQuiz] New SQL connection established!");
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Is called after players give an accepted answer.
     * Begins by obtaining the player's HoloQuiz ID given their name.
     * Next, updates the answer logs and the players stats in the database.
     *
     * @param player       The player who answered correctly
     * @param timeAnswered UNIX timestamp of when the player answered
     * @param timeTaken    Time taken for player to answer, in milliseconds
     * @return The player's PlayerData, to update the leaderboards
     */
    public PlayerData updateAfterCorrectAnswer(Player player, long timeAnswered, int timeTaken, String gameMode) {
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();
        connection = getConnection();
        int playerHoloQuizID = userInfo.getHoloQuizIDByUUID(connection, playerUUID, playerName);
        if (playerHoloQuizID == 0) {
            Bukkit.getLogger().info(ERROR_HOLOQUIZ_ID_NOT_FOUND);
            return null;
        }
        answersLogs.updateLogsRecord(connection, playerHoloQuizID, timeAnswered, timeTaken, gameMode);
        return holoQuizStats.updateStatsRecord(connection, playerHoloQuizID, timeTaken, playerName);
    }

    /**
     * Fetches the player's PlayerData from the database
     * Begins by obtaining the player's HoloQuiz ID given their name.
     * Next, obtains the player's information from the database
     *
     * @param playerName The player's name
     * @return The player's PlayerData
     */
    public PlayerData loadPlayerData(String playerName) {
        connection = getConnection();
        int playerHoloQuizID = userInfo.getHoloQuizIDByUserName(connection, playerName);
        if (playerHoloQuizID == 0) {
            return null;
        }
        return holoQuizStats.loadPlayerData(connection, playerHoloQuizID, playerName);
    }

    public UserPersonalisation getUserPersonalisation() {
        return this.userPersonalisation;
    }

    /**
     * Recomputes holoquiz_stats based on the information in answers_logs.
     */
    public int reloadDatabase() {
        connection = getConnection();
        HashMap<Integer, Long> timeRecord = new HashMap<>();
        HashMap<Integer, Integer> bestRecord = new HashMap<>();
        HashMap<Integer, Integer> totalAnsRecord = new HashMap<>();
        try {
            PreparedStatement fetchPlayerStatsQuery = connection.prepareStatement(SQL_STATEMENT_FETCH_ALL_LOGS);
            ResultSet resultSet = fetchPlayerStatsQuery.executeQuery();
            //First read the logs
            while (resultSet.next()) {
                int timeTaken = resultSet.getInt("took");
                int holoQuizID = resultSet.getInt("user_id");
                if(timeRecord.containsKey(holoQuizID)) {
                    timeRecord.put(holoQuizID, timeTaken + timeRecord.get(holoQuizID));
                    totalAnsRecord.put(holoQuizID, 1 + totalAnsRecord.get(holoQuizID));
                    int tempBest = bestRecord.get(holoQuizID);
                    if(tempBest > timeTaken) {
                        bestRecord.put(holoQuizID, timeTaken);
                    }
                } else {
                    timeRecord.put(holoQuizID, (long) timeTaken);
                    bestRecord.put(holoQuizID, timeTaken);
                    totalAnsRecord.put(holoQuizID, 1);
                }
            }
            //Then recompute
            int size = 0;
            for(Map.Entry<Integer, Long> keyValueSet : timeRecord.entrySet()) {
                size++;
                int totalAnswers = totalAnsRecord.get(keyValueSet.getKey());
                long totalTimeTaken = keyValueSet.getValue();
                int averageTimeTaken = (int)(totalTimeTaken / totalAnswers);
                PreparedStatement insertStatsQuery = connection.prepareStatement(SQL_STATEMENT_UPDATE_STATS);
                insertStatsQuery.setInt(1, bestRecord.get(keyValueSet.getKey()));
                insertStatsQuery.setInt(2, totalAnswers);
                insertStatsQuery.setLong(3,totalTimeTaken);
                insertStatsQuery.setInt(4, averageTimeTaken);
                insertStatsQuery.setInt(5, keyValueSet.getKey());
                insertStatsQuery.executeUpdate();
            }
            return size;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public ArrayList<PlayerData> loadLeaderboard(int size, int minReq, String column, boolean order) {
        ArrayList<PlayerData> leaderboardList = new ArrayList<>();
        String formattedColumn = column;
        if(order) {
            formattedColumn += " ASC";
        } else {
            formattedColumn += " DESC";
        }
        holoQuizStats.getLeaderboardForColumn(connection, formattedColumn, size, minReq, leaderboardList);
        return leaderboardList;
    }

    public String getPlayerNameByHoloQuizID (Connection connection, int holoQuizID) {
        return userInfo.getPlayerNameByHoloQuizID(connection, holoQuizID);
    }

    public ArrayList<ArrayList<PlayerContestStats>> fetchContestWinners(ContestInfo endedContest) {
        if(endedContest == null) {
            return new ArrayList<>();
        }
        connection = getConnection();
        long startTime = endedContest.getStartTime();
        long endTime = endedContest.getEndTime();
        int minAnsForBestAvg = endedContest.getBestAvgMinReq();
        int minAnsForBestX = endedContest.getBestXMinReq();

        //Fetch All the Winners
        ArrayList<PlayerContestStats> mostAnswerWinners = answersLogs.getTopAnswerersWithinTimestamp(connection,
                startTime, endTime, endedContest.getRewardCountByCategory(0));

        ArrayList<PlayerContestStats> fastestAnswerWinners = answersLogs.getFastestAnswerersWithinTimestamp(connection,
                    startTime, endTime, endedContest.getRewardCountByCategory(1));

        ArrayList<PlayerContestStats> bestAverageWinners = answersLogs.getBestAnswerersWithinTimestamp(connection,
                startTime, endTime, endedContest.getRewardCountByCategory(2), minAnsForBestAvg);

        ArrayList<PlayerContestStats> bestXWinners = answersLogs.getBestXWithinTimestamp(connection,
                startTime, endTime, endedContest.getRewardCountByCategory(3), minAnsForBestX);


        //Return ArrayLists for issuing rewards
        ArrayList<ArrayList<PlayerContestStats>> fullWinnersList = new ArrayList<>(3);
        fullWinnersList.add(mostAnswerWinners);
        fullWinnersList.add(fastestAnswerWinners);
        fullWinnersList.add(bestAverageWinners);
        fullWinnersList.add(bestXWinners);
        return fullWinnersList;
    }

    public void storeRewardToStorage(String playerName, String type, String contents, String metaDetails, int count) {
        connection = getConnection();
        int playerHoloQuizID = userInfo.getHoloQuizIDByUserName(connection, playerName);
        if (playerHoloQuizID == 0) {
            Bukkit.getLogger().info(ERROR_HOLOQUIZ_ID_NOT_FOUND);
            return;
        }
        storage.addToStorage(connection, playerHoloQuizID, type, contents, metaDetails, count);
    }

    public void setRewardsHandler(RewardsHandler rewardsHandler) {
        this.rewardsHandler = rewardsHandler;
    }

    /**
     * Fetches stored rewards, and issues as much as inventory space allows.
     *
     * @param player the player
     * @return  -2 if there are no rewards present
     *          -1 if a rewardTier is null, which should be impossible given it's from storage.
     *          0 if all rewards are issued
     *          1 if the storage is Full
     *          2 if SRTS Overwrite is triggered
     */
    public int getRewardsFromStorage(Player player) {
        connection = getConnection();
        int playerHoloQuizID = userInfo.getHoloQuizIDByUserName(connection, player.getName());
        RewardTier storedRewards = storage.retrieveFromStorage(connection, playerHoloQuizID);
        if(storedRewards.checkIfRewardPresent()) {
            return -2;
        }
        return rewardsHandler.giveRewardsByTier(player, storedRewards);
    }

    public void logContestWinners(ArrayList<ArrayList<PlayerContestStats>> allContestWinners, ContestInfo endedContest) {
        contests.logContestWinners(connection, allContestWinners.get(0), endedContest, 0);
        contests.logContestWinners(connection, allContestWinners.get(1), endedContest, 1);
        contests.logContestWinners(connection, allContestWinners.get(2), endedContest, 2);
        contests.logContestWinners(connection, allContestWinners.get(3), endedContest, 3);
    }

    public PlayerContestStats fetchPlayerContestPlacement(ContestInfo contest, String playerName, String playerUUID) {
        int holoQuizID = getHoloQuizIDbyName(playerName, playerUUID);
        if(holoQuizID == 0) {
            return null;
        }
        connection = getConnection();
        long startTime = contest.getStartTime();
        long endTime = contest.getEndTime();
        return answersLogs.getPlayerStatsWithinTimestamp(connection, startTime, endTime, holoQuizID, contest.getBestXMinReq(), playerName);
    }

    public List<Double> fetchPrevTimes(int count, Player player) {
        int holoQuizID = getHoloQuizIDbyName(player.getName(), player.getUniqueId().toString());
        return answersLogs.getPreviousTimings(connection, holoQuizID, count);
    }

    public int getHoloQuizIDbyName(String playerName, String playerUUID) {
        connection = getConnection();
        int holoQuizID = userInfo.getHoloQuizIDByUUID(connection, playerUUID, playerName);
        if(holoQuizID == 0) {
            Bukkit.getLogger().info(ERROR_HOLOQUIZ_ID_NOT_FOUND);
        }
        return holoQuizID;
    }
}
