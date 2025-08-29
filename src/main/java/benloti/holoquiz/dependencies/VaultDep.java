package benloti.holoquiz.dependencies;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultDep {
    private final JavaPlugin plugin;

    private Economy economy = null;
    private boolean isEnabled = false;

    public VaultDep(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialiseDep() {
        if (setupEconomy()) {
            isEnabled = true;
            return;
        }
        Bukkit.getLogger().info("[HoloQuiz] Error setting up Vault dependency, Vault disabled!");
    }

    public void addBalance(Player player, double amount) {
        if(economy == null || !isEnabled) {
            return;
        }
        economy.depositPlayer(player, amount);
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
