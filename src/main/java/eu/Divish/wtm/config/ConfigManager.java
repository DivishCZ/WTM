package eu.Divish.wtm.config;

import eu.Divish.wtm.WTM;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final WTM plugin;
    private final FileConfiguration config;

    public ConfigManager(WTM plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public String getLanguage() {
        return config.getString("language", "cs");
    }

    public boolean isWelcomeTitleEnabled() {
        return config.getBoolean("features.welcome_title.enabled", true);
    }

    public String getWelcomeTitle() {
        return config.getString("features.welcome_title.title", "&aVítej na serveru, %player_name%!");
    }

    public String getWelcomeSubtitle() {
        return config.getString("features.welcome_title.subtitle", "&7Užij si hru!");
    }

    public double getWelcomeFadeIn() {
        return config.getDouble("features.welcome_title.fade_in", 1.0);
    }

    public double getWelcomeStay() {
        return config.getDouble("features.welcome_title.stay", 3.5);
    }

    public double getWelcomeFadeOut() {
        return config.getDouble("features.welcome_title.fade_out", 1.0);
    }

    public double getWelcomeDelay() {
        return config.getDouble("features.welcome_title.delay", 1.0);
    }

    public boolean isFirstJoinEnabled() {
        return config.getBoolean("features.first_join.enabled", true);
    }

    public String getFirstJoinTitle() {
        return config.getString("features.first_join.title", "&6První připojení, %player_name%!");
    }

    public String getFirstJoinSubtitle() {
        return config.getString("features.first_join.subtitle", "&eUžij si speciální uvítání!");
    }

    public double getFirstJoinFadeIn() {
        return config.getDouble("features.first_join.fade_in", 1.0);
    }

    public double getFirstJoinStay() {
        return config.getDouble("features.first_join.stay", 5.0);
    }

    public double getFirstJoinFadeOut() {
        return config.getDouble("features.first_join.fade_out", 1.0);
    }

    public double getFirstJoinDelay() {
        return config.getDouble("features.first_join.delay", 1.0);
    }

    public boolean isUpdateCheckEnabled() {
        return config.getBoolean("updates.check_enabled", true);
    }
}
