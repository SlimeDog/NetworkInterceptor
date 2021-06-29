package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;

import java.util.List;

public class AllowBlocker implements Blocker {
    private final List<String> allowed;

    public AllowBlocker(List<String> allowed) {
        this.allowed = allowed;
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
        return !this.allowed.contains(event.getHost().toLowerCase());
    }
}
