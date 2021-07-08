package me.lucko.networkinterceptor;

import me.lucko.networkinterceptor.blockers.Blocker;
import me.lucko.networkinterceptor.blockers.CompositeBlocker;
import me.lucko.networkinterceptor.blockers.LearningBlocker;
import me.lucko.networkinterceptor.bukkit.BukkitConfiguration;
import me.lucko.networkinterceptor.common.AbstractConfiguration;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor;
import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor.IllegalConfigStateException;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class NetworkInterceptor extends JavaPlugin implements NetworkInterceptorPlugin {
    private final CommonNetworkInterceptor<NetworkInterceptor> delegate;
    private BukkitConfiguration config;
    private boolean registerManualStopTask = false;

    public NetworkInterceptor() {
        // init early
        // this is seen as bad practice, but we want to try and catch as
        // many requests as possible
        config = new BukkitConfiguration(getConfig());
        delegate = new CommonNetworkInterceptor<NetworkInterceptor>(this);

        // check and enable bStats
        boolean useMetrics = getConfig().getBoolean("enable-metrics", true);
        if (useMetrics) {
            int pluginId = 11822;
            new Metrics(this, pluginId);
        }
        getLogger().info(useMetrics ? "bStats metrics enabled" : "bStats metrics disabled");
    }

    public void onEnable() {
        if (registerManualStopTask) {
            getServer().getScheduler().runTaskLater(this, () -> {
                if (delegate.getBlocker() instanceof CompositeBlocker) {
                    ((CompositeBlocker) delegate.getBlocker()).stopUsingManualBlocker();
                } else if (delegate.getBlocker() instanceof LearningBlocker) {
                    Blocker delegate = ((LearningBlocker) this.delegate.getBlocker()).getDelegate();
                    if (delegate instanceof CompositeBlocker) {
                        ((CompositeBlocker) delegate).stopUsingManualBlocker();
                    }
                }
            }, 1L);
        }
        getCommand("networkinterceptor").setExecutor(new NetworkInterceptorCommand(this));
    }

    @Override
    public void onDisable() {
        disable();
    }

    public void reload() {
        reloadConfig();
        config = new BukkitConfiguration(getConfig());

        disable();
        try {
            enable();
        } catch (IllegalConfigStateException e) {
            getLogger().severe(e.getMessage());
            getLogger().severe("Disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public void logBlock(InterceptEvent event) {
        delegate.logAttempt(event);
    }

    public boolean shouldBlock(InterceptEvent event) {
        return delegate.shouldBlock(event);
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
    public boolean isBukkit() {
        return true;
    }

    @Override
    public boolean isBungee() {
        return false;
    }

    @Override
    public CommonNetworkInterceptor<?> getDelegate() {
        return delegate;
    }

}
