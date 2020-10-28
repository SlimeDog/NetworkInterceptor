package me.lucko.networkinterceptor;

import com.google.common.collect.ImmutableList;

import me.lucko.networkinterceptor.blockers.BlacklistBlocker;
import me.lucko.networkinterceptor.blockers.Blocker;
import me.lucko.networkinterceptor.blockers.WhitelistBlocker;
import me.lucko.networkinterceptor.interceptors.Interceptor;
import me.lucko.networkinterceptor.interceptors.ProxySelectorInterceptor;
import me.lucko.networkinterceptor.interceptors.SecurityManagerInterceptor;
import me.lucko.networkinterceptor.loggers.CompositeLogger;
import me.lucko.networkinterceptor.loggers.ConsoleLogger;
import me.lucko.networkinterceptor.loggers.EventLogger;
import me.lucko.networkinterceptor.loggers.FileLogger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class NetworkInterceptor extends JavaPlugin {
    private final Map<InterceptMethod, Interceptor> interceptors = new EnumMap<>(InterceptMethod.class);
    private EventLogger logger = null;
    private Blocker blocker = null;

    private boolean ignoreWhitelisted = false;

    public NetworkInterceptor() {
        // init early
        // this is seen as bad practice, but we want to try and catch as
        // many requests as possible

        saveDefaultConfig();

        enable();
    }

    @Override
    public void onEnable() {
        getCommand("networkinterceptor").setExecutor(new NetworkInterceptorCommand(this));
    }

    @Override
    public void onDisable() {
        disable();
    }

    public void reload() {
        reloadConfig();

        disable();

        enable();
    }

    public void logAttempt(InterceptEvent event) {
        if (this.logger == null) {
            return;
        }

        if (this.ignoreWhitelisted && this.blocker instanceof WhitelistBlocker && !this.blocker.shouldBlock(event)) {
            return;
        }

        this.logger.logAttempt(event);
    }

    public void logBlock(InterceptEvent event) {
        if (this.logger == null) {
            return;
        }
        this.logger.logBlock(event);
    }

    public boolean shouldBlock(InterceptEvent event) {
        return this.blocker != null && this.blocker.shouldBlock(event);
    }

    private void enable() {
        FileConfiguration config = getConfig();

        setupBlockers(config);
        setupLoggers(config);
        setupInterceptors(config);

        for (Interceptor interceptor : this.interceptors.values()) {
            try {
                interceptor.enable();
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Exception occurred whilst enabling " + interceptor.getClass().getName(), e);
            }
        }
    }

    private void disable() {
        for (Interceptor interceptor : this.interceptors.values()) {
            try {
                interceptor.disable();
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Exception occurred whilst disabling " + interceptor.getClass().getName(), e);
            }
        }
    }

    private void setupInterceptors(FileConfiguration configuration) {
        List<String> methods = configuration.getStringList("methods");
        if (methods.isEmpty()) {
            getLogger().info("No methods are defined");
            return;
        }

        Set<InterceptMethod> enabled = EnumSet.noneOf(InterceptMethod.class);
        for (String method : methods) {
            try {
                enabled.add(InterceptMethod.fromString(method));
            } catch (IllegalArgumentException e) {
                getLogger().severe("Unknown method: " + method);
            }
        }

        getLogger().info("Interceptors: " + enabled);

        for (InterceptMethod method : enabled) {
            try {
                Constructor<? extends Interceptor> constructor = method.clazz.getDeclaredConstructor(NetworkInterceptor.class);
                Interceptor interceptor = constructor.newInstance(this);
                this.interceptors.put(method, interceptor);
            } catch (Throwable t) {
                getLogger().log(Level.SEVERE, "Exception occurred whilst initialising method " + method, t);
            }
        }
    }

    private void setupLoggers(FileConfiguration configuration) {
        if (!configuration.getBoolean("logging.enabled", true)) {
            getLogger().info("Logging is not enabled");
            return;
        }

        this.ignoreWhitelisted = configuration.getBoolean("logging.ignore-whitelisted", false);

        String mode = configuration.getString("logging.mode", "console");
        boolean includeTraces = configuration.getBoolean("logging.include-traces", true);
        switch (mode.toLowerCase()) {
            case "all":
                getLogger().info("Using console+file combined logger");
                this.logger = new CompositeLogger(new ConsoleLogger(this, includeTraces), new FileLogger(this));
                break;
            case "console":
                getLogger().info("Using console logger");
                this.logger = new ConsoleLogger(this, includeTraces);
                break;
            case "file":
                getLogger().info("Using file logger");
                this.logger = new FileLogger(this);
                break;
            default:
                getLogger().severe("Unknown logging mode: " + mode);
        }
    }

    private void setupBlockers(FileConfiguration configuration) {
        if (!configuration.getBoolean("block.enabled", false)) {
            getLogger().info("Blocking is not enabled");
            return;
        }

        List<String> list = ImmutableList.copyOf(configuration.getStringList("block.list"));

        String mode = configuration.getString("block.mode", "blacklist");
        switch (mode.toLowerCase()) {
            case "whitelist":
                getLogger().info("Using whitelist blocking strategy");
                this.blocker = new WhitelistBlocker(list);
                break;
            case "blacklist":
                getLogger().info("Using blacklist blocking strategy");
                this.blocker = new BlacklistBlocker(list);
                break;
            default:
                getLogger().severe("Unknown blocking mode: " + mode);
        }
    }

    enum InterceptMethod {
        SECURITY_MANAGER("security-manager", SecurityManagerInterceptor.class),
        PROXY_SELECTOR("proxy-selector", ProxySelectorInterceptor.class);

        private final String name;
        private final Class<? extends Interceptor> clazz;

        InterceptMethod(String name, Class<? extends Interceptor> clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        static InterceptMethod fromString(String string) {
            for (InterceptMethod method : values()) {
                if (method.name.equalsIgnoreCase(string)) {
                    return method;
                }
            }
            throw new IllegalArgumentException();
        }
    }

}
