package eu.Divish.wtm.title;

import eu.Divish.wtm.config.ConfigManager;
import eu.Divish.wtm.WTM;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public class TitleManager {
    private final WTM plugin;
    private final ConfigManager config;
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    public TitleManager(WTM plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void sendWelcomeTitle(Player player) {
        if (!config.isWelcomeTitleEnabled()) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String title = config.getWelcomeTitle();
            String subtitle = config.getWelcomeSubtitle();

            title = PlaceholderAPI.setPlaceholders(player, title);
            subtitle = PlaceholderAPI.setPlaceholders(player, subtitle);

            Component titleComponent = legacy.deserialize(title);
            Component subtitleComponent = legacy.deserialize(subtitle);

            Title.Times times = Title.Times.of(
                    Duration.ofMillis((long) (config.getWelcomeFadeIn() * 1000)),
                    Duration.ofMillis((long) (config.getWelcomeStay() * 1000)),
                    Duration.ofMillis((long) (config.getWelcomeFadeOut() * 1000))
            );

            Title welcomeTitle = Title.title(titleComponent, subtitleComponent, times);
            player.showTitle(welcomeTitle);
        }, (long) (config.getWelcomeDelay() * 20));
    }

    public void sendFirstJoinTitle(Player player) {
        if (!config.isFirstJoinEnabled()) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String title = config.getFirstJoinTitle();
            String subtitle = config.getFirstJoinSubtitle();

            title = PlaceholderAPI.setPlaceholders(player, title);
            subtitle = PlaceholderAPI.setPlaceholders(player, subtitle);

            Component titleComponent = legacy.deserialize(title);
            Component subtitleComponent = legacy.deserialize(subtitle);

            Title.Times times = Title.Times.of(
                    Duration.ofMillis((long) (config.getFirstJoinFadeIn() * 1000)),
                    Duration.ofMillis((long) (config.getFirstJoinStay() * 1000)),
                    Duration.ofMillis((long) (config.getFirstJoinFadeOut()  * 1000))
            );

            Title firstJoinTitle = Title.title(titleComponent, subtitleComponent, times);
            player.showTitle(firstJoinTitle);
        }, (long) (config.getFirstJoinDelay() * 20));
    }
}