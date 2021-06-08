package me.lucko.networkinterceptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

public class InterceptEvent {
    private final String host;
    private final StackTraceElement[] stackTrace;
    private final Map<StackTraceElement, JavaPlugin> nonInternalStackTrace = new LinkedHashMap<>();

    public InterceptEvent(String host, StackTraceElement[] stackTrace) {
        this.host = host;
        this.stackTrace = stackTrace;
        generateNonInternalStackTrace();
    }

    public String getHost() {
        return this.host;
    }

    public StackTraceElement[] getStackTrace() {
        return this.stackTrace;
    }

    public Map<StackTraceElement, JavaPlugin> getNonInternalStackTraceWithPlugins() {
        return Collections.unmodifiableMap(nonInternalStackTrace);
    }

    private void generateNonInternalStackTrace() {
        boolean shouldPrint = false;
        for (StackTraceElement element : stackTrace) {
            if (!shouldPrint) {
                boolean isInternal = element.getClassName().startsWith("me.lucko.networkinterceptor")
                        || element.getClassName().startsWith("java.net")
                        || element.getClassName().startsWith("java.security")
                        || element.getClassName().startsWith("sun.net")
                        || element.getClassName().startsWith("sun.security.ssl");

                if (!isInternal) {
                    shouldPrint = true;
                }
            }

            if (shouldPrint) {
                // sb.append("\tat ").append(element);
                JavaPlugin providingPlugin;
                try {
                    // append the name of the plugin
                    Class<?> clazz = Class.forName(element.getClassName());
                    providingPlugin = JavaPlugin.getProvidingPlugin(clazz);
                    // sb.append(" [").append(providingPlugin.getName()).append(']');
                } catch (Exception e) {
                    // ignore
                    providingPlugin = null;
                }
                nonInternalStackTrace.put(element, providingPlugin);
            }
        }
    }
}
