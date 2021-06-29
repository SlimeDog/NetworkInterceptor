package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;

public class CompositeBlocker implements Blocker {
    private final Blocker[] delegates;

    public CompositeBlocker(Blocker... delegates) {
        this.delegates = delegates;
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
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

}
