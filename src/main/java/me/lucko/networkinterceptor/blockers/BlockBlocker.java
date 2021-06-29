package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.plugin.PluginOptions;

import java.util.List;

public class BlockBlocker extends PluginAwareBlocker {
    private final List<String> blocked;

    public BlockBlocker(List<String> blocked, PluginOptions options) {
        super(options);
        this.blocked = blocked;
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
        boolean original = this.blocked.contains(event.getHost().toLowerCase());
        return shouldBeBlockedInternal(event, original);
    }
}
