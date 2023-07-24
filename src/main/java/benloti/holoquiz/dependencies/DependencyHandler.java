package benloti.holoquiz.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DependencyHandler {

    private VaultDep vaultDep = null;

    public DependencyHandler(JavaPlugin plugin) {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            Bukkit.getLogger().info("Suichan kyou mo kawaii");
            this.vaultDep = new VaultDep(plugin);
        }
    }

    public VaultDep getVaultDep() {
        return vaultDep;
    }
}
