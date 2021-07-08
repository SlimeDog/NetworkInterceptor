package me.lucko.networkinterceptor.common;

import java.io.File;
import java.util.logging.Logger;

public interface NetworkInterceptorPlugin {

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

    boolean isBukkit();

    boolean isBungee();

    CommonNetworkInterceptor<?> getDelegate();

}
