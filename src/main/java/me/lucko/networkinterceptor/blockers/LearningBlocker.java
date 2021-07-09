package me.lucko.networkinterceptor.blockers;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.lucko.networkinterceptor.InterceptEvent;

public class LearningBlocker<PLUGIN> implements Blocker<PLUGIN> {
    private final Blocker<PLUGIN> delegate;
    private final Cache<StackTraces, StackTraces> cachedAllowedTraces;
    private final Cache<StackTraces, StackTraces> cachedBlockedTraces;
    private final long similarStackTimeoutMs;

    public LearningBlocker(Blocker<PLUGIN> delegate, long similarStackTimeoutMs) {
        this.delegate = delegate;
        this.similarStackTimeoutMs = similarStackTimeoutMs;
        cachedAllowedTraces = CacheBuilder.newBuilder().expireAfterWrite(similarStackTimeoutMs, TimeUnit.MILLISECONDS)
                .build();
        cachedBlockedTraces = CacheBuilder.newBuilder().expireAfterWrite(similarStackTimeoutMs, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public boolean shouldBlock(InterceptEvent<PLUGIN> event) {
        boolean rawBlock = delegate.shouldBlock(event);
        StackTraces traces = new StackTraces(event.getNonInternalStackTraceWithPlugins(), event.getHost());
        StackTraces prev = cachedAllowedTraces.getIfPresent(traces);
        if (prev != null) {
            // similar trace has been allowed in the past
            event.setOriginalHost(prev.originalHost);
        }
        if (!rawBlock) { // allowed by default -> allow
            if (prev == null) {
                cachedAllowedTraces.put(traces, traces);
            } else { // update expiry
                cachedAllowedTraces.put(prev, prev);
            }
            return false; // allow
        }
        // not allowed by default
        if (prev != null) { // has original allowed host
            return false; // allow
        }
        prev = cachedBlockedTraces.getIfPresent(traces); // see if there's a FQDN that has previously been blocked
        if (prev != null) {
            event.setOriginalHost(prev.originalHost); // blocked original
        } else {
            cachedBlockedTraces.put(traces, traces);
        }
        return true; // block
    }

    public void clear() {
        cachedAllowedTraces.invalidateAll();
        cachedBlockedTraces.invalidateAll();
    }

    public Blocker<PLUGIN> getDelegate() {
        return delegate;
    }

    public long getTimeoutMs() {
        return similarStackTimeoutMs;
    }

    private class StackTraces {
        private final Map<StackTraceElement, PLUGIN> payload;
        private final String originalHost;

        private StackTraces(Map<StackTraceElement, PLUGIN> payload, String originalHost) {
            this.payload = payload;
            this.originalHost = originalHost;
        }

        @Override
        public int hashCode() {
            return Objects.hash(payload); // original host not included
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof LearningBlocker.StackTraces)) {
                return false;
            }
            @SuppressWarnings("unchecked")
            StackTraces o = (StackTraces) other;
            return payload.equals(o.payload); // original host not included
        }

    }

}
