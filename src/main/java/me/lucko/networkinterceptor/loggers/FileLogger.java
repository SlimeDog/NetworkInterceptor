package me.lucko.networkinterceptor.loggers;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;
import me.lucko.networkinterceptor.velocity.VelocityNetworkInterceptor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class FileLogger<PLUGIN> extends AbstractEventLogger<PLUGIN> {
    private final Logger logger;
    private final File myFile;
    private final Formatter formatter;
    private final VelocityWrapper velocityWrapper;

    public FileLogger(NetworkInterceptorPlugin<PLUGIN> plugin, boolean truncateFile) {
        super(true, plugin.isBungee(), plugin.isVelocity());
        File file = new File(plugin.getDataFolder(), "intercept.log");
        this.myFile = file;
        System.out.println("Initializing file logger that logs to the file " + file);
        formatter = new Formatter() {
            @Override
            public String format(LogRecord record) {
                return new Date(record.getMillis()).toString() + ": " + record.getMessage() + "\n";
            }
        };
        velocityWrapper = isVelocity ? new VelocityWrapper() : null;
        if (isVelocity) {
            ((VelocityNetworkInterceptor) plugin).runRepeatingTask(velocityWrapper::write, 20L);
            logger = velocityWrapper;
            return;
        }
        this.logger = Logger.getLogger(FileLogger.class.getName());
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
        System.out.println("File logger uses logger: " + this.logger);
        System.out.println("Logger level: " + this.logger.getLevel());
        Logger parent = this.logger.getParent();
        while (parent != null) {
            parent.setLevel(Level.ALL);
            parent.setFilter(record -> true);
            System.out.println("Parent logger for file logger: " + parent);
            System.out.println("Parent logger level: " + parent.getLevel());
            attemptJulLoggerLevel(parent);
            parent = parent.getParent();
        }
        // TODO - remove
        this.logger.warning("(WARN) Current Server version: " + plugin.getServerVersion());
        this.logger.warning("(WARN) Current NetworkInterceptor version: " + plugin.getPluginVersion());
        this.logger.severe("(ERROR) Current Server version: " + plugin.getServerVersion());
        this.logger.severe("(ERROR) Current NetworkInterceptor version: " + plugin.getPluginVersion());
        // TODO - remove
    }

    private void attemptJulLoggerLevel(Logger logger) {
        if (!isVelocity) {
            return; // below only needed in Velocity
        }
        try {
            Class<?> coreLoggerClass = Class.forName("org.apache.logging.log4j.jul.CoreLogger");
            Class<?> log4JLoggerClass = Class.forName("org.apache.logging.log4j.core.Logger");
            Class<?> lo4jLevelClass = Class.forName("org.apache.logging.log4j.Level");
            if (logger.getClass().isAssignableFrom(coreLoggerClass)) {
                System.out.println("CoreLogger (attempting parent fix)");
                Field loggerField = coreLoggerClass.getDeclaredField("logger");
                loggerField.setAccessible(true);
                Object parent = loggerField.get(logger);
                System.out.println("REAL parent: " + parent + " of class " + parent.getClass());
                if (log4JLoggerClass.isInstance(parent)) {
                    System.out.println("log4j Logger (attempting parent fix #2) - setting all");
                    Field allField = lo4jLevelClass.getField("ALL");
                    Object allLevel = allField.get(null);
                    Method setLevelMethod = log4JLoggerClass.getMethod("setLevel", lo4jLevelClass);
                    setLevelMethod.invoke(parent, allLevel);
                    dealWithLog4JLogger(parent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dealWithLog4JLogger(Object logger) throws Exception {
        Class<?> asyncLoggerClass = Class.forName("org.apache.logging.log4j.core.async.AsyncLogger");
        Field theadLocalTranslatorTypeField = asyncLoggerClass.getDeclaredField("threadLocalTranslatorType");
        Field varargTranslatorTypeField = asyncLoggerClass.getDeclaredField("varargTranslatorType");
        theadLocalTranslatorTypeField.setAccessible(true);
        varargTranslatorTypeField.setAccessible(true);
        Object threadLocalTranslatorType = theadLocalTranslatorTypeField.get(logger);
        Object varargTranslatorType = varargTranslatorTypeField.get(logger);
        Method getTranslatorTypeMethod = asyncLoggerClass.getDeclaredMethod("getTranslatorType");
        getTranslatorTypeMethod.setAccessible(true);
        Object translatorType = getTranslatorTypeMethod.invoke(logger);
        if (translatorType == threadLocalTranslatorType) {
            System.out.println("Using threadLocalTranslatorType");
        } else if (translatorType == varargTranslatorType) {
            System.out.println("Using varargTranslatorType");
        } else {
            System.out.println("Unkonwn translator type: " + translatorType + " -> expected " + threadLocalTranslatorType + " or " + varargTranslatorType);
        }
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public void logAttempt(InterceptEvent<PLUGIN> event) {
        Logger parent = this.logger.getParent();
        System.out.println("Logging (attempt) to FILE: " + event + " with logger " + this.logger + " @ " + this.logger.getLevel() + " and parent " + parent + " @ " + parent.getLevel());
        super.logAttempt(event);
        System.out.println("Logged  (attempt) to FILE: " + event + " with logger " + this.logger + " @ " + this.logger.getLevel() + " and parent @ " + parent.getLevel());
    }

    @Override
    public void logBlock(InterceptEvent<PLUGIN> event) {
        Logger parent = this.logger.getParent();
        System.out.println("Logging (blocked) to FILE: " + event + " with logger " + this.logger + " @ " + this.logger.getLevel() + " and parent " + parent + " @ " + parent.getLevel());
        super.logBlock(event);
        System.out.println("Logged  (blocked) to FILE: " + event + " with logger " + this.logger + " @ " + this.logger.getLevel() + " and parent @ " + parent.getLevel());
    }
    
    private final class VelocityWrapper extends Logger {
        private final Queue<String> queue = new LinkedBlockingQueue<>();

        protected VelocityWrapper() {
            super("VelocityWrapper", null);
            
        }

        @Override
        public void log(LogRecord record) {
            queue.add(formatter.format(record));
        }

        private synchronized void write() {
            try (BufferedWriter output = new BufferedWriter(new FileWriter(myFile, true))) {
                while (!queue.isEmpty()) {
                    output.write(queue.poll());
                    output.newLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        

    }
}
