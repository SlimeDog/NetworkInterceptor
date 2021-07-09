package me.lucko.networkinterceptor.blockers;

import me.lucko.networkinterceptor.InterceptEvent;

public interface Blocker<PLUGIN> {

    boolean shouldBlock(InterceptEvent<PLUGIN> event);

}
