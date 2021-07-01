package me.lucko.networkinterceptor.blockers;

import java.util.Arrays;

import me.lucko.networkinterceptor.InterceptEvent;

public class CompositeBlocker implements Blocker {
    private final ManualPluginDetectingBlocker manual;
    private final Blocker[] delegates;
    private boolean useManualBlocker = true;

    public CompositeBlocker(ManualPluginDetectingBlocker manualBlocker, Blocker... delegates) {
        this.manual = manualBlocker;
        if (manual == null) {
            useManualBlocker = false;
        }
        this.delegates = delegates;
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
        if (useManualBlocker && !manual.shouldBlock(event)) {
            return false; // allowed by manual plugin detecting blockeru
        }
        for (Blocker delegate : delegates) {
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

    public Blocker[] getDelegates() {
        return Arrays.copyOf(delegates, delegates.length);
    }

    public void stopUsingManualBlocker() {
        useManualBlocker = false;
    }

}
