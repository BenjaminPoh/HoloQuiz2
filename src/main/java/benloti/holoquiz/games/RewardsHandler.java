package benloti.holoquiz.games;

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

    public RewardsHandler(JavaPlugin plugin, UserInterface userInterface) {
        File rewardsYml = new File(plugin.getDataFolder(), "Rewards.yml");
        this.allRewards = new ArrayList<>();
        this.userInterface = userInterface;

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
            int moneyReward = rewardTierSection.getInt("Money");
            List<String> commandsExecuted = rewardTierSection.getStringList("Commands");
            ConfigurationSection rewardTierItemSection = rewardTierSection.getConfigurationSection("Items");

            ArrayList<ItemStack> itemReward = new ArrayList<>();
            for (String key2 : rewardTierItemSection.getKeys(false)) {
                ConfigurationSection rewardTierItem = rewardTierItemSection.getConfigurationSection(key2);
                String itemType = rewardTierItem.getString("Material");
                Material itemMaterial = Material.matchMaterial(itemType);
                if (itemMaterial == null) {
                    itemMaterial = Material.CARROT;
                    Bukkit.getLogger().info("Failed to load item: " + itemType);
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
        for(ItemStack item : rewardTier.getItemRewards()) {
            ItemMeta itemMeta = item.getItemMeta();
            List<String> itemLore = itemMeta.getLore();
            if(itemLore == null) {
                continue;
            }

            List<String> itemLoreFormatted = new ArrayList<>();
            for(String peko : itemLore) {
                String formattedLoreLine = userInterface.attachPlayerName(peko, player);
                formattedLoreLine = userInterface.formatColours(formattedLoreLine);
                itemLoreFormatted.add(formattedLoreLine);
            }
            itemMeta.setLore(itemLoreFormatted);
            item.setItemMeta(itemMeta);
            player.getInventory().addItem(item);
            Bukkit.getLogger().info("This should be seen and gucci");
        }
    }

    private RewardTier determineRewardTier(int timeTaken) {
        for(RewardTier tier : allRewards) {
            int limit = tier.getMaxTimeInMilliseconds();
            if(timeTaken <= limit) {
                return tier;
            }
        }
        return null;
    }

}
