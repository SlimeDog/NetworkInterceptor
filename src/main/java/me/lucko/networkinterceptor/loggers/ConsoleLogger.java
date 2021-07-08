package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;

import java.util.logging.Logger;

public class ConsoleLogger extends AbstractEventLogger {
    private final NetworkInterceptorPlugin plugin;

    public ConsoleLogger(NetworkInterceptorPlugin plugin, boolean includeTraces) {
        super(includeTraces);
        this.plugin = plugin;
    }

    @Override
    protected Logger getLogger() {
        return this.plugin.getLogger();
    }
}
