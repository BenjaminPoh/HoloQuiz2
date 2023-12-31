package benloti.holoquiz.games;

import benloti.holoquiz.dependencies.VaultDep;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.structs.RewardTier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class RewardsHandler {

    private final ArrayList<RewardTier> allRewards;
    private final ArrayList<RewardTier> secretRewards;
    private final UserInterface userInterface;
    private final VaultDep vaultDep;
    private final JavaPlugin plugin;

    public RewardsHandler(JavaPlugin plugin, UserInterface userInterface, VaultDep vaultDep,
                          ArrayList<RewardTier> allRewards, ArrayList<RewardTier> secretRewards) {
        this.allRewards = allRewards;
        this.secretRewards = secretRewards;
        this.userInterface = userInterface;
        this.vaultDep = vaultDep;
        this.plugin = plugin;
    }

    public void giveNormalRewards(Player player, int timeTaken) {
        RewardTier rewardTier = determineRewardTier(timeTaken, allRewards);
        giveRewardsByTier(player, rewardTier);
    }

    public void giveSecretRewards(Player player, int timeTaken) {
        RewardTier rewardTier = determineRewardTier(timeTaken, secretRewards);
        giveRewardsByTier(player, rewardTier);
    }

    private void giveRewardsByTier(Player player, RewardTier rewardTier) {
        if (rewardTier == null) {
            return;
        }
        giveItemRewards(player, rewardTier);
        giveMoneyRewards(player, rewardTier);
        executeCommandRewards(player, rewardTier);
    }

    private void giveItemRewards(Player player, RewardTier rewardTier) {
        for (ItemStack item : rewardTier.getItemRewards()) {
            ItemMeta itemMeta = item.getItemMeta();
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
            item.setItemMeta(itemMeta);
            player.getInventory().addItem(item);
            //Stupidly reset itemMeta
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);
        }
    }

    private void giveMoneyRewards(Player player, RewardTier rewardTier) {
        double moneyGained = rewardTier.getMoneyReward();
        if(vaultDep != null) {
            vaultDep.addBalance(player, moneyGained);
        }
    }

    private void executeCommandRewards(Player player, RewardTier rewardTier) {
        for(String peko : rewardTier.getCommandsExecuted()) {
            String command = userInterface.attachPlayerName(peko, player);
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            });
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
