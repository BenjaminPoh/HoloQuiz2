package benloti.holoquiz.commands;

import benloti.holoquiz.files.ExternalFiles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CmdAutoComplete implements TabCompleter {
    private final boolean easterEggs;
    private final JavaPlugin plugin;

    public CmdAutoComplete(ExternalFiles externalFiles, JavaPlugin plugin) {
        this.easterEggs = externalFiles.getConfigFile().isEasterEggsEnabled();
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> autoCompleteSuggestions = new ArrayList<>();
        autoCompleteSuggestions.add("help");
        autoCompleteSuggestions.add("info");
        autoCompleteSuggestions.add("stats");
        autoCompleteSuggestions.add("toggle");
        autoCompleteSuggestions.add("top");
        if (sender.hasPermission("HoloQuiz.admin")) {
            autoCompleteSuggestions.add("next");
            autoCompleteSuggestions.add("start");
            autoCompleteSuggestions.add("stop");
            autoCompleteSuggestions.add("reload");
            autoCompleteSuggestions.add("repairDB");
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
