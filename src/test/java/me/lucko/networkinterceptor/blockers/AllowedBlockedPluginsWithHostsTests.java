package me.lucko.networkinterceptor.blockers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import io.netty.util.internal.ThreadLocalRandom;
import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.blockers.PluginAwareBlockerTests.TestPluginFinder;
import me.lucko.networkinterceptor.common.Platform;
import me.lucko.networkinterceptor.plugin.PluginOptions;
import me.lucko.networkinterceptor.plugin.TrustedAndBlockedOptions;
import me.lucko.networkinterceptor.plugin.DelagatingPluginOptionsTests.LocalPluginOptions;
import me.lucko.networkinterceptor.plugin.DelagatingPluginOptionsTests.Plugin;

public class AllowedBlockedPluginsWithHostsTests {

    @ParameterizedTest
    @MethodSource("provideArgsFor_test_with")
    public void test_with(List<String> trustedPlugins, List<String> blockedPlugins, List<String> hostTargets,
            boolean allowMode, String testHost, StackTraceElement testElement, Plugin testElementOwner,
            Expectation expectation) {
        List<Plugin> trustedPluginList = trustedPlugins.stream().map(name -> new Plugin(name))
                .collect(Collectors.toList());
        List<Plugin> blockedPluginList = blockedPlugins.stream().map(name -> new Plugin(name))
                .collect(Collectors.toList());
        PluginOptions<Plugin> trustedOptions = LocalPluginOptions.allow(trustedPluginList.toArray(new Plugin[0]));
        trustedOptions.searchForPlugins(null);
        PluginOptions<Plugin> blockedOptions = LocalPluginOptions.block(blockedPluginList.toArray(new Plugin[0]));
        blockedOptions.searchForPlugins(null);
        TrustedAndBlockedOptions<Plugin> options = new TrustedAndBlockedOptions<>(trustedOptions, blockedOptions);
        PluginAwareBlocker<Plugin> pluginAwareBlocked = new PluginAwareBlocker<>(options);
        TestPluginFinder finder = new TestPluginFinder(testElement, testElementOwner);
        InterceptEvent<Plugin> event = new InterceptEvent<>(testHost, new StackTraceElement[] {
                testElement
        }, Platform.OTHER, finder);
        Blocker<Plugin> modeBlocker;
        if (allowMode) {
            modeBlocker = new AllowBlocker<>(hostTargets);
        } else {
            modeBlocker = new BlockBlocker<>(hostTargets);
        }
        Expectation manualExpectation;
        if (trustedPluginList.contains(testElementOwner)) {
            manualExpectation = Expectation.PASS;
        } else if (blockedPluginList.contains(testElementOwner)) {
            manualExpectation = Expectation.BLOCK;
        } else {
            if (allowMode) {
                manualExpectation = hostTargets.contains(testHost) ? Expectation.PASS : Expectation.BLOCK;
            } else { // deny mode
                manualExpectation = hostTargets.contains(testHost) ? Expectation.BLOCK : Expectation.PASS;
            }
        }
        Blocker<Plugin> composite = new CompositeBlocker<>(null, pluginAwareBlocked, modeBlocker);
        Assertions.assertSame(expectation, manualExpectation,
                String.format("Expection %s, manual expection %s", expectation, manualExpectation)
                        + describeOptions(trustedPlugins, blockedPlugins, hostTargets, allowMode, testHost, testElement,
                                testElementOwner));
        Expectation result = composite.shouldBlock(event) ? Expectation.BLOCK : Expectation.PASS;
        Assertions.assertSame(expectation, result, String.format("Expected %s, got %s", expectation, result)
                + describeOptions(trustedPlugins, blockedPlugins, hostTargets, allowMode, testHost, testElement,
                        testElementOwner));
    }

    private static String describeOptions(List<String> trustedPlugins, List<String> blockedPlugins,
            List<String> hostTargets, boolean allowMode, String testHost, StackTraceElement testElement,
            Plugin testElementOwner) {
        return String.format(
                " OPTS:{TP: %s, BP: %s, targets: %s, mode: %s, testHost: %s, testElement: %s, testElementOwner: %s}",
                trustedPlugins, blockedPlugins, hostTargets, allowMode ? "allow" : "deny", testHost, testElement,
                testElementOwner);
    }

