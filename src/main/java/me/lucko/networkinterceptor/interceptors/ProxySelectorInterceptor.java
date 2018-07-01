package me.lucko.networkinterceptor.interceptors;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.NetworkInterceptor;
import me.lucko.networkinterceptor.utils.SneakyThrow;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.List;

public class ProxySelectorInterceptor implements Interceptor {
    // private static final List<Proxy> DUMMY_PROXY = new DummyProxyList();

    private final NetworkInterceptor plugin;

    public ProxySelectorInterceptor(NetworkInterceptor plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        ProxySelector selector = ProxySelector.getDefault();
        if (selector instanceof LoggingSelector) {
            return;
        }

        this.plugin.getLogger().info("[ProxySelectorInterceptor] Replacing '" + selector.getClass().getName() + "' selector with logged variant.");
        ProxySelector.setDefault(new LoggingSelector(selector));
    }

    @Override
    public void disable() {
        ProxySelector selector = ProxySelector.getDefault();
        if (selector instanceof LoggingSelector) {
            ProxySelector.setDefault(((LoggingSelector) selector).delegate);
        }
    }

    private final class LoggingSelector extends ProxySelector {
        private final ProxySelector delegate;

        private LoggingSelector(ProxySelector delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<Proxy> select(URI uri) {
            String host = uri.getHost();
            StackTraceElement[] trace = new Exception().getStackTrace();
            InterceptEvent event = new InterceptEvent(host, trace);

            ProxySelectorInterceptor.this.plugin.logAttempt(event);

            if (ProxySelectorInterceptor.this.plugin.shouldBlock(event)) {
                ProxySelectorInterceptor.this.plugin.logBlock(event);
                SneakyThrow.sneakyThrow(new SocketTimeoutException("Connection timed out"));
                throw new AssertionError();
            }

            return this.delegate.select(uri);
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            this.delegate.connectFailed(uri, sa, ioe);
        }
    }

    /* private static final class DummyProxyList extends AbstractList<Proxy> {
        @Override
        public Proxy get(int index) {
            SneakyThrow.sneakyThrow(new IOException("Connection not allowed"));
            throw new AssertionError();
        }

        @Override
        public int size() {
            return 1;
        }
    } */
}
