package benloti.holoquiz.structs;

import benloti.holoquiz.files.ContestManager;
import benloti.holoquiz.files.UserInterface;
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
    private String playerName;
    private UserInterface userInterface;

    private static final String DESCRIPTION_FOR_PLAYER_DQ = "&c%s. %s | %s";
    private static final String DESCRIPTION_FOR_PLAYER = "&a%s. %s | %s";
    private static final String DESCRIPTION_FOR_OTHERS = "&3%s. %s | %s";
    private static final String DESCRIPTION_REASON_FOR_PLAYER_DQ = "&4You need &c%d &4more questions to qualify!";

    public ContestProgressGUI(ContestManager contestManager, String playerName, UserInterface userInterface) {
        int size = contestManager.getTotalEnabledSubcontests();
        size = ((size + 8)/ 9) * 9;
        this.inventory = Bukkit.createInventory(null, size, "HoloQuiz Contests");
        this.playerName = playerName;
        this.userInterface = userInterface;
    }

    public Inventory getGUI() {
        return this.inventory;
    }

    //All java coding standards are violated in this 1 function.
    public void addInfo(ContestInfo contest, ArrayList<ArrayList<PlayerContestStats>> allContestWinners, PlayerContestStats playerInfo) {
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
            itemMeta.setDisplayName(userInterface.formatColours("&6" + contestTitle));
            List<String> description = new ArrayList<>();
            description.add(userInterface.formatColours(dateRangeDescription));
            boolean playerFound = false;
            for(int k = 0; k < currContestWinners.size(); k++) {
                PlayerContestStats winner = currContestWinners.get(k);
                String score = getScoreByIndex(winner, i);
                String descriptionFormat = DESCRIPTION_FOR_OTHERS;
                if(winner.getPlayerName().equals(playerName)) {
                    descriptionFormat = DESCRIPTION_FOR_PLAYER;
                    playerFound = true;
                }
                String formattedDescription = String.format(descriptionFormat, k + 1, winner.getPlayerName(), score);
                description.add(userInterface.formatColours(formattedDescription));
            }
            if(!playerFound) {
                String score = getScoreByIndex(playerInfo, i);
                String formattedDescription = String.format(DESCRIPTION_FOR_PLAYER_DQ, "N/A", playerInfo.getPlayerName(), score);
                description.add(userInterface.formatColours(formattedDescription));
                if(i == 2 && playerInfo.getQuestionsAnswered() < contest.getBestAvgMinReq()) {
                    int remainder =  contest.getBestAvgMinReq() - playerInfo.getQuestionsAnswered();
                    formattedDescription = String.format(DESCRIPTION_REASON_FOR_PLAYER_DQ, remainder);
                    description.add(userInterface.formatColours(formattedDescription));
                }
                if(i == 3 && playerInfo.getQuestionsAnswered() < contest.getBestXMinReq()) {
                    int remainder =  contest.getBestXMinReq() - playerInfo.getQuestionsAnswered();
                    formattedDescription = String.format(DESCRIPTION_REASON_FOR_PLAYER_DQ, remainder);
                    description.add(userInterface.formatColours(formattedDescription));
                }
            }
            itemMeta.setLore(description);
            placeholderItem.setItemMeta(itemMeta);
            this.inventory.setItem(nextIndex, placeholderItem);
            nextIndex++;
        }

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
