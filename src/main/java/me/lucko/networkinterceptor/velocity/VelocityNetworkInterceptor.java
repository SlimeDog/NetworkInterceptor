package me.lucko.networkinterceptor.velocity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;

import me.lucko.networkinterceptor.common.AbstractConfiguration;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor;
import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor.IllegalConfigStateException;

@Plugin(id = "networkinterceptor", name = "NetworkInterceptor", version = VelocityNetworkInterceptorInfo.VERSION, //
        description = "Plugin to monitor and block outgoing network requests", authors = { "drives_a_ford" })
public class VelocityNetworkInterceptor implements NetworkInterceptorPlugin<VelocityNetworkInterceptor> {
    private final ProxyServer server;
    private final Logger logger;
    private final VelictyLoggerWrapper loggerWrapper;
    private final Path dataDirectory;
    private final CommonNetworkInterceptor<VelocityNetworkInterceptor, VelocityNetworkInterceptor> delegate;
    private VelocityConfiguration config;

    @Inject
    public VelocityNetworkInterceptor(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.loggerWrapper = new VelictyLoggerWrapper(this, this.logger);
        saveDefaultConfig();
        reloadConfig();
        this.delegate = new CommonNetworkInterceptor<>(this);
    }

    @Override
    public void saveDefaultConfig() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = getConfigFile();

        if (!file.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
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
        server.getPluginManager().getPlugin("networkinterceptor");
    }

    @Override
    public File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Override
    public String getServerVersion() {
        return server.getVersion().getVersion();
    }

    @Override
    public String getPluginVersion() {
        return VelocityNetworkInterceptorInfo.VERSION;
    }

    @Override
    public void runTaskLater(Runnable runnable, long ticks) {
        server.getScheduler().buildTask(this, runnable).delay(ticks * 50L, TimeUnit.MILLISECONDS).schedule();
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
    public CommonNetworkInterceptor<VelocityNetworkInterceptor, VelocityNetworkInterceptor> getDelegate() {
        return delegate;
    }

    @Override
    public VelocityNetworkInterceptor asPlugin() {
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
}