    private static Stream<Arguments> provideArgsFor_test_with() {
        // List<String> trustedPlugins, List<String> blockedPlugins,
        // List<String> hostTargets, boolean allowMode,
        // String testHost, StackTraceElement testElement,
        // Plugin testElementOwner, Expectation expectation
        return Stream.of(
                // no trusted, no blocked, no targets, ALLOW,
                // random host, random stack trace
                // random plugin -> BLOCK (because of allow mode)
                Arguments.of(Arrays.asList(), Arrays.asList(),
                        Arrays.asList(), true,
                        "random.host.woo", generateRandomStackTraceElement(),
                        new Plugin("RandomPlugin1"), Expectation.BLOCK),
                // no trusted, no blocked, no targets, DENY,
                // random host, random stack trace
                // random plugin -> PASS (because of deny mode)
                Arguments.of(Arrays.asList(), Arrays.asList(),
                        Arrays.asList(), false,
                        "random.host.woo", generateRandomStackTraceElement(),
                        new Plugin("RandomPlugin2"), Expectation.PASS),
                // ONE trusted, no blocked, ONE (used) target, DENY,
                // BLOCKED host, random stack trace
                // TRUSTED plugin -> PASS (because of trusted plugin)
                Arguments.of(Arrays.asList("AllowedPlugin"), Arrays.asList(),
                        Arrays.asList("random.blocked.host"), false,
                        "random.blocked.host", generateRandomStackTraceElement(),
                        new Plugin("AllowedPlugin"), Expectation.PASS),
                // ONE trusted, no blocked, ONE (used) target, ALLOW,
                // BLOCKED host, random stack trace
                // TRUSTED plugin -> PASS (because of trusted plugin but also just for host)
                Arguments.of(Arrays.asList("AllowedPlugin"), Arrays.asList(),
                        Arrays.asList("random.blocked.host"), true,
                        "random.blocked.host", generateRandomStackTraceElement(),
                        new Plugin("AllowedPlugin"), Expectation.PASS));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/test-cases.csv", numLinesToSkip = 1)
    void test_with_config_entries(String trustedPlugins, String blockedPlugins, String hostTargets, String allowMode,
            String testHost, String plguinUnderTest, String expectation) {
        List<String> trustedPluginNameList = Arrays.asList(trustedPlugins.split(","));
        List<String> blockedPluginNameList = Arrays.asList(blockedPlugins.split(","));
        List<String> hostTargetNames = Arrays.asList(hostTargets.split(","));
        boolean allow;
        switch (allowMode.toLowerCase()) {
            case "allow":
            case "pass":
                allow = true;
                break;
            case "block":
            case "deny":
                allow = false;
                break;
            default:
                Assertions.assertTrue(true, "Unknown allow type provided: " + allowMode);
                return;
        }
        Plugin testPlugin = new Plugin(plguinUnderTest);
        Expectation expect;
        switch (expectation) {
            case "allow":
            case "pass":
                expect = Expectation.PASS;
                break;
            case "block":
            case "deny":
                expect = Expectation.BLOCK;
                break;
            default:
                Assertions.assertTrue(true, "Unknown expectation provided: " + expectation);
                return;
        }
        test_with(trustedPluginNameList, blockedPluginNameList, hostTargetNames, allow, testHost,
                generateRandomStackTraceElement(), testPlugin, expect);
    }

    private static StackTraceElement generateRandomStackTraceElement() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        return new StackTraceElement("classLoaderName" + rnd.nextInt(), "moduleName" + rnd.nextInt(),
                "moduleVersion" + rnd.nextInt(), "declaringClass" + rnd.nextInt(), "methodName" + rnd.nextInt(),
                "fileName" + rnd.nextInt(), rnd.nextInt());
    }

    private static enum Expectation {
        PASS, BLOCK
    }

}
