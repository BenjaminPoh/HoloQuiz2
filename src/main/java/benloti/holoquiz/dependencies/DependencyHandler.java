package benloti.holoquiz.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DependencyHandler {

    private VaultDep vaultDep = null;
    private CMIDep cmiDep = null;

    public DependencyHandler(JavaPlugin plugin) {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            Bukkit.getLogger().info("[HoloQuiz] Vault Plugin detected as Soft Dep!");
            this.vaultDep = new VaultDep(plugin);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            Bukkit.getLogger().info("[HoloQuiz] CMI Plugin detected as Soft Dep!");
            this.cmiDep = new CMIDep(plugin);
        }
    }

    public VaultDep getVaultDep() {
        return vaultDep;
    }

    public boolean isCmiPresent() {
        return (cmiDep != null);
    }
}