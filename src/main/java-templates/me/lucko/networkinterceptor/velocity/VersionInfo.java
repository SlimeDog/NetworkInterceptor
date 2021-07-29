package me.lucko.networkinterceptor.velocity;

public final class VersionInfo {
    public static final String VERSION = "${project.version}";

    private VersionInfo() {
        throw new IllegalStateException("Should not be initialized");
    }
    
}
