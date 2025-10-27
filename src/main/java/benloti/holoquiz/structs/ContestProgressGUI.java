package benloti.holoquiz.structs;

import benloti.holoquiz.files.ContestManager;
import benloti.holoquiz.files.MessageFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContestProgressGUI {
    private Inventory inventory;
    private int nextIndex = 0;
    private int leaderboardMaxSize;
    private String playerName;

    private static final String DESCRIPTION_FOR_PLAYER_DQ = "&c%s. %s | %s";
    private static final String DESCRIPTION_FOR_PLAYER = "&a%s. %s | %s";
    private static final String DESCRIPTION_FOR_OTHERS = "&3%s. %s | %s";
    private static final String DESCRIPTION_REASON_FOR_PLAYER_DQ = "&4You need &c%d &4more questions to qualify!";
    private static final String DESCRIPTION_TIME_TO_IMPROVE = "&2You need to be faster than &a%s &2to improve your score!";
    private static final String DESCRIPTION_LEADERBOARD_OVERFLOW = "&3+%d more players...";

    public ContestProgressGUI(ContestManager contestManager, String playerName) {
        int size = contestManager.getTotalEnabledSubcontests();
        size = ((size + 8)/ 9) * 9;
        this.inventory = Bukkit.createInventory(null, size, "HoloQuiz Contests");
        this.playerName = playerName;
        this.leaderboardMaxSize = contestManager.getContestLeaderboardMaxSize();
    }

    public Inventory getGUI() {
        return this.inventory;
    }

    //Adds the info for all contest categories of a contest
    public void addContestInfo(ContestInfo contest, ArrayList<ArrayList<PlayerContestStats>> allContestWinners, PlayerContestStats playerInfo) {
        String dateRangeDescription = formatDateTime(contest.getStartDate(), contest.getEndDate());
        for(int i = 0; i < allContestWinners.size(); i++) {
            if(contest.getRewardByCategory(i).isEmpty()) {
                continue;
            }
            ArrayList<PlayerContestStats> currContestWinners = allContestWinners.get(i);
            ItemStack placeholderItem = new ItemStack(Material.PAPER, 1);
            ItemMeta itemMeta = placeholderItem.getItemMeta();
            assert itemMeta != null;
            String contestTitle = formatContestName(contest.getContestName(), i);
            itemMeta.setDisplayName(MessageFormatter.getSender().formatColours("&6" + contestTitle));
            List<String> description = new ArrayList<>();
            description.add(MessageFormatter.getSender().formatColours(dateRangeDescription));

            addLeaderboardInformation(contest, playerInfo, description, currContestWinners, i);

            itemMeta.setLore(description);
            placeholderItem.setItemMeta(itemMeta);
            this.inventory.setItem(nextIndex, placeholderItem);
            nextIndex++;
        }

    }

    //Adds information related to the leaderboard
    //All java coding standards are violated in this 1 function.
    private void addLeaderboardInformation(ContestInfo contest, PlayerContestStats playerInfo, List<String> description,
                                           ArrayList<PlayerContestStats> currContestWinners, int code) {
        int playerPlacement = getPlayerPlacementForContest(currContestWinners, playerName);
        for(int k = 0; k < currContestWinners.size(); k++) {
            PlayerContestStats winner = currContestWinners.get(k);
            int position = k + 1;
            if(position == leaderboardMaxSize) {
                //This spot is for the last person in the leaderboard, which is the player in last place.
                int remainder = currContestWinners.size() - leaderboardMaxSize;
                if(playerPlacement > k && playerPlacement < (currContestWinners.size() - 1)){
                    //Regardless, add the player if they are in leaderboard between them and Last Place
                    PlayerContestStats playerStats = currContestWinners.get(playerPlacement);
                    addContestLeaderboardEntry(playerStats, playerPlacement + 1 , code, description);
                    remainder--;
                }
                if(remainder > 0) {
                    if(remainder == 1) {
                        int secondLastPlace = currContestWinners.size() - 2;
                        addContestLeaderboardEntry(currContestWinners.get(secondLastPlace), secondLastPlace + 1, code, description);
                    } else {
                        String formattedDescription = String.format(DESCRIPTION_LEADERBOARD_OVERFLOW, remainder);
                        description.add(MessageFormatter.getSender().formatColours(formattedDescription));
                    }
                }
                int lastPlace = currContestWinners.size() - 1;
                addContestLeaderboardEntry(currContestWinners.get(lastPlace), lastPlace + 1, code, description);
                break;
            }
            addContestLeaderboardEntry(winner, k + 1, code, description);
        }
        if(playerPlacement == -1) {
            addDisqualifiedPlayerInfo(contest, playerInfo, code, description);
        }
        if(code == 3 && playerPlacement != -1) {
            addImprovementTimeInfo(playerInfo, description);
        }
    }

    private void addContestLeaderboardEntry(PlayerContestStats winner, int pos, int i, List<String> description) {
        String score = getScoreByIndex(winner, i);
        String descriptionFormat = DESCRIPTION_FOR_OTHERS;
        if(winner.getPlayerName().equals(playerName)) {
            descriptionFormat = DESCRIPTION_FOR_PLAYER;
        }
        String formattedDescription = String.format(descriptionFormat, pos, winner.getPlayerName(), score);
        description.add(MessageFormatter.getSender().formatColours(formattedDescription));
    }

    private int getPlayerPlacementForContest(ArrayList<PlayerContestStats> winners, String playerName) {
        for(int i = 0; i < winners.size(); i++) {
            if(winners.get(i).getPlayerName().equals(playerName)) {
                return i;
            }
        }
        return -1;
    }

    private void addDisqualifiedPlayerInfo(ContestInfo contest, PlayerContestStats playerInfo, int i, List<String> description) {
        String score = getScoreByIndex(playerInfo, i);
        String formattedDescription = String.format(DESCRIPTION_FOR_PLAYER_DQ, "N/A", playerInfo.getPlayerName(), score);
        description.add(MessageFormatter.getSender().formatColours(formattedDescription));
        if(i == 2 && playerInfo.getQuestionsAnswered() < contest.getBestAvgMinReq()) {
            int remainder =  contest.getBestAvgMinReq() - playerInfo.getQuestionsAnswered();
            formattedDescription = String.format(DESCRIPTION_REASON_FOR_PLAYER_DQ, remainder);
            description.add(MessageFormatter.getSender().formatColours(formattedDescription));
        }
        if(i == 3 && playerInfo.getQuestionsAnswered() < contest.getBestXMinReq()) {
            int remainder =  contest.getBestXMinReq() - playerInfo.getQuestionsAnswered();
            formattedDescription = String.format(DESCRIPTION_REASON_FOR_PLAYER_DQ, remainder);
            description.add(MessageFormatter.getSender().formatColours(formattedDescription));
        }
    }

    private void addImprovementTimeInfo(PlayerContestStats playerInfo, List<String> description) {
        String timeToImprove = playerInfo.getTimeToImproveInSeconds3dp();
        String formattedDescription = String.format(DESCRIPTION_TIME_TO_IMPROVE, timeToImprove);
        description.add(MessageFormatter.getSender().formatColours(formattedDescription));
    }

    private String formatDateTime(LocalDate start, LocalDate end) {
        return String.format("&e%s - %s", start, end);
    }

    private String formatContestName(String contestType, int i) {
        if(i == 0) {
            return contestType + " Top";
        }
        if(i == 1) {
            return contestType + " Fastest";
        }
        if(i == 2) {
            return contestType + " Best Avg";
        }
        if(i == 3) {
            return contestType + " Best";
        }
        return "Bugged Contest :(";
    }

    private String getScoreByIndex(PlayerContestStats winner, int index) {
        if(index == 0) {
            return Integer.toString(winner.getQuestionsAnswered());
        }
        if(index == 1) {
            return winner.getBestTimeInSeconds3DP();
        }
        if (index == 2) {
            return winner.getAverageTimeInSeconds3DP();
        }
        if (index == 3) {
            return winner.getBestXTimesInSeconds3DP();
        }
        return "Error?";
    }

}
