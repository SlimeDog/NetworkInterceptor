package me.lucko.networkinterceptor.plugin;

import java.util.Collections;

import org.apache.commons.lang.Validate;

public class DelagatingPluginOptions<PLUGIN> extends PluginOptions<PLUGIN> {
    private final PluginOptions<PLUGIN> one, two;

    public DelagatingPluginOptions(PluginOptions<PLUGIN> one, PluginOptions<PLUGIN> two) {
        super(one.getKeepType(), one.shouldAllowNonPlugin(), Collections.emptySet(), false);
        Validate.isTrue(one.getKeepType() == two.getKeepType(), "Need keep types to be the same");
        Validate.isTrue(one.shouldAllowNonPlugin() == two.shouldAllowNonPlugin(),
                "Need non-plugin behaviour to be the same");
        this.one = one;
        this.two = two;
    }

    @Override
    protected boolean attemptAddPlugin(String name) {
        throw new IllegalStateException("This method should not be called");
    }

    @Override
    public boolean isTrusted(PLUGIN plugin) {
        return one.isTrusted(plugin) && two.isTrusted(plugin);
    }

    @Override
    public boolean isListedAsTrustedPluginName(String pluginName) {
        return one.isListedAsTrustedPluginName(pluginName) && two.isListedAsTrustedPluginName(pluginName);
    }

}
