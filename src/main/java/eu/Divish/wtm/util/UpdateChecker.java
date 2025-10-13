package eu.Divish.wtm.util;

import eu.Divish.wtm.WTM;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private final WTM plugin;
    private final String repoUrl = "https://api.github.com/repos/YOUR_USERNAME/YOUR_REPOSITORY/releases/latest";

    public UpdateChecker(WTM plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdate() {
        if (!plugin.getConfigManager().isUpdateCheckEnabled()) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(repoUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String json = response.toString();
                String latestVersion = json.split("\"tag_name\":\"")[1].split("\"")[0];

                PluginDescriptionFile desc = plugin.getDescription();
                String currentVersion = desc.getVersion();

                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    plugin.getLogger().warning("Nova verze WTM je dostupna: v" + latestVersion + " (nyni: v" + currentVersion + ")");
                    plugin.getLogger().warning("Navstiv GitHub pro stazeni aktualizace.");
                } else {
                    plugin.getLogger().info("WTM je aktualni (verze " + currentVersion + ")");
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Nepodarilo se zkontrolovat aktualizace: " + e.getMessage());
            }
        });
    }
}
