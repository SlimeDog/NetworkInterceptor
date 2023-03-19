package me.lucko.networkinterceptor;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.lucko.networkinterceptor.common.PluginManager;

public class BukkitPluginManager implements PluginManager<JavaPlugin> {
    private final org.bukkit.plugin.PluginManager delegate;

    public BukkitPluginManager(org.bukkit.plugin.PluginManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName(JavaPlugin plugin) {
        return plugin.getName();
    }

    @Override
    public JavaPlugin getPlugin(String name) {
        Plugin plugin = delegate.getPlugin(name);
        if (plugin instanceof JavaPlugin) {
            return (JavaPlugin) plugin;
        }
        return null;
    }

}
