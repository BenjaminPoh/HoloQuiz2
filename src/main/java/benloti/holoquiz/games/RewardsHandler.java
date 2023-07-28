package benloti.holoquiz.games;

import benloti.holoquiz.dependencies.VaultDep;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.structs.RewardTier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RewardsHandler {

    private final ArrayList<RewardTier> allRewards;
    private final UserInterface userInterface;
    private final VaultDep vaultDep;
    private final JavaPlugin plugin;

    public RewardsHandler(JavaPlugin plugin, UserInterface userInterface, VaultDep vaultDep) {
        File rewardsYml = new File(plugin.getDataFolder(), "Rewards.yml");
        this.allRewards = new ArrayList<>();
        this.userInterface = userInterface;
        this.vaultDep = vaultDep;
        this.plugin = plugin;

        if (!rewardsYml.exists()) {
            try {
                rewardsYml.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileConfiguration rewardsFile = YamlConfiguration.loadConfiguration(rewardsYml);
        ConfigurationSection rewardsSection = rewardsFile.getConfigurationSection("Rewards");

        for (String key : rewardsSection.getKeys(false)) {
            ConfigurationSection rewardTierSection = rewardsSection.getConfigurationSection(key);
            double maxTime = rewardTierSection.getDouble("MaxAnswerTime");
            int maxTimeInMilliseconds = (int) maxTime * 1000;
            double moneyReward = rewardTierSection.getDouble("Money");
            List<String> commandsExecuted = rewardTierSection.getStringList("Commands");
            ConfigurationSection rewardTierItemSection = rewardTierSection.getConfigurationSection("Items");

            ArrayList<ItemStack> itemReward = new ArrayList<>();
            for (String key2 : rewardTierItemSection.getKeys(false)) {
                ConfigurationSection rewardTierItem = rewardTierItemSection.getConfigurationSection(key2);
                String itemType = rewardTierItem.getString("Material");
                Material itemMaterial = Material.matchMaterial(itemType);
                if (itemMaterial == null) {
                    itemMaterial = Material.CARROT;
                    Bukkit.getLogger().info("Failed to load item of name: " + itemType);
                }
                int itemQty = rewardTierItem.getInt("Qty");
                List<String> itemLore = rewardTierItem.getStringList("Lore");
                ItemStack itemStack = new ItemStack(itemMaterial, itemQty);

                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setLore(itemLore);
                itemStack.setItemMeta(itemMeta);
                itemReward.add(itemStack);
            }

            allRewards.add(new RewardTier(maxTimeInMilliseconds, moneyReward, commandsExecuted, itemReward));
        }
    }

    public void giveRewards(Player player, int timeTaken) {
        RewardTier rewardTier = determineRewardTier(timeTaken);
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
        vaultDep.addBalance(player, moneyGained);
    }

    private void executeCommandRewards(Player player, RewardTier rewardTier) {
        for(String peko : rewardTier.getCommandsExecuted()) {
            String command = userInterface.attachPlayerName(peko, player);
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            });
        }
    }

    private RewardTier determineRewardTier(int timeTaken) {
        for (RewardTier tier : allRewards) {
            int limit = tier.getMaxTimeInMilliseconds();
            if (timeTaken <= limit) {
                return tier;
            }
        }
        return null;
    }

}
