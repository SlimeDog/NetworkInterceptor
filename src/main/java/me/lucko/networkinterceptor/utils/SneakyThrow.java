package me.lucko.networkinterceptor.utils;

public final class SneakyThrow {

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    private SneakyThrow() {}
}
