package me.lucko.networkinterceptor.blockers;

import java.util.Arrays;

import me.lucko.networkinterceptor.InterceptEvent;

public class CompositeBlocker<PLUGIN> implements Blocker<PLUGIN> {
    private final ManualPluginDetectingBlocker<PLUGIN> manual;
    private final Blocker<PLUGIN>[] delegates;
    private boolean useManualBlocker = true;
    private final PluginAwareBlocker<PLUGIN> pluginAwareBlocker;

    @SafeVarargs // hopefully
    public CompositeBlocker(ManualPluginDetectingBlocker<PLUGIN> manualBlocker,
            PluginAwareBlocker<PLUGIN> pluginAwareBlocker, Blocker<PLUGIN>... delegates) {
        this.manual = manualBlocker;
        if (manual == null) {
            useManualBlocker = false;
        }
        this.pluginAwareBlocker = pluginAwareBlocker;
        this.delegates = delegates;
    }

    @Override
    public boolean shouldBlock(InterceptEvent<PLUGIN> event) {
        if (useManualBlocker && !manual.shouldBlock(event)) {
            return false; // allowed by manual plugin detecting blockeru
        }
        if (pluginAwareBlocker.hasTrustedPlugins(event)) {
            return false; // allow
        }
        if (pluginAwareBlocker.hasBlockedPlugins(event)) {
            return true; // block
        }
        for (Blocker<PLUGIN> delegate : delegates) {
            if (delegate == this.pluginAwareBlocker) {
                continue; // ignore since this was checked before
            }
            boolean shouldBlock;
            try {
                shouldBlock = delegate.shouldBlock(event);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (!shouldBlock) {
                return false; // allowed by this delegate
            }
        }
        return true; // not allowed by any delegate -> block
    }

    public Blocker<PLUGIN>[] getDelegates() {
        return Arrays.copyOf(delegates, delegates.length);
    }

    public void stopUsingManualBlocker() {
        useManualBlocker = false;
    }

}
