package eu.Divish.wtm.command;

import eu.Divish.wtm.WTM;
import eu.Divish.wtm.lang.LangManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class UpdateCommand implements CommandExecutor {

    private final WTM plugin;
    private final LangManager lang;

    public UpdateCommand(WTM plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (!player.isOp() && !player.hasPermission("wtm.update")) {
            player.sendMessage(Component.text(nz(lang.get("update.no_permission"), "You don't have permission.")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text(nz(lang.get("update.usage"), "/wtmupdate <confirm|cancel|open>")));
            return true;
        }

        // /wtmupdate cancel
        if (args[0].equalsIgnoreCase("cancel")) {
            plugin.markDeclinedThisBoot(player.getUniqueId());
            player.sendMessage(Component.text(nz(lang.get("update.cancelled"), "Update cancelled.")));
            return true;
        }

        // /wtmupdate open  -> pošle klikací link na stránku releasu
        if (args[0].equalsIgnoreCase("open")) {
            String tag = plugin.getLatestVersion();
            String url = (tag != null && !tag.isEmpty())
                    ? "https://github.com/DivishCZ/WTM/releases/tag/" + tag
                    : "https://github.com/DivishCZ/WTM/releases/latest";

            String labelText = nz(lang.get("update.open"), "Open release");

            Component msg = Component.text("[WTM] ", NamedTextColor.YELLOW)
                    .append(Component.text(labelText + " → ", NamedTextColor.GRAY))
                    .append(Component.text(url, NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.openUrl(url)));

            player.sendMessage(msg);
            return true;
        }

        // /wtmupdate confirm  -> stáhne wtm-<verze>.jar do plugins/update
        if (args[0].equalsIgnoreCase("confirm")) {
            if (!plugin.isUpdateAvailable()) {
                player.sendMessage(Component.text(nz(lang.get("update.no_update_available"), "No update is currently available.")));
                return true;
            }

            final String tag = plugin.getLatestVersion();           // např. "v1.2.3" nebo "1.2.3"
            final String versionForAsset = normalizeVersion(tag);   // "1.2.3"
            final String assetName = "wtm-" + versionForAsset + ".jar";
            final String downloadUrlStr = "https://github.com/DivishCZ/WTM/releases/download/" + tag + "/" + assetName;

            player.sendMessage(Component.text(nz(lang.get("update.downloading"), "Downloading the latest plugin version...")));

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                File updateDir = new File("plugins", "update");
                if (!updateDir.exists() && !updateDir.mkdirs()) {
                    player.sendMessage(Component.text(nz(lang.get("update.failed"), "Failed to download the update") + ": cannot create plugins/update"));
                    return;
                }

                File outputFile = new File(updateDir, assetName);
                try {
                    if (outputFile.exists()) Files.delete(outputFile.toPath());
                } catch (Exception ex) {
                    player.sendMessage(Component.text(nz(lang.get("update.failed"), "Failed to download the update") + ": cannot overwrite old file"));
                    return;
                }

                HttpURLConnection conn = null;
                try {
                    URL downloadUrl = new URL(downloadUrlStr);
                    conn = (HttpURLConnection) downloadUrl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "WTM-Updater/" + plugin.getDescription().getVersion());
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(15000);
                    conn.connect();

                    int code = conn.getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK) {
                        player.sendMessage(Component.text(nz(lang.get("update.failed"), "Failed to download the update") + ": HTTP " + code + " (" + downloadUrlStr + ")"));
                        return;
                    }

                    try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                         FileOutputStream out = new FileOutputStream(outputFile)) {
                        byte[] buf = new byte[8192];
                        int read;
                        while ((read = in.read(buf)) != -1) {
                            out.write(buf, 0, read);
                        }
                        out.flush();
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(Component.text(nz(lang.get("update.success"), "The new version was successfully downloaded. It will be applied after a server restart.")));
                        // po úspěšném stažení už hráče v této session znovu nevyzývat
                        plugin.markDeclinedThisBoot(player.getUniqueId());
                    });

                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            player.sendMessage(Component.text(nz(lang.get("update.failed"), "Failed to download the update") + ": " + e.getClass().getSimpleName() + " - " + e.getMessage()))
                    );
                } finally {
                    if (conn != null) conn.disconnect();
                }
            });
            return true;
        }

        // Neznámý subcommand
        player.sendMessage(Component.text(nz(lang.get("update.usage"), "/wtmupdate <confirm|cancel|open>")));
        return true;
    }

    /** Odstraní prefix 'v'/'V' a prerelease/build metadata pro názvy assetu. */
    private static String normalizeVersion(String v) {
        if (v == null) return "0.0.0";
        v = v.trim();
        if (v.startsWith("v") || v.startsWith("V")) v = v.substring(1);
        int dash = v.indexOf('-');
        if (dash >= 0) v = v.substring(0, dash);
        int plus = v.indexOf('+');
        if (plus >= 0) v = v.substring(0, plus);
        return v;
    }

    /** Null/empty → default string. */
    private static String nz(String s, String def) {
        return (s == null || s.isEmpty()) ? def : s;
    }
}
