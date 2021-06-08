package me.lucko.networkinterceptor.blockers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.plugin.java.JavaPlugin;

import me.lucko.networkinterceptor.InterceptEvent;

public class LearningBlocker implements Blocker {
    private static final long STACK_CHECK_TIMEOUT_MS = 10L; // TODO - configurable
    private static final long STACK_TIMEOUT_CLEAR_DELAY = 70 * 1000L; // TODO - configurable
    private final JavaPlugin plugin;
    private final Blocker delegate;
    private final Map<StackTraces, StackTraces> cachedAllowedTraces = new HashMap<>();

    public LearningBlocker(JavaPlugin plugin, Blocker delegate) {
        this.plugin = plugin;
        this.delegate = delegate;
    }

    public void scheduleCleanup() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::cleanOldTraces,
                STACK_TIMEOUT_CLEAR_DELAY, STACK_TIMEOUT_CLEAR_DELAY);
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
        boolean rawBlock = delegate.shouldBlock(event);
        // unmodifiable map
        StackTraces traces = new StackTraces(event.getNonInternalStackTraceWithPlugins(), event.getHost());
        if (!rawBlock) { // allowed by default -> allow
            cachedAllowedTraces.put(traces, traces);
            return rawBlock; // allow
        }
        // not allowed by default
        StackTraces prev = cachedAllowedTraces.get(traces);
        if (prev != null) { // TODO - configurable ?
            // similar trace has been allowed in the past
            plugin.getLogger().info("Allowing because of similar trace! ['" + event.getHost()
                    + "' was found to correspond to '" + prev.originalHost + "']");
            return false; // allow
        }
        return rawBlock; // block
    }

    private void cleanOldTraces() {
        long oldestStamp = System.currentTimeMillis() - STACK_CHECK_TIMEOUT_MS;
        cachedAllowedTraces.values().removeIf(val -> val.stamp < oldestStamp);
    }

    private class StackTraces {
        private final Map<StackTraceElement, JavaPlugin> payload;
        private final String originalHost;
        private final long stamp = System.currentTimeMillis();

        private StackTraces(Map<StackTraceElement, JavaPlugin> payload, String originalHost) {
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
            if (!(other instanceof StackTraces)) {
                return false;
            }
            StackTraces o = (StackTraces) other;
            return payload.equals(o.payload); // original host not included
        }

    }

}
