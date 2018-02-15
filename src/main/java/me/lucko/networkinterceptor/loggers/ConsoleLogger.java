package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.NetworkInterceptor;

import java.util.logging.Logger;

public class ConsoleLogger extends AbstractEventLogger {
    private final NetworkInterceptor plugin;

    public ConsoleLogger(NetworkInterceptor plugin, boolean includeTraces) {
        super(includeTraces);
        this.plugin = plugin;
    }

    @Override
    protected Logger getLogger() {
        return this.plugin.getLogger();
    }
}
