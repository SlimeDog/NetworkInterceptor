package me.lucko.networkinterceptor.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.lucko.networkinterceptor.NetworkInterceptor;

public class PluginOptions {
    private final KeepPlugins keepType;
    private final boolean allowNonPlugin;
    private final Set<JavaPlugin> trustedPlugins;
    private final Set<String> pluginNames;

    public PluginOptions(KeepPlugins keepType, boolean allowNonPlugin, Set<String> trustedPlugins) {
        this.keepType = keepType;
        this.allowNonPlugin = allowNonPlugin;
        this.pluginNames = trustedPlugins;
        this.trustedPlugins = new HashSet<>();
    }

    public void searchForPlugins(NetworkInterceptor owner) {
        List<String> toBeLoaded = findAndAddPlugins(pluginNames, owner);
        if (!toBeLoaded.isEmpty()) {
            owner.getServer().getScheduler().runTaskLater(owner, () -> {
                List<String> residual = findAndAddPlugins(toBeLoaded, owner);
                if (!residual.isEmpty()) {
                    for (String name : residual) {
                        owner.getLogger().warning("Unable to find plugin: " + name);
                    }
                }
            }, 1L); // TODO - is this enough?
        }
    }

    private List<String> findAndAddPlugins(Collection<String> pluginNames, JavaPlugin owner) {
        List<String> remainder = new ArrayList<>();
        for (String name : pluginNames) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            if (plugin == null) {
                remainder.add(name);
                continue;
            }
            if (!(plugin instanceof JavaPlugin)) {
                owner.getLogger().warning("Plugin of unknown type (" + name + "): " + plugin);
                continue;
            }
            this.trustedPlugins.add((JavaPlugin) plugin);
        }
        return remainder;
    }

    public KeepPlugins getKeepType() {
        return keepType;
    }

    public boolean shouldAllowNonPlugin() {
        return allowNonPlugin;
    }

    public boolean isTrusted(JavaPlugin plugin) {
        return trustedPlugins.contains(plugin);
    }

}
