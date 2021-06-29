package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;

import java.util.List;

public class WhitelistBlocker implements Blocker {
    private final List<String> whitelist;

    public WhitelistBlocker(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
        return !this.whitelist.contains(event.getHost().toLowerCase());
    }
}
