package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.InterceptEvent;

public interface EventLogger {

    void logAttempt(InterceptEvent event);

    void logBlock(InterceptEvent event);

}
