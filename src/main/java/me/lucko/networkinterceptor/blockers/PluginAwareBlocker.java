package me.lucko.networkinterceptor.blockers;

import java.util.Collection;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.plugin.PluginOptions;

public class PluginAwareBlocker<PLUGIN> implements Blocker<PLUGIN> {
    private final PluginOptions<PLUGIN> options;

    public PluginAwareBlocker(PluginOptions<PLUGIN> options) {
        this.options = options;
    }

    @Override
    public boolean shouldBlock(InterceptEvent<PLUGIN> event) {
        Collection<PLUGIN> plugins = event.getOrderedTracedPlugins();
        if (plugins.isEmpty()) {
            return !options.shouldAllowNonPlugin();
        }
        for (PLUGIN plugin : plugins) { // TODO - make more versatile. Right now the FIRST trusted plugin will
                                        // suffice
            if (options.isTrusted(plugin)) {
                event.setTrustedPlugin(plugin);
                return false; // trusted!
            }
        }
        return true; // not trusted -> block
    }

}
