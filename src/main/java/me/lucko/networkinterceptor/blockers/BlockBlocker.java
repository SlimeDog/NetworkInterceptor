package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;

import java.util.List;

public class BlockBlocker<PLUGIN> implements Blocker<PLUGIN> {
    private final List<String> blocked;

    public BlockBlocker(List<String> blocked) {
        this.blocked = blocked;
    }

    @Override
    public boolean shouldBlock(InterceptEvent<PLUGIN> event) {
        return this.blocked.contains(event.getHost().toLowerCase());
    }
}
