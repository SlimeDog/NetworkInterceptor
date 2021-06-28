package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.plugin.PluginOptions;

import java.util.List;

public class WhitelistBlocker extends PluginAwareBlocker {
    private final List<String> whitelist;

    public WhitelistBlocker(List<String> whitelist, PluginOptions options) {
        super(options);
        this.whitelist = whitelist;
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
        boolean original = !this.whitelist.contains(event.getHost().toLowerCase());
        return shouldBeBlockedInternal(event, original);
    }
}
