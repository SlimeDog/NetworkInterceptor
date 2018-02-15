package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;

import java.util.List;

public class BlacklistBlocker implements Blocker {
    private final List<String> blacklist;

    public BlacklistBlocker(List<String> blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
        return this.blacklist.contains(event.getHost().toLowerCase());
    }
}
