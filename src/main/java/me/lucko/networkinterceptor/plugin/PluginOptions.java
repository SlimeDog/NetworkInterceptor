package me.lucko.networkinterceptor.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;

public abstract class PluginOptions<T> {
    private final KeepPlugins keepType;
    private final boolean allowNonPlugin;
    private final Set<String> allTrustedPluginNames;
    private final Set<String> pluginNames;

    public PluginOptions(KeepPlugins keepType, boolean allowNonPlugin, Set<String> trustedPlugins) {
        this.keepType = keepType;
        this.allowNonPlugin = allowNonPlugin;
        this.allTrustedPluginNames = new HashSet<>(trustedPlugins);
        this.pluginNames = trustedPlugins;
    }

    public void searchForPlugins(NetworkInterceptorPlugin owner) {
        List<String> toBeLoaded = findAndAddPlugins(pluginNames, owner);
        if (!toBeLoaded.isEmpty()) {
            owner.runTaskLater(() -> {
                List<String> residual = findAndAddPlugins(toBeLoaded, owner);
                if (!residual.isEmpty()) {
                    for (String name : residual) {
                        owner.getLogger().warning("Unable to find plugin: " + name);
                    }
                }
            }, 1L); // TODO - is this enough?
        }
    }

    private List<String> findAndAddPlugins(Collection<String> pluginNames, NetworkInterceptorPlugin owner) {
        List<String> remainder = new ArrayList<>();
        for (String name : pluginNames) {
            if (!attemptAddPlugin(name)) {
                remainder.add(name);
            }
        }
        return remainder;
    }

    protected abstract boolean attemptAddPlugin(String name);

    public KeepPlugins getKeepType() {
        return keepType;
    }

    public boolean shouldAllowNonPlugin() {
        return allowNonPlugin;
    }

    public abstract boolean isTrusted(T plugin);

    // public boolean isTrusted(JavaPlugin plugin) {
    //     return trustedPlugins.contains(plugin);
    // }

    public boolean isListedAsTrustedPluginName(String pluginName) {
        return allTrustedPluginNames.contains(pluginName);
    }

}
