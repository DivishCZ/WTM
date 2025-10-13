package eu.Divish.wtm.listener;

import eu.Divish.wtm.WTM;
import eu.Divish.wtm.config.ConfigManager;
import eu.Divish.wtm.title.TitleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final TitleManager titleManager;

    public JoinListener(WTM plugin, ConfigManager configManager) {
        this.titleManager = new TitleManager(plugin, configManager);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            titleManager.sendFirstJoinTitle(player);
        } else {
            titleManager.sendWelcomeTitle(player);
        }
    }
}