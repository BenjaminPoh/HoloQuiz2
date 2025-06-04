package benloti.holoquiz.structs;

import org.bukkit.inventory.ItemStack;

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
}
