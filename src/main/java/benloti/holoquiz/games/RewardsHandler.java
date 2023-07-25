package benloti.holoquiz.games;

import benloti.holoquiz.structs.RewardTier;
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

    private ArrayList<RewardTier> allRewards;
    private boolean isHexSupported;

    public RewardsHandler(JavaPlugin plugin, boolean isCmiPresent) {
        File rewardsYml = new File(plugin.getDataFolder(), "Rewards.yml");
        this.allRewards = new ArrayList<>();
        this.isHexSupported = isCmiPresent;

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
            int maxTime = rewardTierSection.getInt("MaxAnswerTime");
            int moneyReward = rewardTierSection.getInt("Money");
            List<String> commandsExecuted = rewardTierSection.getStringList("Commands");
            ConfigurationSection rewardTierItemSection = rewardTierSection.getConfigurationSection("Items");

            ArrayList<ItemStack> itemReward = new ArrayList<>();
            for (String key2 : rewardTierItemSection.getKeys(false)) {
                ConfigurationSection rewardTierItem = rewardTierItemSection.getConfigurationSection(key2);
                String itemType = rewardTierItem.getString("Material");
                int itemQty = rewardTierItem.getInt("Qty");
                List<String> itemLore = rewardTierItem.getStringList("Lore");
                ItemStack itemStack = new ItemStack(Material.matchMaterial(itemType), itemQty);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setLore(itemLore);
                itemStack.setItemMeta(itemMeta);
                itemReward.add(itemStack);
            }

            allRewards.add(new RewardTier(maxTime, moneyReward, commandsExecuted, itemReward));
        }
    }

    public void giveRewards(Player player, int timeTaken) {
        RewardTier rewardTier = determineRewardTier(timeTaken);
        if (rewardTier == null) {
            return;
        }
        for(ItemStack item : rewardTier.getItemRewards()) {
            List<String> itemLore = item.getItemMeta().getLore();
            if(itemLore == null) {
                continue;
            }

            for(String lore : itemLore) {
                //regex it up
            }
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
