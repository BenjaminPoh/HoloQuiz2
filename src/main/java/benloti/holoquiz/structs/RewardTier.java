package benloti.holoquiz.structs;

import benloti.holoquiz.files.UserInterface;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RewardTier {
    private final int maxTimeInMilliseconds;
    private final double moneyReward;
    private final List<String> commandsExecuted;
    private final ArrayList<String> messagesSent;
    private final ArrayList<ItemStack> itemRewards;

    public RewardTier(int time, double money, List<String> commands, ArrayList<ItemStack> items, ArrayList<String> messages) {
        this.maxTimeInMilliseconds = time;
        this.moneyReward = money;
        this.commandsExecuted = commands;
        this.itemRewards = items;
        this.messagesSent = messages;
    }

    public RewardTier(RewardTier template, ContestWinner contestWinner, ContestInfo contestInfo, UserInterface userInterface) {
        this.maxTimeInMilliseconds = -1;
        this.moneyReward = template.getMoneyReward();
        this.itemRewards = formatItemRewards(template.getItemRewards(), userInterface, contestWinner, contestInfo);
        this.messagesSent = formatPlaceholders(template.getMessages(), userInterface, contestWinner, contestInfo);
        this.commandsExecuted = formatPlaceholders(template.getCommandsExecuted(), userInterface, contestWinner, contestInfo);
    }

    public int getMaxTimeInMilliseconds() {
        return maxTimeInMilliseconds;
    }

    public double getMoneyReward() {
        return moneyReward;
    }

    public List<String> getCommandsExecuted() {
        return commandsExecuted;
    }

    public ArrayList<ItemStack> getItemRewards() {
        return itemRewards;
    }

    public ArrayList<String> getMessages() {
        return messagesSent;
    }

    public boolean checkIfRewardPresent() {
        return (commandsExecuted.isEmpty() && itemRewards.isEmpty() && moneyReward == 0 && messagesSent.isEmpty());
    }

    private ArrayList<ItemStack> formatItemRewards(ArrayList<ItemStack> template, UserInterface userInterface, ContestWinner winner, ContestInfo info) {
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
                peko = attachContestStats(peko, winner, info);
                peko = userInterface.formatColours(peko);
                itemLoreFormatted.add(peko);
            }
            itemMeta.setLore(itemLoreFormatted);
            item.setItemMeta(itemMeta);
            formattedItemRewards.add(item);
        }
        return formattedItemRewards;
    }

    private ArrayList<String> formatPlaceholders(List<String> unformattedMessages, UserInterface userInterface, ContestWinner winner, ContestInfo info) {
        ArrayList<String> formattedList = new ArrayList<>();
        for(String peko : unformattedMessages) {
            peko = attachContestStats(peko, winner, info);
            peko = userInterface.formatColours(peko);
            formattedList.add(peko);
        }
        return formattedList;
    }

    private String attachContestStats(String message, ContestWinner contestWinner, ContestInfo contestInfo) {
        if(message.contains("[player]")) {
            return message.replace("[player]", contestWinner.getContestWinnerData().getPlayerName());
        }
        if(message.contains("[count]")) {
            String totalAnswers = String.valueOf(contestWinner.getContestWinnerData().getQuestionsAnswered());
            message = message.replace("[count]", totalAnswers);
        }
        if(message.contains("[best]")) {
            String bestTime = contestWinner.getContestWinnerData().getBestTimeInSeconds3DP();
            message = message.replace("[best]", bestTime);
        }
        if(message.contains("[avg]")) {
            String avgTime = contestWinner.getContestWinnerData().getAverageTimeInSeconds3DP();
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
            String position = String.valueOf(contestWinner.getPosition());
            message = message.replace("[pos]", position);
        }
        if(message.contains("[pos++]")) {
            String position = positionFormatter(contestWinner.getPosition());
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

}
