package me.lucko.networkinterceptor.interceptors;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.NetworkInterceptor;
import me.lucko.networkinterceptor.utils.SneakyThrow;

import java.net.SocketTimeoutException;
import java.security.Permission;

public class SecurityManagerInterceptor extends SecurityManager implements Interceptor {
    private final NetworkInterceptor plugin;

    private boolean enabled = true;

    public SecurityManagerInterceptor(NetworkInterceptor plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        System.setSecurityManager(this);
    }

    @Override
    public void disable() {
        enabled = false;

        System.setSecurityManager(null);
    }

    @Override
    public void checkConnect(String host, int port) {
        StackTraceElement[] trace = new Exception().getStackTrace();
        InterceptEvent event = new InterceptEvent(host, trace);

        this.plugin.logAttempt(event);

        if (this.plugin.shouldBlock(event)) {
            this.plugin.logBlock(event);
            SneakyThrow.sneakyThrow(new SocketTimeoutException("Connection timed out"));
            throw new AssertionError();
        }
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        checkConnect(host, port);
    }

    @Override
    public void checkPermission(Permission perm) {
        String name = perm.getName();
        if (name == null) {
            return;
        }

        if (this.enabled && name.equals("setSecurityManager")) {
            throw new SecurityException("Cannot replace the security manager.");
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        checkPermission(perm);
    }
}
