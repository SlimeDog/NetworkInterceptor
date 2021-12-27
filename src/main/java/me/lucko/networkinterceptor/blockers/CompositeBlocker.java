package me.lucko.networkinterceptor.blockers;

import java.util.Arrays;

import me.lucko.networkinterceptor.InterceptEvent;

public class CompositeBlocker<PLUGIN> implements Blocker<PLUGIN> {
    private final ManualPluginDetectingBlocker<PLUGIN> manual;
    private final Blocker<PLUGIN>[] delegates;
    private boolean useManualBlocker = true;
    private final boolean hasPluginAwareBlocker;
    private final PluginAwareBlocker<PLUGIN> pluginAwareBlocker;

    @SafeVarargs // hopefully
    public CompositeBlocker(ManualPluginDetectingBlocker<PLUGIN> manualBlocker, Blocker<PLUGIN>... delegates) {
        this.manual = manualBlocker;
        if (manual == null) {
            useManualBlocker = false;
        }
        this.delegates = delegates;
        boolean containsPluginAwareBlocker = false;
        PluginAwareBlocker<PLUGIN> thePluginAwareBlocker = null;
        for (Blocker<PLUGIN> blocker : this.delegates) {
            if (blocker instanceof PluginAwareBlocker) {
                containsPluginAwareBlocker = true;
                thePluginAwareBlocker = (PluginAwareBlocker<PLUGIN>) blocker;
                break; // assume only 1
            }
        }
        this.hasPluginAwareBlocker = containsPluginAwareBlocker;
        this.pluginAwareBlocker = thePluginAwareBlocker;
    }

    @Override
    public boolean shouldBlock(InterceptEvent<PLUGIN> event) {
        if (useManualBlocker && !manual.shouldBlock(event)) {
            return false; // allowed by manual plugin detecting blockeru
        }
        if (hasPluginAwareBlocker && pluginAwareBlocker.shouldBlock(event)) {
            return true;
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
