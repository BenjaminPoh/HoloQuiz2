package benloti.holoquiz.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DependencyHandler {

    private final VaultDep vaultDep;
    private final CMIDep cmiDep;

    public DependencyHandler(JavaPlugin plugin) {
        this.cmiDep = new CMIDep();
        this.vaultDep = new VaultDep(plugin);
        if (Bukkit.getPluginManager().isPluginEnabled("CMI") && Bukkit.getPluginManager().isPluginEnabled("CMILib")) {
            Bukkit.getLogger().info("[HoloQuiz] CMI & CMILib Plugin detected as Soft Dep!");
            this.cmiDep.setEnabled();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            Bukkit.getLogger().info("[HoloQuiz] Vault Plugin detected as Soft Dep!");
            this.vaultDep.initialiseDep();
        }
    }

    public VaultDep getVaultDep() {
        return vaultDep;
    }

    public CMIDep getCMIDep() {
        return cmiDep;
    }
}
