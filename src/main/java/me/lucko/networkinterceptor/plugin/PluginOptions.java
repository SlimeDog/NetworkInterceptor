package me.lucko.networkinterceptor.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;

public abstract class PluginOptions<PLUGIN> {
    private final KeepPlugins keepType;
    private final boolean allowNonPlugin;
    private final Set<String> allTrustedPluginNames;
    private final Set<String> pluginNames;
    protected final boolean trust;

    public PluginOptions(KeepPlugins keepType, boolean allowNonPlugin, Set<String> plugins, boolean trust) {
        this.keepType = keepType;
        this.allowNonPlugin = allowNonPlugin;
        this.allTrustedPluginNames = new HashSet<>(plugins);
        this.pluginNames = plugins;
        this.trust = trust;
    }

    public void searchForPlugins(NetworkInterceptorPlugin<PLUGIN> owner) {
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

    private List<String> findAndAddPlugins(Collection<String> pluginNames, NetworkInterceptorPlugin<PLUGIN> owner) {
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

    public abstract boolean isTrusted(PLUGIN plugin);

    public boolean isListedAsTrustedPluginName(String pluginName) {
        if (!trust) {
            return !allTrustedPluginNames.contains(pluginName);
        }
        return allTrustedPluginNames.contains(pluginName);
    }

}
