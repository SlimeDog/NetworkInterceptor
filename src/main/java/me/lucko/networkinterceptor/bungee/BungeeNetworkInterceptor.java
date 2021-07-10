package me.lucko.networkinterceptor.bungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import me.lucko.networkinterceptor.common.AbstractConfiguration;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor;
import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeNetworkInterceptor extends Plugin implements NetworkInterceptorPlugin<Plugin> {
    private final CommonNetworkInterceptor<BungeeNetworkInterceptor, Plugin> delegate;
    private Configuration configuration;
    private BungeeConfiguration bungeeConfig;

    public BungeeNetworkInterceptor() {
        saveDefaultConfig();
        loadConfig();
        delegate = new CommonNetworkInterceptor<>(this);
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
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void saveDefaultConfig() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
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
    public boolean isBukkit() {
        return false;
    }

    @Override
    public boolean isBungee() {
        return true;
    }

    @Override
    public CommonNetworkInterceptor<BungeeNetworkInterceptor, Plugin> getDelegate() {
        return delegate;
    }

    @Override
    public Plugin asPlugin() {
        return this;
    }

}
