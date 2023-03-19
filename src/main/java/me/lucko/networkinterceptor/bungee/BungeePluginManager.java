package me.lucko.networkinterceptor.bungee;

import me.lucko.networkinterceptor.common.PluginManager;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePluginManager implements PluginManager<Plugin> {
    private final net.md_5.bungee.api.plugin.PluginManager delegate;

    public BungeePluginManager(net.md_5.bungee.api.plugin.PluginManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName(Plugin plugin) {
        return plugin.getDescription().getName();
    }

    @Override
    public Plugin getPlugin(String name) {
        return delegate.getPlugin(name);
    }

}
