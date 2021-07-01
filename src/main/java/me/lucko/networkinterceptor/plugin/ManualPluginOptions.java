package me.lucko.networkinterceptor.plugin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

public class ManualPluginOptions {
    private final Map<String, String> packageToPluginNameMap = new HashMap<>();
    private final boolean isEnabled;
    private final boolean disableAfterStartup;

    public ManualPluginOptions(ConfigurationSection section) {
        if (section == null) {
            isEnabled = false;
            disableAfterStartup = false;
            return;
        }
        isEnabled = section.getBoolean("enabled", false);
        disableAfterStartup = section.getBoolean("disable-after-startup", true);
        loadPackages(section.getConfigurationSection("plugins"));
    }

    private void loadPackages(ConfigurationSection section) {
        if (section == null) {
            return; // empty
        }
        for (String pluginName : section.getKeys(false)) {
            for (String packageName : section.getStringList(pluginName)) {
                packageToPluginNameMap.put(packageName, pluginName);
            }
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean disableAfterStartup() {
        return disableAfterStartup;
    }

    public boolean isEmpty() {
        return !isEnabled || packageToPluginNameMap.isEmpty();
    }

    public void clear() {
        packageToPluginNameMap.clear();
    }

    public String getPluginNameFor(String className) {
        for (String packageName : packageToPluginNameMap.keySet()) {
            if (className.startsWith(packageName)) {
                return packageToPluginNameMap.get(packageName);
            }
        }
        return null;
    }
    
}
