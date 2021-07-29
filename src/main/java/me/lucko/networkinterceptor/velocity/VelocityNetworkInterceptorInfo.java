package me.lucko.networkinterceptor.velocity;

public final class VelocityNetworkInterceptorInfo {
    static final String VERSION = VersionInfo.VERSION; // the VersionInfo.VERSION string is automatically filled in by a
                                                       // maven plugin. This way I do not need to manually set the
                                                       // version for Velocity.
    static final String NAME = "NetworkInterceptor";
    static final String ID = "networkinterceptor";
    static final String DESCRIPTION = "Plugin to monitor and block outgoing network requests";

    private VelocityNetworkInterceptorInfo() {
        throw new IllegalStateException("Class should not be initialized");
    }

}
