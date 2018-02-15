package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.NetworkInterceptor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class FileLogger extends AbstractEventLogger {
    private final Logger logger;

    public FileLogger(NetworkInterceptor plugin) {
        super(true);
        File file = new File(plugin.getDataFolder(), "intercept.log");
        this.logger = Logger.getLogger(FileLogger.class.getName());
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();

            FileHandler fileHandler = new FileHandler(file.getAbsolutePath(), 0, 1, true);
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return new Date(record.getMillis()).toString() + ": " + record.getMessage() + "\n";
                }
            });
            this.logger.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.logger.setUseParentHandlers(false);
        this.logger.setLevel(Level.ALL);
        this.logger.setFilter(record -> true);
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }
}
