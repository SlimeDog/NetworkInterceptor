package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.InterceptEvent;

public interface EventLogger<PLUGIN> {

    void logAttempt(InterceptEvent<PLUGIN> event);

    void logBlock(InterceptEvent<PLUGIN> event);

}
