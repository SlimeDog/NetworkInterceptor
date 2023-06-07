package me.lucko.networkinterceptor;

import me.lucko.networkinterceptor.blockers.Blocker;
import me.lucko.networkinterceptor.blockers.CompositeBlocker;
import me.lucko.networkinterceptor.blockers.LearningBlocker;
import me.lucko.networkinterceptor.bukkit.BukkitConfiguration;
import me.lucko.networkinterceptor.common.AbstractConfiguration;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor;
import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;
import me.lucko.networkinterceptor.common.Platform;
import me.lucko.networkinterceptor.common.PluginManager;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor.IllegalConfigStateException;

import java.io.File;
import java.util.function.BiConsumer;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

import dev.ratas.slimedogcore.impl.SlimeDogCore;
import dev.ratas.slimedogcore.impl.utils.UpdateChecker;

public class NetworkInterceptor extends SlimeDogCore implements NetworkInterceptorPlugin<JavaPlugin> {
    private static final int SPIGOT_ID = 53351;
    private static final String HANGAR_AUTHOR = "SlimeDog";
    private static final String HANGAR_SLUG = "NetworkInterceptor";
    private static final String SAMPLE_ALLOW_CONFIG_FILE_NAME = "sample-allow-config.yml";
    private static final String SAMPLE_DENY_CONFIG_FILE_NAME = "sample-deny-config.yml";
    private final CommonNetworkInterceptor<NetworkInterceptor, JavaPlugin> delegate;
    private BukkitConfiguration config;
    private boolean registerManualStopTask = false;
    private PluginManager<JavaPlugin> pluginManager;

    public NetworkInterceptor() {
        // init early
        // this is seen as bad practice, but we want to try and catch as
        // many requests as possible
        saveDefaultConfig();
        saveResourceInternal(SAMPLE_ALLOW_CONFIG_FILE_NAME);
        saveResourceInternal(SAMPLE_DENY_CONFIG_FILE_NAME);
        config = new BukkitConfiguration(getDefaultConfig());
        delegate = new CommonNetworkInterceptor<>(this);

        // check and enable bStats
        boolean useMetrics = getConfig().getBoolean("enable-metrics", true);
        if (useMetrics) {
            int pluginId = 11822;
            Metrics metrics = new Metrics(this, pluginId);
            metrics.addCustomChart(new SimplePie("mode", () -> config.getString("mode", "N/A")));
            metrics.addCustomChart(new SimplePie("trustedplugins",
                    () -> String.valueOf(delegate.getPluginOptions().getTrustedOptions().getPluginNames().size())));
            metrics.addCustomChart(new SimplePie("blockedplugins",
                    () -> String.valueOf(delegate.getPluginOptions().getBlockedOptions().getPluginNames().size())));
        }
        getLogger().info(useMetrics ? "bStats metrics enabled" : "bStats metrics disabled");
        pluginManager = new BukkitPluginManager(getServer().getPluginManager());
        // updating
        if (getDefaultConfig().getConfig().getBoolean("check-for-updates", true)) {
            String source = getDefaultConfig().getConfig().getString("update-source", "Hangar");
            BiConsumer<UpdateChecker.VersionResponse, String> consumer = (response, version) -> {
                switch (response) {
                    case LATEST:
                        getLogger().info("Already on latest version");
                        break;
                    case FOUND_NEW:
                        getLogger().info("Found new version: " + version);
                        break;
                    case UNAVAILABLE:
                        getLogger().info("Version information not available");
                        break;
                }
            };
            UpdateChecker checker;
            if (source.equalsIgnoreCase("Hangar")) {
                checker = UpdateChecker.forHangar(this, consumer, HANGAR_AUTHOR, HANGAR_SLUG);
            } else {
                checker = UpdateChecker.forSpigot(this, consumer, SPIGOT_ID);
            }
            checker.check();
        }
    }

    private void saveResourceInternal(String name) {
        File file = new File(getDataFolder(), name);
        if (file.exists()) {
            return;
        }
        saveResource(name, false);
    }

    @Override
    public void pluginEnabled() {
        delegate.onEnable();
        if (registerManualStopTask) {
            getServer().getScheduler().runTaskLater(this, () -> {
                if (delegate.getBlocker() instanceof CompositeBlocker) {
                    ((CompositeBlocker<JavaPlugin>) delegate.getBlocker()).stopUsingManualBlocker();
                } else if (delegate.getBlocker() instanceof LearningBlocker) {
                    Blocker<JavaPlugin> delegate = ((LearningBlocker<JavaPlugin>) this.delegate.getBlocker())
                            .getDelegate();
                    if (delegate instanceof CompositeBlocker) {
                        ((CompositeBlocker<JavaPlugin>) delegate).stopUsingManualBlocker();
                    }
                }
            }, 1L);
        }
        getCommand("networkinterceptor").setExecutor(new NetworkInterceptorCommand<>(this).asSpigotCommand());
    }

    @Override
    public void pluginDisabled() {
        disable();
    }

    @Override
    public void reload() {
        reloadConfig();
        config = new BukkitConfiguration(getDefaultConfig());

        disable();
        try {
            enable();
        } catch (IllegalConfigStateException e) {
            getLogger().severe(e.getMessage());
            getLogger().severe("Disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void enable() throws IllegalConfigStateException {
        delegate.enable();
    }

    private void disable() {
        delegate.disable();
    }

    @Override
    public AbstractConfiguration getConfiguration() {
        return config;
    }

    @Override
    public void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public String getServerVersion() {
        return getServer().getVersion();
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public void runTaskLater(Runnable runnable, long ticks) {
        getServer().getScheduler().runTaskLater(this, runnable, ticks);
    }

    @Override
    public Platform getPlatformType() {
        return Platform.BUKKIT;
    }

    @Override
    public CommonNetworkInterceptor<NetworkInterceptor, JavaPlugin> getDelegate() {
        return delegate;
    }

    @Override
    public JavaPlugin asPlugin() {
        return this;
    }

    @Override
    public PluginManager<JavaPlugin> getNIPluginManager() {
        return pluginManager;
    }

}
