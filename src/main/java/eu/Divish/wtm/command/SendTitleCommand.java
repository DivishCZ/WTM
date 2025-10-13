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

public class SendTitleCommand implements CommandExecutor {

    private final WTM plugin;
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    public SendTitleCommand(WTM plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getLangManager().get("command.sendtitle.usage"));
            return true;
        }

        String fullMessage = String.join(" ", args);
        String[] parts = fullMessage.split("\\|", 2);

        String title = parts[0].trim();
        String subtitle = parts.length > 1 ? parts[1].trim() : "";

        Title.Times times = Title.Times.of(
                Duration.ofMillis(1000),
                Duration.ofMillis(3000),
                Duration.ofMillis(1000)
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            String parsedTitle = PlaceholderAPI.setPlaceholders(player, title);
            String parsedSubtitle = PlaceholderAPI.setPlaceholders(player, subtitle);

            Title adventureTitle = Title.title(
                    legacy.deserialize(parsedTitle),
                    legacy.deserialize(parsedSubtitle),
                    times
            );

            player.showTitle(adventureTitle);
        }

        return true;
    }
}
