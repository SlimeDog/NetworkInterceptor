package me.lucko.networkinterceptor.bungee;

import java.util.HashSet;
import java.util.Set;

import me.lucko.networkinterceptor.plugin.KeepPlugins;
import me.lucko.networkinterceptor.plugin.PluginOptions;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePluginOptions<T extends Plugin> extends PluginOptions<T> {
    private final Plugin owner;
    private final Set<Plugin> plugins = new HashSet<>();

    public BungeePluginOptions(Plugin owner, KeepPlugins keepType, boolean allowNonPlugin, Set<String> trustedPlugins) {
        this(owner, keepType, allowNonPlugin, trustedPlugins, true);
    }

    public BungeePluginOptions(Plugin owner, KeepPlugins keepType, boolean allowNonPlugin, Set<String> plugins,
            boolean trust) {
        super(keepType, allowNonPlugin, plugins, trust);
        this.owner = owner;
    }

    @Override
    protected boolean attemptAddPlugin(String name) {
        Plugin plugin = owner.getProxy().getPluginManager().getPlugin(name);
        if (plugin == null) {
            return false;
        }
        this.plugins.add(plugin);
        return true;
    }

    @Override
    public boolean isTrusted(T plugin) {
        return plugins.contains(plugin);
    }

}
