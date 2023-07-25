package benloti.holoquiz.structs;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RewardTier {
    private int maxTimeInMilliseconds;
    private final int moneyReward;
    private  List<String> commandsExecuted;
    private ArrayList<ItemStack> itemRewards;

    public RewardTier(int time, int money, List<String> commands, ArrayList<ItemStack> items) {
        this.maxTimeInMilliseconds = time;
        this.moneyReward = money;
        this.commandsExecuted = commands;
        this.itemRewards = items;
    }


    public int getMaxTimeInMilliseconds() {
        return maxTimeInMilliseconds;
    }

    public int getMoneyReward() {
        return moneyReward;
    }

    public List<String> getCommandsExecuted() {
        return commandsExecuted;
    }

    public ArrayList<ItemStack> getItemRewards() {
        return itemRewards;
    }
}
