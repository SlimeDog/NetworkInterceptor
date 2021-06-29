package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.plugin.PluginOptions;

import java.util.List;

public class AllowBlocker extends PluginAwareBlocker {
    private final List<String> allowed;

    public AllowBlocker(List<String> allowed, PluginOptions options) {
        super(options);
        this.allowed = allowed;
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
        boolean original = !this.allowed.contains(event.getHost().toLowerCase());
        return shouldBeBlockedInternal(event, original);
    }
}
