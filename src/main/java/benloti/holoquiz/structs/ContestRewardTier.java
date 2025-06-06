package benloti.holoquiz.structs;

import benloti.holoquiz.files.UserInterface;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ContestRewardTier  {
    private final double moneyReward;
    private final List<String> commandsExecuted;
    private final ArrayList<ItemStack> itemRewards;
    private final String message;

    public ContestRewardTier(double money, List<String> commands, ArrayList<ItemStack> items, String message) {
        this.moneyReward = money;
        this.commandsExecuted = commands;
        this.itemRewards = items;
        this.message = message;
    }

    public ContestRewardTier(ContestWinner contestWinner, ContestInfo contestInfo, UserInterface userInterface) {
        ContestRewardTier template = contestWinner.getContestWinnerPrize();
        this.moneyReward = template.getMoneyReward();
        this.itemRewards = formatItemRewards(template.getItemRewards(), userInterface, contestWinner, contestInfo);
        this.message = formatMessage(template.getMessage(), userInterface, contestWinner, contestInfo);
        this.commandsExecuted = formatCommandsExecuted(template.getCommandsExecuted(), userInterface, contestWinner, contestInfo);
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

    public String getMessage() {
        return message;
    }

    private ArrayList<ItemStack> formatItemRewards(ArrayList<ItemStack> template, UserInterface userInterface, ContestWinner winner, ContestInfo info) {
        String playerName = winner.getContestWinnerData().getPlayerName();
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
                peko = userInterface.attachPlayerName(peko, playerName);
                peko = userInterface.attachContestStats(peko, winner, info);
                peko = userInterface.formatColours(peko);
                itemLoreFormatted.add(peko);
            }
            itemMeta.setLore(itemLoreFormatted);
            item.setItemMeta(itemMeta);
            formattedItemRewards.add(item);
        }
        return formattedItemRewards;
    }

    private String formatMessage(String message, UserInterface userInterface, ContestWinner winner, ContestInfo info) {
        String playerName = winner.getContestWinnerData().getPlayerName();
        String peko = message;
        peko = userInterface.attachPlayerName(peko, playerName);
        peko = userInterface.attachContestStats(peko, winner, info);
        peko = userInterface.formatColours(peko);
        return peko;
    }

    private ArrayList<String> formatCommandsExecuted(List<String> template, UserInterface userInterface, ContestWinner winner, ContestInfo info) {
        String playerName = winner.getContestWinnerData().getPlayerName();
        ArrayList<String> formattedList = new ArrayList<>();
        for(String peko : template) {
            peko = userInterface.attachPlayerName(peko, playerName);
            peko = userInterface.attachContestStats(peko, winner, info);
            peko = userInterface.formatColours(peko);
            formattedList.add(peko);
        }
        return formattedList;
    }
}
