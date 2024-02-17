package me.lucko.networkinterceptor.interceptors;

import java.net.SocketTimeoutException;

public class ConnectionBlockedException extends SocketTimeoutException {

    public ConnectionBlockedException(String interceptorType) {
        super("Connection blocked by NetworkInterceptor [" + interceptorType + "]");
    }
    
}
