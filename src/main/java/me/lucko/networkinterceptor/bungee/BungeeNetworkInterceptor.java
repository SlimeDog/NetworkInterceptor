package me.lucko.networkinterceptor.bungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;

import me.lucko.networkinterceptor.NetworkInterceptorCommand;
import me.lucko.networkinterceptor.common.AbstractConfiguration;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor;
import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;
import me.lucko.networkinterceptor.common.Platform;
import me.lucko.networkinterceptor.common.PluginManager;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor.IllegalConfigStateException;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeNetworkInterceptor extends Plugin implements NetworkInterceptorPlugin<Plugin> {
    private static final String SAMPLE_ALLOW_CONFIG_FILE_NAME = "sample-allow-config.yml";
    private static final String SAMPLE_DENY_CONFIG_FILE_NAME = "sample-deny-config.yml";
    private final CommonNetworkInterceptor<BungeeNetworkInterceptor, Plugin> delegate;
    private Configuration configuration;
    private BungeeConfiguration bungeeConfig;
    private BungeePluginManager pluginManager;

    public BungeeNetworkInterceptor() {
        saveDefaultConfig();
        saveResource(SAMPLE_ALLOW_CONFIG_FILE_NAME);
        saveResource(SAMPLE_DENY_CONFIG_FILE_NAME);
        loadConfig();
        delegate = new CommonNetworkInterceptor<>(this);

        // check and enable bStats
        boolean useMetrics = getConfiguration().getBoolean("enable-metrics", true);
        if (useMetrics) {
            int pluginId = 12035;
            Metrics metrics = new Metrics(this, pluginId);
            metrics.addCustomChart(new SimplePie("mode", () -> configuration.getString("mode", "N/A")));
        }
        getLogger().info(useMetrics ? "bStats metrics enabled" : "bStats metrics disabled");
        this.pluginManager = new BungeePluginManager(getProxy().getPluginManager());
    }

    private void loadConfig() {
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            getLogger().warning("Problem loading config");
            e.printStackTrace();
        }
        bungeeConfig = new BungeeConfiguration(configuration, "");

        getProxy().getPluginManager().registerCommand(this, new NetworkInterceptorCommand<>(this).asBungeeCommand());
    }

    @Override
    public void onEnable() {
        delegate.onEnable();
    }

    @Override
    public void onDisable() {
        delegate.onDisable();
    }

    @Override
    public void saveDefaultConfig() {
        saveResource("config.yml");
    }

    public void saveResource(String fileName) {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), fileName);

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream(fileName)) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveConfig() {
        if (configuration == null) {
            loadConfig();
        }
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration,
                    new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            getLogger().warning("Problem saving default config");
            e.printStackTrace();
        }
    }

    @Override
    public AbstractConfiguration getConfiguration() {
        return bungeeConfig;
    }

    @Override
    public void reloadConfig() {
        loadConfig();
    }

    @Override
    public void disablePlugin() {
        getLogger().severe("Plugin should now disable but I am unaware as to how this should be done");
    }

    @Override
    public String getServerVersion() {
        return getProxy().getVersion();
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public void runTaskLater(Runnable runnable, long ticks) {
        getProxy().getScheduler().schedule(this, runnable, ticks * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public Platform getPlatformType() {
        return Platform.BUNGEE;
    }

    @Override
    public CommonNetworkInterceptor<BungeeNetworkInterceptor, Plugin> getDelegate() {
        return delegate;
    }

    @Override
    public Plugin asPlugin() {
        return this;
    }

    @Override
    public void reload() {
        reloadConfig();

        delegate.disable();
        try {
            delegate.enable();
        } catch (IllegalConfigStateException e) {
            getLogger().severe(e.getMessage());
            getLogger().severe("Disabling plugin");
            disablePlugin();
        }
    }

    @Override
    public PluginManager<Plugin> getNIPluginManager() {
        return pluginManager;
    }

}
