package benloti.holoquiz.structs;

import benloti.holoquiz.files.MessageFormatter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ContestWinner {

    private final RewardTier contestPrize;
    private final PlayerContestStats contestWinnerData;
    private final int position;

    //Used to create the template
    public ContestWinner(RewardTier prizesTemplate, PlayerContestStats winner, int position, ContestInfo contestInfo) {
        this.contestWinnerData = winner;
        this.position = position;
        this.contestPrize = parseContestPrizes(prizesTemplate, contestInfo);
    }

    private RewardTier parseContestPrizes(RewardTier template, ContestInfo contestInfo) {
        int unusedTime = -1;
        double moneyReward = template.getMoneyReward();
        ArrayList<ItemStack> itemRewards = formatItemRewards(template.getItemRewards(), contestInfo);
        ArrayList<String> messageList = formatPlaceholders(template.getMessages(), contestInfo);
        List<String> commandList = formatPlaceholders(template.getCommandsExecuted(), contestInfo);
        return new RewardTier(unusedTime,moneyReward, commandList, itemRewards, messageList);
    }

    private ArrayList<ItemStack> formatItemRewards(ArrayList<ItemStack> template, ContestInfo info) {
        ArrayList<ItemStack> formattedItemRewards = new ArrayList<>();
        for(ItemStack unformattedItem : template) {
            ItemStack item = unformattedItem.clone(); //Important to prevent overwriting template
            ItemMeta itemMeta = item.getItemMeta();
            List<String> itemLore = itemMeta.getLore();
            if (itemLore == null) {
                continue;
            }
            List<String> itemLoreFormatted = new ArrayList<>();
            for (String peko : itemLore) {
                peko = attachContestStats(peko, info);
                peko = MessageFormatter.getSender().formatColours(peko);
                itemLoreFormatted.add(peko);
            }
            itemMeta.setLore(itemLoreFormatted);
            item.setItemMeta(itemMeta);
            formattedItemRewards.add(item);
        }
        return formattedItemRewards;
    }

    private ArrayList<String> formatPlaceholders(List<String> unformattedMessages, ContestInfo info) {
        ArrayList<String> formattedList = new ArrayList<>();
        for(String peko : unformattedMessages) {
            peko = attachContestStats(peko, info);
            peko = MessageFormatter.getSender().formatColours(peko);
            formattedList.add(peko);
        }
        return formattedList;
    }

    private String attachContestStats(String message, ContestInfo contestInfo) {
        if(message.contains("[player]")) {
            return message.replace("[player]", this.contestWinnerData.getPlayerName());
        }
        if(message.contains("[count]")) {
            String totalAnswers = String.valueOf(this.contestWinnerData.getQuestionsAnswered());
            message = message.replace("[count]", totalAnswers);
        }
        if(message.contains("[best]")) {
            String bestTime = this.contestWinnerData.getBestTimeInSeconds3DP();
            message = message.replace("[best]", bestTime);
        }
        if(message.contains("[avg]")) {
            String avgTime = this.contestWinnerData.getAverageTimeInSeconds3DP();
            message = message.replace("[avg]", avgTime);
        }
        if(message.contains("[month]")) {
            String monthOfContest = contestInfo.getStartDate().getMonth().toString();
            String formattedMonth = monthOfContest.charAt(0) + monthOfContest.substring(1).toLowerCase();
            message = message.replace("[month]", formattedMonth);
        }
        if(message.contains("[year]")) {
            String yearOfContest = Integer.toString(contestInfo.getStartDate().getYear());
            message = message.replace("[year]", yearOfContest);
        }
        if(message.contains("[start]")) {
            String startDateOfContest = contestInfo.getStartDate().toString();
            message = message.replace("[start]", startDateOfContest);
        }
        if(message.contains("[end]")) {
            String endDateOfContest = contestInfo.getEndDate().toString();
            message = message.replace("[end]", endDateOfContest);
        }
        if(message.contains("[pos]")) {
            String position = String.valueOf(this.position);
            message = message.replace("[pos]", position);
        }
        if(message.contains("[pos++]")) {
            String position = positionFormatter(this.position);
            message = message.replace("[pos++]", position);
        }
        return message;
    }

    private String positionFormatter(int i) {
        if(i == 1) {
            return i + "st";
        }
        if(i == 2) {
            return i + "nd";
        }
        if(i == 3) {
            return i + "rd";
        }
        return i + "th";
    }


    public RewardTier getContestWinnerPrize() {
        return contestPrize;
    }

    public PlayerContestStats getContestWinnerData() {
        return contestWinnerData;
    }

    public int getPosition() {
        return position;
    }

}
