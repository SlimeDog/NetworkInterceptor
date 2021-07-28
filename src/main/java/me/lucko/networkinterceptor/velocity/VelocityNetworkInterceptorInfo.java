package me.lucko.networkinterceptor.velocity;

public final class VelocityNetworkInterceptorInfo {
    static final String VERSION = "3.1.1";
    static final String NAME = "NetworkInterceptor";
    static final String ID = "networkinterceptor";
    static final String DESCRIPTION = "Plugin to monitor and block outgoing network requests";

    private VelocityNetworkInterceptorInfo() {
        throw new IllegalStateException("Class should not be initialized");
    }
    
}
