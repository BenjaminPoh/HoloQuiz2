package benloti.holoquiz.games;

import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.dependencies.VaultDep;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.files.ExternalFiles;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.structs.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RewardsHandler {

    private final ArrayList<RewardTier> allRewards;
    private final ArrayList<RewardTier> secretRewards;
    private final UserInterface userInterface;
    private final VaultDep vaultDep;
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;

    private final boolean SRTS_isWhitelist;
    private final List<String> SRTS_worldList;

    public RewardsHandler(JavaPlugin plugin, UserInterface userInterface, VaultDep vaultDep, DatabaseManager databaseManager,
                          ExternalFiles externalFiles, ConfigFile configFile) {
        this.allRewards = externalFiles.getAllNormalRewards();
        this.secretRewards = externalFiles.getAllSecretRewards();
        this.SRTS_isWhitelist = configFile.isSRTS_useWhitelist();
        this.SRTS_worldList = configFile.getSRTS_WorldList();

        this.userInterface = userInterface;
        this.vaultDep = vaultDep;
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        databaseManager.setRewardsHandler(this);
    }

    public int giveNormalRewards(Player player, int timeTaken) {
        RewardTier rewardTier = determineRewardTier(timeTaken, allRewards);
        return giveRewardsByTier(player, rewardTier);
    }

    public int giveSecretRewards(Player player, int timeTaken) {
        RewardTier rewardTier = determineRewardTier(timeTaken, secretRewards);
        return giveRewardsByTier(player, rewardTier);
    }

    public void giveContestRewards(ArrayList<ContestWinner> allContestWinners, ContestInfo contestInfo) {
        for(ContestWinner contestWinner : allContestWinners) {
            PlayerData contestWinnerData = contestWinner.getContestWinnerData();
            String playerName = contestWinnerData.getPlayerName();
            Player winningPlayer = plugin.getServer().getPlayer(playerName);
            if (winningPlayer == null || checkSRTS(winningPlayer.getWorld().getName())) {
                storeContestRewardToStorage(playerName, contestWinner);
            } else {
                issueContestReward(winningPlayer, contestWinner, contestInfo);
            }
        }
    }

    /**
     * Issues rewards, then sends a status code based on the result.
     *
     * @param player The player
     * @param rewardTier The RewardTier
     * @return -1 if a rewardTier is null -> No item rewards to be given
     *          0 if all rewards are issued
     *          1 if the storage is Full
     *          2 if SRTS Overwrite is triggered
     */
    public int giveRewardsByTier(Player player, RewardTier rewardTier) {
        if (rewardTier == null) {
            return -1;
        }

        int statusCode = giveItemRewards(player, rewardTier.getItemRewards());
        giveMoneyRewards(player, rewardTier.getMoneyReward());
        executeCommandRewards(player, rewardTier.getCommandsExecuted());
        userInterface.sendMessages(player, rewardTier.getMessages());
        return statusCode;
    }

    private void storeContestRewardToStorage(String playerName, ContestWinner contestWinner) {
        double money = contestWinner.getContestWinnerPrize().getMoneyReward();
        if(money != 0) {
            databaseManager.storeRewardToStorage(playerName, "V", String.valueOf(money), "", 1);
        }
        String message = contestWinner.getContestWinnerPrize().getMessage();
        if(!message.isEmpty()) {
            databaseManager.storeRewardToStorage(playerName, "M", message, "", 1);
        }
        List<String> commands = contestWinner.getContestWinnerPrize().getCommandsExecuted();
        for(String command : commands) {
            databaseManager.storeRewardToStorage(playerName, "C", command, "", 1);
        }
        List<ItemStack> itemList = contestWinner.getContestWinnerPrize().getItemRewards();
        for(ItemStack item : itemList) {
            storeItemToStorage(playerName, item);
        }
    }

    private void issueContestReward(Player player, ContestWinner winner, ContestInfo contestInfo) {
        ContestRewardTier reward = winner.getContestWinnerPrize();
        if(reward == null) {
            return;
        }
        giveItemRewards(player, reward.getItemRewards(), winner, contestInfo);
        giveMoneyRewards(player, reward.getMoneyReward());
        executeCommandRewards(player, reward.getCommandsExecuted());
        String message = userInterface.formatColours(reward.getMessage());
        userInterface.attachSuffixAndSend(player, message);
    }

    private void storeItemToStorage(String name, ItemStack item) {
        List<String> itemLoreList = item.getItemMeta().getLore();
        String lore = "";
        if (itemLoreList != null) {
            lore = String.join("\n", itemLoreList);
        }
        databaseManager.storeRewardToStorage(name, "I", item.getType().toString(), lore, item.getAmount());
    }

    private void giveItemRewards(Player player, ArrayList<ItemStack> itemRewards,
                                    ContestWinner winnerStats, ContestInfo contestInfo) {
        for (ItemStack item : itemRewards) {
            ItemMeta itemMeta = item.getItemMeta();
            List<String> itemLore = itemMeta.getLore();
            if (itemLore == null) {
                continue;
            }
            List<String> itemLoreFormatted = new ArrayList<>();
            for (String peko : itemLore) {
                peko = userInterface.attachPlayerName(peko, player);
                peko = userInterface.attachContestStats(peko, winnerStats, contestInfo);
                peko = userInterface.formatColours(peko);
                itemLoreFormatted.add(peko);
            }
            itemMeta.setLore(itemLoreFormatted);
            item.setItemMeta(itemMeta);
            HashMap<Integer, ItemStack> notAddedItem = player.getInventory().addItem(item);
            if(!notAddedItem.isEmpty()) {
                storeItemToStorage(player.getName(), item);
            }
            //Stupidly reset itemMeta
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);
        }
    }

    private int giveItemRewards(Player player, ArrayList<ItemStack> itemRewards) {
        boolean fullInvDetected = false;
        boolean forceSendRewardToStorage = checkSRTS(player.getWorld().getName());

        for (ItemStack item : itemRewards) {
            ItemStack formattedItem = item.clone(); //??
            ItemMeta itemMeta = formattedItem.getItemMeta();
            List<String> itemLore = itemMeta.getLore();
            if (itemLore == null) {
                continue;
            }
            List<String> itemLoreFormatted = new ArrayList<>();
            for (String peko : itemLore) {
                peko = userInterface.attachPlayerName(peko, player);
                peko = userInterface.formatColours(peko);
                itemLoreFormatted.add(peko);
            }
            itemMeta.setLore(itemLoreFormatted);
            formattedItem.setItemMeta(itemMeta);
            if(forceSendRewardToStorage) {
                storeItemToStorage(player.getName(), formattedItem);
                continue;
            }
            HashMap<Integer, ItemStack> notAddedItem = player.getInventory().addItem(formattedItem);
            if (!notAddedItem.isEmpty()) {
                fullInvDetected = true;
                storeItemToStorage(player.getName(), formattedItem);
            }

        }

        if(forceSendRewardToStorage) {
            return 2;
        }
        if(fullInvDetected) {
            return 1;
        }
        return 0;
    }

    private boolean checkSRTS(String name) {
        boolean isInList = this.SRTS_worldList.contains(name);
        if(isInList && !this.SRTS_isWhitelist) {
            return true;
        }
        if(!isInList && this.SRTS_isWhitelist) {
            return true;
        }
        return false;
    }

    private void giveMoneyRewards(Player player, double moneyGained) {
        if(vaultDep != null) {
            vaultDep.addBalance(player, moneyGained);
        }
    }

    private void executeCommandRewards(Player player, List<String> commandsToExecute) {
        for(String peko : commandsToExecute) {
            String command = userInterface.attachPlayerName(peko, player);
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
    }

    private RewardTier determineRewardTier(int timeTaken, ArrayList<RewardTier> rewardsTierList) {
        for (RewardTier tier : rewardsTierList) {
            int limit = tier.getMaxTimeInMilliseconds();
            if (timeTaken <= limit) {
                return tier;
            }
        }
        return null;
    }
}
