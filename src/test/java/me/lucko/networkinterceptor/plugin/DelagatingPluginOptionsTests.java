package me.lucko.networkinterceptor.plugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DelagatingPluginOptionsTests {

    @Test
    void trustsListedPlugin() {
        LocalPluginOptions allowed = LocalPluginOptions.allow(new Plugin("NAMED_TEST_PLUGIN_1"));
        allowed.searchForPlugins(null);
        LocalPluginOptions blocked = LocalPluginOptions.block();
        TrustedAndBlockedOptions<Plugin> parent = new TrustedAndBlockedOptions<>(allowed, blocked);
        Assertions.assertTrue(parent.isTrusted(allowed.potentialPlugins.iterator().next()),
                "Plugin not listed after being added");
    }

    @Test
    void blocksListedPlugin() {
        LocalPluginOptions allowed = LocalPluginOptions.allow();
        LocalPluginOptions blocked = LocalPluginOptions.block(new Plugin("NAMED_TEST_PLUGIN_2"));
        blocked.searchForPlugins(null);
        TrustedAndBlockedOptions<Plugin> parent = new TrustedAndBlockedOptions<>(allowed, blocked);
        Assertions.assertFalse(parent.isTrusted(blocked.potentialPlugins.iterator().next()),
                "Plugin is listed after being added as blocked");
    }

    @Test
    void allowsNonListedPlugin() {
        LocalPluginOptions allowed = LocalPluginOptions.allow();
        LocalPluginOptions blocked = LocalPluginOptions.block(new Plugin("NAMED_TEST_PLUGIN_3_BLOCKED"));
        blocked.searchForPlugins(null);
        Plugin plugin = new Plugin("NAMED_TEST_PLUGIN_3_NOT_BLOCKED");
        TrustedAndBlockedOptions<Plugin> parent = new TrustedAndBlockedOptions<>(allowed, blocked);
        Assertions.assertFalse(parent.isTrusted(plugin),
                "Plugin is listed after being added as blocked");
    }

    public static final class LocalPluginOptions extends PluginOptions<Plugin> {
        private final Set<Plugin> potentialPlugins;

        public LocalPluginOptions(KeepPlugins keepType, boolean allowNonPlugin, Set<String> plugins, boolean trust,
                Set<Plugin> potentialPlugins) {
            super(keepType, allowNonPlugin, plugins, trust);
            this.potentialPlugins = potentialPlugins;
        }

        @Override
        protected boolean attemptAddPlugin(String name) {
            for (Plugin plugin : potentialPlugins) {
                if (plugin.name.equals(name)) {
                    this.plugins.add(plugin);
                    return true;
                }
            }
            return false;
        }

        public static LocalPluginOptions of(boolean trust, Plugin... pluginArray) {
            Set<Plugin> plugins = new HashSet<>(Arrays.asList(pluginArray));
            return new LocalPluginOptions(KeepPlugins.ALL, true,
                    plugins.stream().map(p -> p.name).collect(Collectors.toSet()), trust, plugins);
        }

        public static LocalPluginOptions allow(Plugin... pluginArray) {
            return of(true, pluginArray);
        }

        public static LocalPluginOptions block(Plugin... pluginArray) {
            return of(false, pluginArray);
        }

    }

    public static final class Plugin {
        private final String name;

        public Plugin(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Plugin)) {
                return false;
            }
            return ((Plugin) other).name.equals(this.name);
        }

        @Override
        public String toString() {
            return String.format("{TEST PLUGIN NAMED '%s'}", name);
        }

    }

}
