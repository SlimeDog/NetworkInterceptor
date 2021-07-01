package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.InterceptEvent;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
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

        StringBuilder sb = new StringBuilder("Intercepted connection to ").append(host);
        String origHost = event.getOriginalHost();
        if (origHost != null) {
            sb.append(" (").append(origHost).append(")");
        }
        appendPluginIfPossible(sb, event);
        sb.append("\n");

        // print stacktrace
        if (this.includeTraces && !event.isRepeatCall()) {
            Map<StackTraceElement, JavaPlugin> map = event.getNonInternalStackTraceWithPlugins();
            for (StackTraceElement element : map.keySet()) {
                sb.append("\tat ").append(element);
                JavaPlugin providingPlugin = map.get(element);
                if (providingPlugin != null) {
                    sb.append(" [").append(providingPlugin.getName()).append(']');
                }
                sb.append("\n");
            }
        } else if (this.includeTraces) {
            sb.append("\tat (identical stack trace omitted)\n");
        }

        sb.setLength(sb.length() - 1);
        getLogger().info(sb.toString());
    }

    private void appendPluginIfPossible(StringBuilder sb, InterceptEvent event) {
        JavaPlugin trustedPlugin = event.getTrustedPlugin();
        if (trustedPlugin != null) {
            sb.append(" by trusted-plugin ").append(trustedPlugin.getName());
        } else {
            Set<JavaPlugin> traced = event.getOrderedTracedPlugins();
            if (!traced.isEmpty()) {
                sb.append(" by plugin ").append(traced.iterator().next().getName());
            }
        }
    }

    @Override
    public void logBlock(InterceptEvent event) {
        StringBuilder sb = new StringBuilder("Blocked connection to ");
        sb.append(event.getHost());
        String origHost = event.getOriginalHost();
        if (origHost != null) {
            sb.append(" (").append(origHost).append(")");
        }
        appendPluginIfPossible(sb, event);
        getLogger().info(sb.toString());
    }
}
