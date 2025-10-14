package eu.Divish.wtm.command;

import eu.Divish.wtm.WTM;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;

public class SendTitleToPlayerCommand implements CommandExecutor {

    private final WTM plugin;
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    public SendTitleToPlayerCommand(WTM plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getLangManager().get("command.sendtitletoplayer.usage"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(plugin.getLangManager().get("command.sendtitletoplayer.notfound"));
            return true;
        }

        String fullMessage = String.join(" ", args).substring(args[0].length()).trim();
        String[] parts = fullMessage.split("\\|", 2);

        String title = parts[0].trim();
        String subtitle = parts.length > 1 ? parts[1].trim() : "";

        Title.Times times = Title.Times.of(
                Duration.ofMillis(1000),
                Duration.ofMillis(3000),
                Duration.ofMillis(1000)
        );

        String parsedTitle = PlaceholderAPI.setPlaceholders(target, title);
        String parsedSubtitle = PlaceholderAPI.setPlaceholders(target, subtitle);

        Title adventureTitle = Title.title(
                legacy.deserialize(parsedTitle),
                legacy.deserialize(parsedSubtitle),
                times
        );

        target.showTitle(adventureTitle);
        return true;
    }
}
