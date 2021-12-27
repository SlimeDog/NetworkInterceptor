package me.lucko.networkinterceptor.velocity;

import java.util.Optional;
import java.util.Set;

import com.velocitypowered.api.plugin.PluginContainer;

import me.lucko.networkinterceptor.plugin.KeepPlugins;
import me.lucko.networkinterceptor.plugin.PluginOptions;

public class VelocityPluginOptions extends PluginOptions<PluginContainer> {
    private final VelocityNetworkInterceptor owner;

    public VelocityPluginOptions(VelocityNetworkInterceptor owner, KeepPlugins keepType, boolean allowNonPlugin,
            Set<String> plugins, boolean trust) {
        super(keepType, allowNonPlugin, plugins, trust);
        this.owner = owner;
    }

    @Override
    protected boolean attemptAddPlugin(String name) {
        Optional<PluginContainer> plugin = owner.getServer().getPluginManager().getPlugin(name);
        if (plugin == null || !plugin.isPresent()) {
            return false;
        }
        this.plugins.add(plugin.get());
        return true;
    }

}
