package me.lucko.networkinterceptor.common;

import java.io.File;
import java.util.logging.Logger;

public interface NetworkInterceptorPlugin<PLUGIN> {

    void saveDefaultConfig();

    AbstractConfiguration getConfiguration();

    void reloadConfig();

    Logger getLogger();

    void onEnable();

    void onDisable();

    void disablePlugin();

    File getDataFolder();

    String getServerVersion();

    String getPluginVersion();

    void runTaskLater(Runnable runnable, long ticks);

    Platform getPlatformType();

    CommonNetworkInterceptor<? extends NetworkInterceptorPlugin<PLUGIN>, PLUGIN> getDelegate();

    PluginManager<PLUGIN> getNIPluginManager();

    PLUGIN asPlugin();

    void reload();

}
