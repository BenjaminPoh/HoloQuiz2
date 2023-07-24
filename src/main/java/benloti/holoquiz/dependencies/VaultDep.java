package benloti.holoquiz.dependencies;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultDep {
    private final JavaPlugin plugin;

    private Economy economy = null;

    public VaultDep(JavaPlugin plugin) {
        Bukkit.getLogger().info("Vault Plugin Detected!");
        this.plugin = plugin;
        if (!setupEconomy()) {
            Bukkit.getLogger().info("Disabled due to no Vault dependency found!");
            //plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public void addBalance(String playerName, double amount) {
        if(economy == null) {
            Bukkit.getLogger().info("No economy, dont gae");
            return;
        }
        economy.bankDeposit(playerName,amount);
    }

    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        this.economy = rsp.getProvider();
        return true;
    }
}
