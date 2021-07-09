package me.lucko.networkinterceptor.loggers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import me.lucko.networkinterceptor.InterceptEvent;

import java.util.List;

public class CompositeLogger<PLUGIN> implements EventLogger<PLUGIN> {
    private final List<EventLogger<PLUGIN>> loggers;

    @SafeVarargs // hopefully
    public CompositeLogger(EventLogger<PLUGIN>... loggers) {
        Preconditions.checkArgument(loggers.length != 0, "no loggers specified");
        this.loggers = ImmutableList.copyOf(loggers);
    }

    @Override
    public void logAttempt(InterceptEvent<PLUGIN> event) {
        for (EventLogger<PLUGIN> logger : this.loggers) {
            try {
                logger.logAttempt(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void logBlock(InterceptEvent<PLUGIN> event) {
        for (EventLogger<PLUGIN> logger : this.loggers) {
            try {
                logger.logBlock(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
