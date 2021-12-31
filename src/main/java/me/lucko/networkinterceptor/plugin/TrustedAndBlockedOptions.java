package me.lucko.networkinterceptor.plugin;

public class TrustedAndBlockedOptions<PLUGIN> {
    private final PluginOptions<PLUGIN> trusted, blocked;

    public TrustedAndBlockedOptions(PluginOptions<PLUGIN> trusted, PluginOptions<PLUGIN> blocked) {
        Validate.isTrue(trusted.getKeepType() == blocked.getKeepType(), "Need keep types to be the same");
        Validate.isTrue(trusted.shouldAllowNonPlugin() == blocked.shouldAllowNonPlugin(),
                "Need non-plugin behaviour to be the same");
        Validate.isTrue(trusted.trust, "First argument should be trusted, second untrusted");
        Validate.isTrue(!blocked.trust, "First argument should be trusted, second untrusted");
        this.trusted = trusted;
        this.blocked = blocked;
    }

    public boolean isTrusted(PLUGIN plugin) {
        return trusted.isTrusted(plugin) && blocked.isTrusted(plugin);
    }

    public PluginOptions<PLUGIN> getTrustedOptions() {
        return trusted;
    }

    public PluginOptions<PLUGIN> getBlockedOptions() {
        return blocked;
    }

    // in case the Validate class is not available
    private static class Validate {
        public static void isTrue(boolean val, String msg) {
            if (!val) {
                throw new IllegalStateException(msg);
            }
        }
    }

}
