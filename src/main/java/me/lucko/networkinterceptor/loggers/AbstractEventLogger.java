package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.InterceptEvent;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.logging.Logger;

public abstract class AbstractEventLogger implements EventLogger {
    private final boolean includeTraces;

    protected AbstractEventLogger(boolean includeTraces) {
        this.includeTraces = includeTraces;
    }

    protected abstract Logger getLogger();

    @Override
    public void logAttempt(InterceptEvent event) {
        String host = event.getHost();

        StringBuilder sb = new StringBuilder("Intercepted outgoing connection to host '").append(host).append("'\n");

        // print stacktrace
        if (this.includeTraces) {
            Map<StackTraceElement, JavaPlugin> map = event.getNonInternalStackTraceWithPlugins();
            for (StackTraceElement element : map.keySet()) {
                sb.append("\tat ").append(element);
                JavaPlugin providingPlugin = map.get(element);
                sb.append(" [").append(providingPlugin == null ? "N/A" : providingPlugin.getName()).append(']');
                sb.append("\n");
            }
        }

        sb.setLength(sb.length() - 1);
        getLogger().info(sb.toString());
    }

    @Override
    public void logBlock(InterceptEvent event) {
        getLogger().info("Blocked connection to host '" + event.getHost() + "'");
    }
}
