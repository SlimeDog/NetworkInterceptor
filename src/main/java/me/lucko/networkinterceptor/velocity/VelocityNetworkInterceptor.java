package me.lucko.networkinterceptor.velocity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import me.lucko.networkinterceptor.NetworkInterceptorCommand;
import me.lucko.networkinterceptor.common.AbstractConfiguration;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor;
import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor.IllegalConfigStateException;

@Plugin(id = VelocityNetworkInterceptorInfo.ID, name = VelocityNetworkInterceptorInfo.NAME, //
        version = VelocityNetworkInterceptorInfo.VERSION, //
        description = VelocityNetworkInterceptorInfo.DESCRIPTION, authors = { "drives_a_ford" }, dependencies = {
                @Dependency(id = "luckperms", optional = true) })
public class VelocityNetworkInterceptor implements NetworkInterceptorPlugin<PluginContainer> {
    private static final String SAMPLE_ALLOW_CONFIG_FILE_NAME = "sample-allow-config.yml";
    private static final String SAMPLE_DENY_CONFIG_FILE_NAME = "sample-deny-config.yml";
    private final ProxyServer server;
    private final Logger logger;
    private final VelictyLoggerWrapper loggerWrapper;
    private final Path dataDirectory;
    private final CommonNetworkInterceptor<VelocityNetworkInterceptor, PluginContainer> delegate;
    private VelocityConfiguration config;
    private final Metrics.Factory metricsFactory;
    private boolean isStartup = true;

    @Inject
    public VelocityNetworkInterceptor(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory,
            Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.loggerWrapper = new VelictyLoggerWrapper(this, this.logger);
        saveDefaultConfig();
        saveResource(SAMPLE_ALLOW_CONFIG_FILE_NAME);
        saveResource(SAMPLE_DENY_CONFIG_FILE_NAME);
        reloadConfig();
        this.delegate = new CommonNetworkInterceptor<>(this);
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // All you have to do is adding the following two lines in your
        // onProxyInitialization method.
        // You can find the plugin ids of your plugins on the page
        // https://bstats.org/what-is-my-plugin-id
        // check and enable bStats
        this.onEnable();
        boolean useMetrics = getConfiguration().getBoolean("enable-metrics", true);
        if (useMetrics) {
            int pluginId = 12197;
            Metrics metrics = metricsFactory.make(this, pluginId);
            metrics.addCustomChart(new SimplePie("mode", () -> config.getString("mode", "N/A")));
        }
        getLogger().info(useMetrics ? "bStats metrics enabled" : "bStats metrics disabled");

        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("networkinterceptorvelocity").aliases("niv").build();

        commandManager.register(meta, new NetworkInterceptorCommand<>(this).asVelocityCommand());
        isStartup = false;
        for (RepeatingTaskInfo info : repeatingTasksToSchedule) {
            runRepeatingTask(info.runnable, info.ticks);
        }
        repeatingTasksToSchedule.clear();
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
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getConfigFile() {
        return new File(getDataFolder(), "config.yml");
    }

    @Override
    public AbstractConfiguration getConfiguration() {
        return config;
    }

    @Override
    public void reloadConfig() {
        try {
            config = new VelocityConfiguration(getConfigFile());
        } catch (FileNotFoundException e) {
            logger.warn("Unable to initialize config: ", e);
        }
    }

    @Override
    public java.util.logging.Logger getLogger() {
        return loggerWrapper;
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
    public void disablePlugin() {
        getLogger().severe("Plugin should now disable but I am unaware as to how this should be done");
    }

    @Override
    public File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Override
    public String getServerVersion() {
        return "Velocity " + server.getVersion().getVersion();
    }

    @Override
    public String getPluginVersion() {
        return VelocityNetworkInterceptorInfo.VERSION;
    }

    @Override
    public void runTaskLater(Runnable runnable, long ticks) {
        server.getScheduler().buildTask(this, runnable).delay(ticks * 50L, TimeUnit.MILLISECONDS).schedule();
    }

    private List<RepeatingTaskInfo> repeatingTasksToSchedule = new ArrayList<>();

    public void runRepeatingTask(Runnable runnable, long ticks) {
        if (isStartup) {
            repeatingTasksToSchedule.add(new RepeatingTaskInfo(runnable, ticks));
            return;
        }
        server.getScheduler().buildTask(this, runnable).repeat(ticks * 50L, TimeUnit.MILLISECONDS)
                .delay(ticks * 50L, TimeUnit.MILLISECONDS).schedule();
    }

    @Override
    public boolean isBukkit() {
        return false;
    }

    @Override
    public boolean isBungee() {
        return false;
    }

    @Override
    public boolean isVelocity() {
        return true;
    }

    @Override
    public CommonNetworkInterceptor<VelocityNetworkInterceptor, PluginContainer> getDelegate() {
        return delegate;
    }

    @Override
    public PluginContainer asPlugin() {
        return server.getPluginManager().getPlugin(VelocityNetworkInterceptorInfo.ID).get();
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

    public ProxyServer getServer() {
        return server;
    }

    private class RepeatingTaskInfo {
        private final Runnable runnable;
        private final long ticks;

        public RepeatingTaskInfo(Runnable runnable, long ticks) {
            this.runnable = runnable;
            this.ticks = ticks;
        }
    }

}
