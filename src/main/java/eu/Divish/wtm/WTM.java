package eu.Divish.wtm;

import eu.Divish.wtm.command.SendTitleCommand;
import eu.Divish.wtm.command.SendTitleToPlayerCommand;
import eu.Divish.wtm.command.UpdateCommand;
import eu.Divish.wtm.command.WelcomeCommand;
import eu.Divish.wtm.config.ConfigManager;
import eu.Divish.wtm.lang.LangManager;
import eu.Divish.wtm.listener.JoinListener;
import eu.Divish.wtm.title.TitleManager;
import eu.Divish.wtm.util.UpdateChecker;
import eu.Divish.wtm.util.ConsoleBanner;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WTM extends JavaPlugin {

    private static WTM instance;
    private ConfigManager configManager;
    private LangManager langManager;
    private TitleManager titleManager;

    // stav updateru
    private volatile boolean updateAvailable = false;
    private volatile String latestVersion = "";

    // kdo v této serverové session zrušil nabídku (platí do restartu)
    private final Set<UUID> declinedThisBoot = ConcurrentHashMap.newKeySet();

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

        // Tvoje původní JoinListener (zachován kvůli ostatní logice pluginu)
        getServer().getPluginManager().registerEvents(new JoinListener(this, configManager), this);

        // Registrace příkazů bezpečně (ochrana proti NPE když chybí v plugin.yml)
        registerCommandSafely("sendtitle",  new SendTitleCommand(this));
        registerCommandSafely("st",         new SendTitleCommand(this));
        registerCommandSafely("sendtitletoplayer", new SendTitleToPlayerCommand(this));
        registerCommandSafely("sttp",       new SendTitleToPlayerCommand(this));
        registerCommandSafely("welcome",    new WelcomeCommand(this));
        registerCommandSafely("wtmupdate",  new UpdateCommand(this));

        // Kontrola aktualizace (asynchronně)
        new UpdateChecker(this).checkForUpdate();

        // Sjednocené upozornění po JOINu
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
                Player player = event.getPlayer();
                // ukážeme s mírným zpožděním (3s), aby hráč stihl načíst
                showUpdatePrompt(player, true);
            }
        }, this);

        sendStartupMessage();
    }

    /* =========================
       Updater – veřejné API
       ========================= */

    public void setUpdateAvailable(boolean updateAvailable, String latestVersion) {
        this.updateAvailable = updateAvailable;
        this.latestVersion = latestVersion == null ? "" : latestVersion;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public boolean hasDeclinedThisBoot(UUID uuid) {
        return declinedThisBoot.contains(uuid);
    }

    public void markDeclinedThisBoot(UUID uuid) {
        declinedThisBoot.add(uuid);
    }

    /**
     * Vrátí verzi vždy s přesně jedním 'v' (např. "v1.0.3").
     */
    private String displayVersion() {
        String v = latestVersion == null ? "" : latestVersion.trim();
        if (v.isEmpty()) return v;
        return (v.startsWith("v") || v.startsWith("V")) ? v : "v" + v;
    }

    /**
     * Sjednocené zobrazení informace o updatu pro daného hráče.
     * @param player          cílový hráč
     * @param afterJoinDelay  true = po joinu s ~3s zpožděním; false = hned (např. při detekci za běhu)
     */
    public void showUpdatePrompt(Player player, boolean afterJoinDelay) {
        if (!updateAvailable) return;
        if (!(player.isOp() || player.hasPermission("wtm.update"))) return;

        Runnable task = () -> {
            String lang = getLangManager().getLang();

            if (hasDeclinedThisBoot(player.getUniqueId())) {
                // už dříve zrušil → jen krátké info, bez otázky a bez tlačítek
                player.sendMessage(buildUpdateInfoLineOnly());
                return;
            }

            // Plná varianta: hlavička, doprovodná věta a interaktivní tlačítka
            player.sendMessage(buildUpdateHeaderLineFull());
            String wish = getLangManager().getMessage("update.wish_update", lang);
            if (wish != null && !wish.isEmpty()) {
                player.sendMessage(Component.text(wish, NamedTextColor.GRAY));
            }
            player.sendMessage(buildUpdateButtons(lang));
        };

        if (afterJoinDelay) {
            Bukkit.getScheduler().runTaskLater(this, task, 60L); // 3 sekundy
        } else {
            Bukkit.getScheduler().runTask(this, task);
        }
    }

    /* =========================
       UI stavebnice (Adventure)
       ========================= */

    // Krátká informační verze – bez otázky a bez tlačítek (použito po "cancel" do restartu)
    private Component buildUpdateInfoLineOnly() {
        // [WTM] Nová verze pluginu WTM (vX.Y.Z) je dostupná.
        return Component.empty()
                .append(Component.text("[WTM] ", NamedTextColor.YELLOW))
                .append(Component.text("Nová verze pluginu ", NamedTextColor.GRAY))
                .append(Component.text("WTM", NamedTextColor.GREEN))
                .append(Component.text(" (" + displayVersion() + ") je dostupná.", NamedTextColor.GRAY));
    }

    // Plná hlavička – přesně ve tvaru, jak jsi psal
    private Component buildUpdateHeaderLineFull() {
        // [WTM] Nová verze pluginu WTM (vX.Y.Z) je dostupná. Přejete si provést aktualizaci ?
        return Component.empty()
                .append(Component.text("[WTM] ", NamedTextColor.YELLOW))
                .append(Component.text("Nová verze pluginu ", NamedTextColor.GRAY))
                .append(Component.text("WTM", NamedTextColor.GREEN))
                .append(Component.text(" (" + displayVersion() + ") je dostupná. Přejete si provést aktualizaci ?", NamedTextColor.GRAY));
    }

    // Interaktivní tlačítka – ✅ confirm / ❌ cancel / 🌐 open
    public Component buildUpdateButtons(String lang) {
        String confirm = getLangManager().getMessage("update.confirm", lang);
        String cancel  = getLangManager().getMessage("update.cancel", lang);
        String openLbl = getLangManager().getMessage("update.open", lang); // nový klíč
        if (confirm == null || confirm.isEmpty()) confirm = "Potvrdit";
        if (cancel  == null || cancel.isEmpty())  cancel  = "Zrušit";
        if (openLbl == null || openLbl.isEmpty()) openLbl = "Otevřít release";

        String releaseUrl = getReleaseUrl();

        return Component.empty()
                .append(Component.text("[✅ ", NamedTextColor.GREEN, TextDecoration.BOLD)
                        .append(Component.text(confirm, NamedTextColor.GREEN, TextDecoration.BOLD))
                        .append(Component.text("]", NamedTextColor.GREEN, TextDecoration.BOLD))
                        .clickEvent(ClickEvent.runCommand("/wtmupdate confirm")))
                .append(Component.text("  "))
                .append(Component.text("[❌ ", NamedTextColor.RED, TextDecoration.BOLD)
                        .append(Component.text(cancel, NamedTextColor.RED, TextDecoration.BOLD))
                        .append(Component.text("]", NamedTextColor.RED, TextDecoration.BOLD))
                        .clickEvent(ClickEvent.runCommand("/wtmupdate cancel")))
                .append(Component.text("  "))
                .append(Component.text("[🌐 ", NamedTextColor.AQUA, TextDecoration.BOLD)
                        .append(Component.text(openLbl, NamedTextColor.AQUA, TextDecoration.BOLD))
                        .append(Component.text("]", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .clickEvent(ClickEvent.openUrl(releaseUrl)));
    }

    // URL releasu pro tlačítko „open“
    private String getReleaseUrl() {
        // Pokud známe tag (v1.2.3), pošleme přímo na stránku tagu, jinak na "latest"
        String tag = getLatestVersion();
        if (tag != null && !tag.isEmpty()) {
            return "https://github.com/DivishCZ/WTM/releases/tag/" + tag;
        }
        return "https://github.com/DivishCZ/WTM/releases/latest";
    }

    /* =========================
       Ostatní původní věci
       ========================= */

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }

    private void registerCommandSafely(String name, org.bukkit.command.CommandExecutor exec) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(exec);
        } else {
            getLogger().warning("Command '" + name + "' není v plugin.yml – přeskočeno.");
        }
    }

    /** Barevné velké DVSH, výchozí kompatibilní ASCII pro webové konzole. */
    private void sendStartupMessage() {
        String[] info = {
                "",
                "&7Vytvořil: &9Divish",
                "&aStable version WTM: &6" + getDescription().getVersion(),
                "&7Plugin running on &b" + getServer().getName()
                        + " &7version &9" + getServer().getBukkitVersion()
        };
        ConsoleBanner.print(getServer().getConsoleSender(),
                getConfig().getString("startup-banner.style", "ascii"), info);
    }
}
