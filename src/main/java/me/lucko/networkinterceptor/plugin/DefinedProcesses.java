package me.lucko.networkinterceptor.plugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum DefinedProcesses {
    MOJANG_PLAYER_AUTHENTICATION, MINECRAFT_VERSION_CHECK, PLUGIN_VERSION_CHECK;

    private static final Set<String> PLUGIN_VERSION_CHECK_HOST_NAMES = new HashSet<>(Arrays.asList("api.spigotmc.org"));
    private static final Set<String> MOJANG_SESSION_HOST_NAMES = new HashSet<>(
            Arrays.asList("sessionserver.mojang.com"));

    public boolean isOfType(StackTraceElement element, String host) {
        switch (this) {
            case PLUGIN_VERSION_CHECK:
                if (PLUGIN_VERSION_CHECK_HOST_NAMES.contains(host)) {
                    return true;
                }
                // TODO - something else?
                break;
            case MOJANG_PLAYER_AUTHENTICATION:
                if (MOJANG_SESSION_HOST_NAMES.contains(host)) {
                    return true;
                }
                // TODO - something else?
                break;
            case MINECRAFT_VERSION_CHECK:
                break; // TODO
        }
        return false;
    }

    public static DefinedProcesses getDefinedProcess(String name) {
        name = name.replace("-", "_").toUpperCase();
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
