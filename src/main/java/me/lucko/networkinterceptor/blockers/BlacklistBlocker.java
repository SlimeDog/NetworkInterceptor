package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.plugin.PluginOptions;

import java.util.List;

public class BlacklistBlocker extends PluginAwareBlocker {
    private final List<String> blacklist;

    public BlacklistBlocker(List<String> blacklist, PluginOptions options) {
        super(options);
        this.blacklist = blacklist;
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
        boolean original = this.blacklist.contains(event.getHost().toLowerCase());
        if (!shouldBlockProcesses(event)) {
            return false; // allow because of allowed process
        }
        if (!shouldBlockPlugins(event)) {
            return false; // allow because of allowed plugin
        }
        return original; // default
    }
}
