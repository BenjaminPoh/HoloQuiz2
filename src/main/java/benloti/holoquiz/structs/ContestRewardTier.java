package benloti.holoquiz.structs;

import org.bukkit.inventory.ItemStack;

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
}
