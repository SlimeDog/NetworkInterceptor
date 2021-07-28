package me.lucko.networkinterceptor.velocity;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

public class VelictyLoggerWrapper extends Logger {
    private final org.slf4j.Logger delegate;

    protected VelictyLoggerWrapper(VelocityNetworkInterceptor plugin, org.slf4j.Logger delegate) {
        super(plugin.getClass().getCanonicalName(), null);
        this.delegate = delegate;
    }

    @Override
    public void log(@NotNull LogRecord logRecord) {
        if (logRecord.getLevel() == Level.INFO) {
            delegate.info(logRecord.getMessage(), logRecord.getThrown());
        } else if (logRecord.getLevel() == Level.WARNING) {
            delegate.warn(logRecord.getMessage(), logRecord.getThrown());
        } else if (logRecord.getLevel() == Level.SEVERE) {
            delegate.error(logRecord.getMessage(), logRecord.getThrown());
        } else {
            delegate.info(logRecord.getLevel() + " " + logRecord.getMessage(), logRecord.getThrown());
        }
    }

}
