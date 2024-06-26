package benloti.holoquiz.structs;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RewardTier {
    private final int maxTimeInMilliseconds;
    private final double moneyReward;
    private final List<String> commandsExecuted;
    private final ArrayList<ItemStack> itemRewards;

    public RewardTier(int time, double money, List<String> commands, ArrayList<ItemStack> items) {
        this.maxTimeInMilliseconds = time;
        this.moneyReward = money;
        this.commandsExecuted = commands;
        this.itemRewards = items;
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

    public boolean checkIfRewardPresent() {
        return (commandsExecuted.isEmpty() && itemRewards.isEmpty() && moneyReward == 0);
    }
}
