package benloti.holoquiz.commands;

import benloti.holoquiz.dependencies.CMIDep;
import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.files.ExternalFiles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CmdAutoComplete implements TabCompleter {
    private final JavaPlugin plugin;
    private boolean easterEggs;
    private CMIDep cmiDep;

    public CmdAutoComplete(ExternalFiles externalFiles, DependencyHandler dep, JavaPlugin plugin) {
        this.easterEggs = externalFiles.getConfigFile().isEasterEggsEnabled();
        this.cmiDep = dep.getCMIDep();
        this.plugin = plugin;
    }

    public void reload(ExternalFiles externalFiles, DependencyHandler dep) {
        this.easterEggs = externalFiles.getConfigFile().isEasterEggsEnabled();
        this.cmiDep = dep.getCMIDep();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> autoCompleteSuggestions = new ArrayList<>();
        autoCompleteSuggestions.add("help");
        autoCompleteSuggestions.add("info");
        autoCompleteSuggestions.add("stats");
        autoCompleteSuggestions.add("toggle");
        autoCompleteSuggestions.add("top");
        autoCompleteSuggestions.add("collect");
        autoCompleteSuggestions.add("contest");
        if(sender.hasPermission("HoloQuiz.alert")) {
            autoCompleteSuggestions.add("alert");
        }
        if (sender.hasPermission("HoloQuiz.admin")) {
            autoCompleteSuggestions.add("next");
            autoCompleteSuggestions.add("start");
            autoCompleteSuggestions.add("stop");
            autoCompleteSuggestions.add("reloadQns");
            autoCompleteSuggestions.add("reloadStats");
            autoCompleteSuggestions.add("reload");
        }
        if (easterEggs) {
            autoCompleteSuggestions.add("pekofy");
            autoCompleteSuggestions.add("normal");
        }

        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String possibleArguments : autoCompleteSuggestions) {
                if(possibleArguments.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(possibleArguments);
                }
            }
            return result;
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if(subCommand.equals("stats")) {
                for(Player player : plugin.getServer().getOnlinePlayers()) {
                    if(cmiDep.isPlayerVanished(player)) {
                        continue;
                    }
                    result.add(player.getName());
                }
                return result;
            }
            if(subCommand.equals("top")) {
                result.add("best");
                result.add("average");
                result.add("answers");
                return result;
            }
        }
        return result;
    }
}
