package me.lucko.networkinterceptor.blockers;

import java.util.Arrays;

import me.lucko.networkinterceptor.InterceptEvent;

public class CompositeBlocker<PLUGIN> implements Blocker<PLUGIN> {
    private final ManualPluginDetectingBlocker<PLUGIN> manual;
    private final Blocker<PLUGIN>[] delegates;
    private boolean useManualBlocker = true;

    @SafeVarargs // hopefully
    public CompositeBlocker(ManualPluginDetectingBlocker<PLUGIN> manualBlocker, Blocker<PLUGIN>... delegates) {
        this.manual = manualBlocker;
        if (manual == null) {
            useManualBlocker = false;
        }
        this.delegates = delegates;
    }

    @Override
    public boolean shouldBlock(InterceptEvent<PLUGIN> event) {
        if (useManualBlocker && !manual.shouldBlock(event)) {
            return false; // allowed by manual plugin detecting blockeru
        }
        for (Blocker<PLUGIN> delegate : delegates) {
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
