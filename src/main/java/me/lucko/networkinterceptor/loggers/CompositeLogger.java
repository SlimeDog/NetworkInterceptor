package me.lucko.networkinterceptor.loggers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import me.lucko.networkinterceptor.InterceptEvent;

import java.util.List;

public class CompositeLogger implements EventLogger {
    private final List<EventLogger> loggers;

    public CompositeLogger(EventLogger... loggers) {
        Preconditions.checkArgument(loggers.length != 0, "no loggers specified");
        this.loggers = ImmutableList.copyOf(loggers);
    }

    @Override
    public void logAttempt(InterceptEvent event) {
        for (EventLogger logger : this.loggers) {
            try {
                logger.logAttempt(event);
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

    @Override
    public void logBlock(InterceptEvent event) {
        for (EventLogger logger : this.loggers) {
            try {
                logger.logBlock(event);
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }
}
