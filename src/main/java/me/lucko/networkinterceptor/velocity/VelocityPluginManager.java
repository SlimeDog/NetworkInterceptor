package me.lucko.networkinterceptor.velocity;

import java.util.Optional;

import com.velocitypowered.api.plugin.PluginContainer;

import me.lucko.networkinterceptor.common.PluginManager;

public class VelocityPluginManager implements PluginManager<PluginContainer> {
    private final com.velocitypowered.api.plugin.PluginManager delegate;

    public VelocityPluginManager(com.velocitypowered.api.plugin.PluginManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName(PluginContainer plugin) {
        Optional<String> name = plugin.getDescription().getName();
        if (name.isPresent()) {
            return name.get();
        }
        return plugin.getDescription().getId();
    }

    @Override
    public PluginContainer getPlugin(String name) {
        Optional<PluginContainer> plugin = delegate.getPlugin(name);
        return plugin.isPresent() ? plugin.get() : null;
    }

}
