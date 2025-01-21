package benloti.holoquiz.database;

import benloti.holoquiz.games.RewardsHandler;
import benloti.holoquiz.structs.ContestInfo;
import benloti.holoquiz.structs.PlayerData;
import benloti.holoquiz.structs.RewardTier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*; //Blasphemy
import java.util.ArrayList;
import java.util.HashMap;
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

    /**
     * In version 1.3.0, Database was completely rewritten
     * This allowed for leaderboards to be maintained without funny tricks.
     */
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

    public ArrayList<ContestInfo> updateOngoingContestInformation(ArrayList<ContestInfo> enabledContests) {
        ArrayList<ContestInfo> updatedOngoingContests = new ArrayList<>();
        ArrayList<ContestInfo> ongoingContests = contests.getOngoingTournaments(connection);
        //We do it clever.
        HashMap<String, ContestInfo> enabledContestFlag = new HashMap<>();
        HashMap<String, ContestInfo> ongoingContestFlag = new HashMap<>();
        for (ContestInfo enabledContest : enabledContests) {
            enabledContestFlag.put(enabledContest.getType(), enabledContest);
        }
        for (ContestInfo ongoingContest : ongoingContests) {
           ongoingContestFlag.put(ongoingContest.getType(), ongoingContest);
        }
        for(String code : enabledContestFlag.keySet() ) {
            if(enabledContestFlag.containsKey(code) && !ongoingContestFlag.containsKey(code)) {
                //Add new contest to db and the return list
                ContestInfo newContestInfo = enabledContestFlag.get(code);
                contests.createOngoingContest(connection, newContestInfo);
                updatedOngoingContests.add(newContestInfo);
            } else if (!enabledContestFlag.containsKey(code) && ongoingContestFlag.containsKey(code)) {
                //Delete disabled contest.
                contests.deleteOngoingContest(connection, code);
            } else if (enabledContestFlag.containsKey(code) && ongoingContestFlag.containsKey(code)) {
                //Add ongoing contest to the return list
                ContestInfo configContestInfo = enabledContestFlag.get(code);
                ContestInfo storedContestInfo = ongoingContestFlag.get(code);
                configContestInfo.updateContestTimes(storedContestInfo.getStartTime(), storedContestInfo.getEndTime());
                updatedOngoingContests.add(configContestInfo);
            }
        }
        return updatedOngoingContests;
    }

    public Map<String, ArrayList<PlayerData>> executeContestEndedTasks(ContestInfo endedContest, long nextContestTime, boolean isMultipleWinsAllowed) {
        connection = getConnection();
        long startTime = endedContest.getStartTime();
        long endTime = endedContest.getEndTime();
        int minAns = endedContest.getMinAnswersNeeded();
        //Fetch All the Winners
        ArrayList<PlayerData> mostAnswerWinners = answersLogs.getTopAnswerersWithinTimestamp(connection,
                startTime, endTime, endedContest.getTopAnswerPlacements());
        ArrayList<PlayerData> fastestAnswerWinners;
        if (isMultipleWinsAllowed) {
            fastestAnswerWinners = answersLogs.getFastestAnswerersWithinTimestamp(connection,
                    startTime, endTime, endedContest.getFastestAnswerPlacements());
        } else {
            fastestAnswerWinners = answersLogs.getFastestAnswerersWithinTimestampNoRepeat(connection,
                    startTime, endTime, endedContest.getFastestAnswerPlacements());
        }

        ArrayList<PlayerData> bestAverageWinners = answersLogs.getBestAnswerersWithinTimestamp(connection,
                startTime, endTime, endedContest.getBestAverageAnswerPlacements(), minAns);

        //Update Contests
        contests.updateContestInfo(connection, endedContest, nextContestTime);

        //Log Winners
        contests.addContestWinners(connection, mostAnswerWinners, endedContest, "M");
        contests.addContestWinners(connection, fastestAnswerWinners, endedContest, "F");
        contests.addContestWinners(connection, bestAverageWinners, endedContest, "B");

        //Return ArrayLists for issuing rewards
        Map<String, ArrayList<PlayerData>> allContestWinners = new HashMap<>();
        allContestWinners.put("M", mostAnswerWinners);
        allContestWinners.put("F", fastestAnswerWinners);
        allContestWinners.put("B", bestAverageWinners);
        return allContestWinners;
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
}
