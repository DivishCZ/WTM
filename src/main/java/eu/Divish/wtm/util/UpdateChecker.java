package eu.Divish.wtm.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.Divish.wtm.WTM;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Asynchronně ověří nejnovější release na GitHubu a při nalezení novější verze:
 *  - nastaví stav v pluginu (updateAvailable, latestVersion)
 *  - sjednoceně upozorní všechny online OP/perm hráče přes WTM.showUpdatePrompt(...)
 */
public class UpdateChecker {

    private final WTM plugin;
    private static final String REPO_URL = "https://api.github.com/repos/DivishCZ/WTM/releases/latest";

    public UpdateChecker(WTM plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdate() {
        if (!plugin.getConfigManager().isUpdateCheckEnabled()) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(REPO_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // GitHub vyžaduje User-Agent, plus timeouty
                PluginDescriptionFile desc = plugin.getDescription();
                conn.setRequestProperty("User-Agent", "WTM-UpdateChecker/" + desc.getVersion());
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int status = conn.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    plugin.getLogger().warning("Kontrola aktualizací selhala: HTTP " + status);
                    return;
                }

                String latestTag;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    JsonObject json = JsonParser.parseReader(in).getAsJsonObject();
                    latestTag = json.get("tag_name").getAsString(); // např. "v1.2.3" nebo "1.2.3"
                } finally {
                    conn.disconnect();
                }

                String current = desc.getVersion();
                String latestNorm  = normalizeVersion(latestTag);
                String currentNorm = normalizeVersion(current);

                boolean isNewer = isNewerSemver(latestNorm, currentNorm);
                if (isNewer) {
                    plugin.getLogger().warning("Nová verze WTM je dostupná: " + latestTag + " (nyní: " + current + ")");
                    plugin.setUpdateAvailable(true, latestTag);

                    // Upozorni aktuálně online hráče s oprávněním (sjednocený UI výpis)
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getOnlinePlayers()
                            .forEach(p -> plugin.showUpdatePrompt(p, false)));

                } else {
                    plugin.getLogger().info("WTM je aktuální (verze " + current + ")");
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Nepodařilo se zkontrolovat aktualizace: " +
                        e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        });
    }

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

    // Jednoduché semver porovnání: 1.2.10 > 1.2.3
    private static boolean isNewerSemver(String latest, String current) {
        String[] a = latest.split("\\.");
        String[] b = current.split("\\.");
        int len = Math.max(a.length, b.length);
        for (int i = 0; i < len; i++) {
            int ai = (i < a.length ? parseIntSafe(a[i]) : 0);
            int bi = (i < b.length ? parseIntSafe(b[i]) : 0);
            if (ai != bi) return ai > bi;
        }
        return false;
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (Exception ignored) {
            return 0;
        }
    }
}
