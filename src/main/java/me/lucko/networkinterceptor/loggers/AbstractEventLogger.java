package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.InterceptEvent;

import org.bukkit.plugin.java.JavaPlugin;

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
        StackTraceElement[] stackTrace = event.getStackTrace();

        StringBuilder sb = new StringBuilder("Intercepted outgoing connection to host '").append(host).append("'\n");

        // print stacktrace
        if (this.includeTraces) {
            boolean shouldPrint = false;
            for (StackTraceElement element : stackTrace) {
                if (!shouldPrint) {
                    boolean isInternal = element.getClassName().startsWith("me.lucko.networkinterceptor") ||
                            element.getClassName().startsWith("java.net") ||
                            element.getClassName().startsWith("java.security") ||
                            element.getClassName().startsWith("sun.net") ||
                            element.getClassName().startsWith("sun.security.ssl");

                    if (!isInternal) {
                        shouldPrint = true;
                    }
                }

                if (shouldPrint) {
                    sb.append("\tat ").append(element);
                    try {
                        // append the name of the plugin
                        Class<?> clazz = Class.forName(element.getClassName());
                        JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(clazz);
                        sb.append(" [").append(providingPlugin.getName()).append("]");
                    } catch (Exception e) {
                        // ignore
                    }
                    sb.append("\n");
                }
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
