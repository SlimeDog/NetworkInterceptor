package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;

import java.util.logging.Logger;

public class ConsoleLogger<PLUGIN> extends AbstractEventLogger<PLUGIN> {
    private final NetworkInterceptorPlugin<PLUGIN> plugin;

    public ConsoleLogger(NetworkInterceptorPlugin<PLUGIN> plugin, boolean includeTraces) {
        super(includeTraces, plugin.isBungee(), plugin.isVelocity());
        this.plugin = plugin;
    }

    @Override
    protected Logger getLogger() {
        return this.plugin.getLogger();
    }
}
