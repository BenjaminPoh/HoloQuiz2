package benloti.holoquiz2.archive;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestClass implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command theCommand, String alias, String [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        sender.sendMessage("So as a joke...");
        return true;
    }
}
