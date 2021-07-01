package me.lucko.networkinterceptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

public class InterceptEvent {
    private static final int MAX_INTERNAL_TRACES = 2;
    private final String host;
    private final StackTraceElement[] stackTrace;
    private final Map<StackTraceElement, JavaPlugin> nonInternalStackTrace = new LinkedHashMap<>();
    private final Set<JavaPlugin> tracedPlugins = new LinkedHashSet<>();
    private String originalHost;
    private boolean isRepeat = false; // is repeat if has original host or repeat connection to the same host
    private JavaPlugin trustedPlugin;

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

    public void updateTraceElement(StackTraceElement trace, JavaPlugin plugin) {
        if (!nonInternalStackTrace.containsKey(trace)) {
            throw new IllegalArgumentException("Stack trace not within event non-internal trace: " + trace);
        }
        JavaPlugin prev = nonInternalStackTrace.get(trace);
        if (prev != null) {
            throw new IllegalStateException("Stack trace " + trace + " already mapped to " + prev);
        }
        if (plugin == null) {
            throw new IllegalArgumentException("Cannot update trace to null plugin");
        }
        nonInternalStackTrace.put(trace, plugin);
    }

    public Map<StackTraceElement, JavaPlugin> getNonInternalStackTraceWithPlugins() {
        return Collections.unmodifiableMap(nonInternalStackTrace);
    }

    public Set<JavaPlugin> getOrderedTracedPlugins() {
        return new LinkedHashSet<>(tracedPlugins);
    }

    private void generateNonInternalStackTrace() {
        boolean shouldPrint = false;
        int internalTraces = 0;
        for (StackTraceElement element : stackTrace) {
            if (!shouldPrint) {
                boolean internalToPlugin = element.getClassName().startsWith("me.lucko.networkinterceptor");
                boolean isInternal = internalToPlugin || element.getClassName().startsWith("java.net")
                        || element.getClassName().startsWith("java.security")
                        || element.getClassName().startsWith("sun.net")
                        || element.getClassName().startsWith("sun.security.ssl");

                if (!isInternal) {
                    shouldPrint = true;
                } else if (internalToPlugin && internalTraces++ >= MAX_INTERNAL_TRACES) {
                    shouldPrint = true;
                }
            }

            if (shouldPrint) {
                JavaPlugin providingPlugin;
                try {
                    Class<?> clazz = Class.forName(element.getClassName());
                    providingPlugin = JavaPlugin.getProvidingPlugin(clazz);
                    tracedPlugins.add(providingPlugin);
                } catch (Exception e) {
                    // ignore
                    providingPlugin = null;
                }
                nonInternalStackTrace.put(element, providingPlugin);
            }
        }
    }

    public void setOriginalHost(String host) {
        isRepeat = true;
        if (host != null && host.equals(this.host)) {
            return; // do not set exactly the same original host
        }
        this.originalHost = host;
    }

    public boolean isRepeatCall() {
        return isRepeat;
    }

    public String getOriginalHost() {
        return originalHost;
    }

    public void setTrustedPlugin(JavaPlugin plugin) {
        this.trustedPlugin = plugin;
    }

    public JavaPlugin getTrustedPlugin() {
        return trustedPlugin;
    }

}
