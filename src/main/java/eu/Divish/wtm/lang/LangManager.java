package eu.Divish.wtm.lang;

import eu.Divish.wtm.WTM;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LangManager {

    private final WTM plugin;
    private final Map<String, FileConfiguration> languages = new HashMap<>();
    private FileConfiguration activeLang;

    public LangManager(WTM plugin) {
        this.plugin = plugin;
        loadLanguages();
    }

    private void loadLanguages() {
        String[] supported = {"cz", "en", "de"};
        for (String lang : supported) {
            File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");

            if (!langFile.exists()) {
                plugin.saveResource("lang/" + lang + ".yml", false);
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);

            // Load default values from inside the jar
            InputStream defConfigStream = plugin.getResource("lang/" + lang + ".yml");
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
                config.setDefaults(defConfig);
            }

            languages.put(lang, config);
        }

        String selectedLang = plugin.getConfigManager().getLanguage().toLowerCase();
        this.activeLang = languages.getOrDefault(selectedLang, languages.get("cz"));
    }

    public String get(String key) {
        return activeLang.getString(key, "Missing lang: " + key);
    }

    public void reload() {
        languages.clear();
        loadLanguages();
    }
}