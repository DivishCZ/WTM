package eu.Divish.wtm.command;

import eu.Divish.wtm.WTM;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class WelcomeCommand implements CommandExecutor {

    private final WTM plugin;

    public WelcomeCommand(WTM plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(plugin.getLangManager().get("command.welcome.usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("wtm.reload")) {
                sender.sendMessage(plugin.getLangManager().get("command.nopermission"));
                return true;
            }

            plugin.getConfigManager().reload();
            plugin.getLangManager().reload();
            sender.sendMessage(plugin.getLangManager().get("command.welcome.reload"));
            return true;
        }

        if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("ver")) {
            String version = plugin.getDescription().getVersion();
            List<String> authors = plugin.getDescription().getAuthors();
            String author = authors.isEmpty() ? "Unknown" : authors.get(0);

            String message = plugin.getLangManager().get("command.welcome.version")
                    .replace("%version%", version)
                    .replace("%author%", author);

            sender.sendMessage(message);
            return true;
        }

        sender.sendMessage(plugin.getLangManager().get("command.welcome.usage"));
        return true;
    }
}