package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.InterceptEvent;
import net.md_5.bungee.api.plugin.Plugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public abstract class AbstractEventLogger<PLUGIN> implements EventLogger<PLUGIN> {
    private final boolean includeTraces;
    private final boolean isBungee;

    protected AbstractEventLogger(boolean includeTraces, boolean isBungee) {
        this.includeTraces = includeTraces;
        this.isBungee = isBungee;
    }

    protected abstract Logger getLogger();

    @Override
    public void logAttempt(InterceptEvent<PLUGIN> event) {
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
            Map<StackTraceElement, PLUGIN> map = event.getNonInternalStackTraceWithPlugins();
            for (StackTraceElement element : map.keySet()) {
                sb.append("\tat ").append(element);
                if (!isBungee) {
                    JavaPlugin providingPlugin = (JavaPlugin) map.get(element);
                    if (providingPlugin != null) {
                        sb.append(" [").append(providingPlugin.getName()).append(']');
                    }
                }
                sb.append("\n");
            }
        } else if (this.includeTraces) {
            sb.append("\tat (identical stack trace omitted)\n");
        }

        sb.setLength(sb.length() - 1);
        getLogger().info(sb.toString());
    }

    private void appendPluginIfPossible(StringBuilder sb, InterceptEvent<PLUGIN> event) {
        PLUGIN trustedPlugin = event.getTrustedPlugin();
        if (trustedPlugin != null) {
            sb.append(" by trusted-plugin ");
            if (!isBungee) {
                sb.append(((JavaPlugin) trustedPlugin).getName());
            } else {
                sb.append(((Plugin) trustedPlugin).getDescription().getName());
            }
        } else {
            Set<PLUGIN> traced = event.getOrderedTracedPlugins();
            if (!traced.isEmpty()) {
                sb.append(" by plugin ");
                PLUGIN next = traced.iterator().next();
                if (!isBungee) {
                    sb.append(((JavaPlugin) next).getName());
                } else {
                    sb.append(((Plugin) next).getDescription().getName());
                }
            }
        }
    }

    @Override
    public void logBlock(InterceptEvent<PLUGIN> event) {
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
