package me.lucko.networkinterceptor;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

import me.lucko.networkinterceptor.common.Platform;

public class InterceptEvent<PLUGIN> {
    private static final int MAX_INTERNAL_TRACES = 2;
    private final String host;
    private final StackTraceElement[] stackTrace;
    private final Map<StackTraceElement, PLUGIN> nonInternalStackTrace = new LinkedHashMap<>();
    private final Set<PLUGIN> tracedPlugins = new LinkedHashSet<>();
    private final Platform platform;
    private final PluginFinder<PLUGIN> pluginFinder;
    private String originalHost;
    private boolean isRepeat = false; // is repeat if has original host or repeat connection to the same host
    private PLUGIN trustedPlugin;
    private PLUGIN blockedPlugin;

    public InterceptEvent(String host, StackTraceElement[] stackTrace, Platform platform) {
        this(host, stackTrace, platform, null);
    }

    public InterceptEvent(String host, StackTraceElement[] stackTrace, Platform platform, PluginFinder<PLUGIN> finder) {
        this.host = host;
        this.stackTrace = stackTrace;
        this.platform = platform;
        if (platform == Platform.BUKKIT) {
            pluginFinder = new BukkitPluginFinder();
        } else if (platform == Platform.BUNGEE) {
            pluginFinder = new BungeePluginFinder();
        } else if (platform == Platform.VELOCITY) {
            // TODO - make velocity implementation
            pluginFinder = new DummyPluginFinder();
        } else { // passed finder if available
            pluginFinder = finder;
        }
        generateNonInternalStackTrace();
    }

    public String getHost() {
        return this.host;
    }

    public StackTraceElement[] getStackTrace() {
        return this.stackTrace;
    }

    public void updateTraceElement(StackTraceElement trace, PLUGIN plugin) {
        if (!nonInternalStackTrace.containsKey(trace)) {
            throw new IllegalArgumentException("Stack trace not within event non-internal trace: " + trace);
        }
        PLUGIN prev = nonInternalStackTrace.get(trace);
        if (prev != null) {
            throw new IllegalStateException("Stack trace " + trace + " already mapped to " + prev);
        }
        if (plugin == null) {
            throw new IllegalArgumentException("Cannot update trace to null plugin");
        }
        nonInternalStackTrace.put(trace, plugin);
    }

    public Map<StackTraceElement, PLUGIN> getNonInternalStackTraceWithPlugins() {
        return Collections.unmodifiableMap(nonInternalStackTrace);
    }

    public Set<PLUGIN> getOrderedTracedPlugins() {
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
                PLUGIN providingPlugin = getProvidingPlugin(element);
                if (providingPlugin != null) {
                    tracedPlugins.add(providingPlugin);
                }
                nonInternalStackTrace.put(element, providingPlugin);
            }
        }
    }

    private PLUGIN getProvidingPlugin(StackTraceElement element) {
        if (pluginFinder != null) {
            return pluginFinder.findPlugin(element);
        }
        throw new IllegalStateException("Plugin finder not defined! Platform: " + platform);
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

    public void setTrustedPlugin(PLUGIN plugin) {
        this.trustedPlugin = plugin;
    }

    public PLUGIN getTrustedPlugin() {
        return trustedPlugin;
    }

    public void setBlockedPlugin(PLUGIN plugin) {
        this.blockedPlugin = plugin;
    }

    public PLUGIN getBlockedPlugin() {
        return blockedPlugin;
    }

    public static interface PluginFinder<PLUGIN> {

        PLUGIN findPlugin(StackTraceElement element);

    }

    private class BukkitPluginFinder implements PluginFinder<PLUGIN> {

        @Override
        @SuppressWarnings("unchecked")
        public PLUGIN findPlugin(StackTraceElement element) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                return (PLUGIN) JavaPlugin.getProvidingPlugin(clazz); // unchecked
            } catch (Exception e) {
                return null;
            }
        }

    }

    private class BungeePluginFinder implements PluginFinder<PLUGIN> {
        private final Class<?> pluginClassloaderClass;
        private final Field pluginField;

        private BungeePluginFinder() {
            try {
                this.pluginClassloaderClass = Class.forName("net.md_5.bungee.api.plugin.PluginClassloader");
                this.pluginField = pluginClassloaderClass.getDeclaredField("plugin");
                this.pluginField.setAccessible(true);
            } catch (ClassNotFoundException | NoSuchFieldException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public PLUGIN findPlugin(StackTraceElement element) {
            Class<?> clazz;
            try {
                clazz = Class.forName(element.getClassName());
            } catch (ClassNotFoundException e1) {
                return null;
            }
            ClassLoader cl = clazz.getClassLoader();
            if (cl == null || !cl.getClass().isAssignableFrom(pluginClassloaderClass)) {
                return null;
            }
            try {
                return (PLUGIN) pluginField.get(cl); // unchecked cast
            } catch (IllegalArgumentException | IllegalAccessException e) {
                System.err.println("Problem finding BungeeCoord plugin for network connection:");
                e.printStackTrace();
                return null;
            }
        }
    }

    private class DummyPluginFinder implements PluginFinder<PLUGIN> {

        @Override
        public PLUGIN findPlugin(StackTraceElement element) {
            return null; // nothing to find
        }

    }

}
