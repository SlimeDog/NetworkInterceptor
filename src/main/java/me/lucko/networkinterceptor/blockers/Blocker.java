package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;

public interface Blocker {

    boolean shouldBlock(InterceptEvent event);

}
