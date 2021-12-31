package me.lucko.networkinterceptor.blockers;

import java.util.Collection;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.plugin.PluginOptions;
import me.lucko.networkinterceptor.plugin.TrustedAndBlockedOptions;

public class PluginAwareBlocker<PLUGIN> {
    private final TrustedAndBlockedOptions<PLUGIN> options;

    public PluginAwareBlocker(TrustedAndBlockedOptions<PLUGIN> options) {
        this.options = options;
    }

    public boolean hasBlockedPlugins(InterceptEvent<PLUGIN> event) {
        Collection<PLUGIN> plugins = event.getOrderedTracedPlugins();
        if (plugins.isEmpty()) {
            return false;
        }
        return !isTrustedIn(event, plugins, options.getBlockedOptions(), TrustType.BLOCK_ON_FIRST_UNTRUST);
    }

    public boolean hasTrustedPlugins(InterceptEvent<PLUGIN> event) {
        Collection<PLUGIN> plugins = event.getOrderedTracedPlugins();
        if (plugins.isEmpty()) {
            return false;
        }
        return isTrustedIn(event, plugins, options.getTrustedOptions(), TrustType.ALLOW_ON_FIRST_TRUST);
    }

    private boolean isTrustedIn(InterceptEvent<PLUGIN> event, Collection<PLUGIN> plugins,
            PluginOptions<PLUGIN> options, TrustType trust) {
        for (PLUGIN plugin : plugins) {
            boolean curTrusted = options.isTrusted(plugin);
            if (curTrusted && trust == TrustType.ALLOW_ON_FIRST_TRUST) {
                event.setTrustedPlugin(plugin);
                return true;
            } else if (!curTrusted && trust == TrustType.BLOCK_ON_FIRST_UNTRUST) {
                event.setBlockedPlugin(plugin);
                return false;
            }
        }
        return trust.getTrustedDefault();
    }

    private enum TrustType {
        ALLOW_ON_FIRST_TRUST, // for trusted plugins
        BLOCK_ON_FIRST_UNTRUST; // for blocked pluggins

        public boolean getTrustedDefault() {
            switch (this) {
                case ALLOW_ON_FIRST_TRUST:
                    return false;
                case BLOCK_ON_FIRST_UNTRUST:
                    return true;
            }
            throw new IllegalStateException("Unedfined trust type: " + this);
        }
    }

}
