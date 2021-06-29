package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.plugin.PluginOptions;

public class ProcessAwareBlocker implements Blocker {
    private final PluginOptions options;

    public ProcessAwareBlocker(PluginOptions options) {
        this.options = options;
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
        return !options.isFromTrustedProcess(event.getStackTrace(), event.getHost());
    }

}
