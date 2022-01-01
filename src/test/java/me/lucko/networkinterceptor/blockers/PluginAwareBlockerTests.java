package me.lucko.networkinterceptor.blockers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import me.lucko.networkinterceptor.plugin.DelagatingPluginOptionsTests.Plugin;
import me.lucko.networkinterceptor.plugin.DelagatingPluginOptionsTests.LocalPluginOptions;
import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.InterceptEvent.PluginFinder;
import me.lucko.networkinterceptor.common.Platform;
import me.lucko.networkinterceptor.plugin.PluginOptions;
import me.lucko.networkinterceptor.plugin.TrustedAndBlockedOptions;

public class PluginAwareBlockerTests {

    @Test
    public void test_RecognizesTrustedPlugin() {
        Plugin trustedPlugin = new Plugin("Some Trusted Plugin 1");
        PluginOptions<Plugin> trusted = LocalPluginOptions.allow(trustedPlugin);
        trusted.searchForPlugins(null);
        PluginOptions<Plugin> blocked = LocalPluginOptions.block();
        TrustedAndBlockedOptions<Plugin> options = new TrustedAndBlockedOptions<>(trusted, blocked);
        PluginAwareBlocker<Plugin> blocker = new PluginAwareBlocker<>(options);
        StackTraceElement trustedPluginElement = new StackTraceElement("TESTCLASS", "TESTMODULE", "TESTMODULEVERSION",
                "TESTDECLARINGCLASS",
                "TESTMETHOD", "TESTFILENAME", -1);
        TestPluginFinder finder = new TestPluginFinder(trustedPluginElement, trustedPlugin);
        InterceptEvent<Plugin> event = new InterceptEvent<>("host.host.host", new StackTraceElement[] {
                trustedPluginElement
        }, Platform.OTHER, finder);
        Assertions.assertFalse(blocker.hasBlockedPlugins(event), "Should not have a blocked plugin");
        Assertions.assertTrue(blocker.hasTrustedPlugins(event), "Should have a trusted plugin");
    }

    @Test
    public void test_RecognizesBlockedPlugin() {
        Plugin blockedPlugin = new Plugin("Some Blocked Plugin 2");
        PluginOptions<Plugin> trusted = LocalPluginOptions.allow();
        PluginOptions<Plugin> blocked = LocalPluginOptions.block(blockedPlugin);
        blocked.searchForPlugins(null);
        TrustedAndBlockedOptions<Plugin> options = new TrustedAndBlockedOptions<>(trusted, blocked);
        PluginAwareBlocker<Plugin> blocker = new PluginAwareBlocker<>(options);
        StackTraceElement blockedPluginElement = new StackTraceElement("TESTCLASS2", "TESTMODULE2",
                "TESTMODULEVERSION2",
                "TESTDECLARINGCLASS2",
                "TESTMETHOD2", "TESTFILENAME2", -2);
        TestPluginFinder finder = new TestPluginFinder(blockedPluginElement, blockedPlugin);
        InterceptEvent<Plugin> event = new InterceptEvent<>("host.host.host", new StackTraceElement[] {
                blockedPluginElement
        }, Platform.OTHER, finder);
        Assertions.assertTrue(blocker.hasBlockedPlugins(event), "Should have a blocked plugin");
        Assertions.assertFalse(blocker.hasTrustedPlugins(event), "Should not have a trusted plugin");
    }

    public static final class TestPluginFinder implements PluginFinder<Plugin> {
        private final StackTraceElement element;
        private final Plugin owner;

        public TestPluginFinder(StackTraceElement element, Plugin owner) {
            this.element = element;
            this.owner = owner;
        }

        @Override
        public Plugin findPlugin(StackTraceElement element) {
            if (element == this.element) {
                return owner;
            }
            return null;
        }

    }

}
