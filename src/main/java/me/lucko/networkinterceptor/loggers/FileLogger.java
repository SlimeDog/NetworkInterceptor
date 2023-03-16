package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;
import me.lucko.networkinterceptor.common.Platform;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class FileLogger<PLUGIN> extends AbstractEventLogger<PLUGIN> {
    private final Logger logger;

    public FileLogger(NetworkInterceptorPlugin<PLUGIN> plugin, boolean truncateFile) {
        super(true, plugin);
        File file = new File(plugin.getDataFolder(), "intercept.log");
        Formatter formatter = new Formatter() {
            @Override
            public String format(LogRecord record) {
                return new Date(record.getMillis()).toString() + ": " + record.getMessage() + "\n";
            }
        };
        this.logger = plugin.getPlatformType() == Platform.VELOCITY ? new VelocityWrapper()
                : Logger.getLogger(FileLogger.class.getName());
        try {
            file.getParentFile().mkdirs();
            if (truncateFile && file.exists()) {
                plugin.getLogger().info("Truncating old log file");
                file.delete();
            }
            file.createNewFile();

            FileHandler fileHandler = new FileHandler(file.getAbsolutePath(), 0, 1, true);
            fileHandler.setFormatter(formatter);
            this.logger.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.logger.setUseParentHandlers(false);
        this.logger.setLevel(Level.ALL);
        this.logger.setFilter(record -> true);
        this.logger.info("Current Server version: " + plugin.getServerVersion());
        this.logger.info("Current NetworkInterceptor version: " + plugin.getPluginVersion());
        Logger parent = this.logger.getParent();
        while (parent != null) {
            parent.setLevel(Level.ALL);
            parent.setFilter(record -> true);
            parent = parent.getParent();
        }
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public void logAttempt(InterceptEvent<PLUGIN> event) {
        super.logAttempt(event);
    }

    @Override
    public void logBlock(InterceptEvent<PLUGIN> event) {
        super.logBlock(event);
    }

    private final class VelocityWrapper extends Logger {

        protected VelocityWrapper() {
            super("VelocityWrapper", null);

        }

    }
}
