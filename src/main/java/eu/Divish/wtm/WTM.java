package eu.Divish.wtm;

import eu.Divish.wtm.command.SendTitleCommand;
import eu.Divish.wtm.command.SendTitleToPlayerCommand;
import eu.Divish.wtm.command.WelcomeCommand;
import eu.Divish.wtm.config.ConfigManager;
import eu.Divish.wtm.lang.LangManager;
import eu.Divish.wtm.listener.JoinListener;
import eu.Divish.wtm.title.TitleManager;
import eu.Divish.wtm.util.UpdateChecker;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class WTM extends JavaPlugin {

    private static WTM instance;
    private ConfigManager configManager;
    private LangManager langManager;
    private TitleManager titleManager;

    public static WTM getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.langManager = new LangManager(this);
        this.titleManager = new TitleManager(this, this.configManager);

        getServer().getPluginManager().registerEvents(new JoinListener(this, configManager), this);

        getCommand("sendtitle").setExecutor(new SendTitleCommand(this));
        getCommand("st").setExecutor(new SendTitleCommand(this));
        getCommand("sendtitletoplayer").setExecutor(new SendTitleToPlayerCommand(this));
        getCommand("sttp").setExecutor(new SendTitleToPlayerCommand(this));
        getCommand("welcome").setExecutor(new WelcomeCommand(this));

        new UpdateChecker(this).checkForUpdate();
        sendStartupMessage();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }

    private void sendStartupMessage() {
        getServer().getConsoleSender().sendMessage(color(
                "\n&2                    __         " +
                        "\n&2|\\ \\        /  /  \\  |   |" +
                        "\n&2| \\ \\      /   |  |   |   |  &3Vytvo\u0159il: &9Divish" +
                        "\n&2|  |  \\    /    \\__   |---|  &aStable version: &6" + getDescription().getVersion() +
                        "\n&2| /    \\  /        \\  |   |  &3Plugin running on " + getServer().getName() + " version &9" + getServer().getBukkitVersion() +
                        "\n&2|/      \\/      \\__/  |   |\n"
        ));
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
