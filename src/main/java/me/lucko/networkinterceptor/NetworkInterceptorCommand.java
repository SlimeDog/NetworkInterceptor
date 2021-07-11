package me.lucko.networkinterceptor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import me.lucko.networkinterceptor.blockers.AllowBlocker;
import me.lucko.networkinterceptor.blockers.Blocker;
import me.lucko.networkinterceptor.blockers.CompositeBlocker;
import me.lucko.networkinterceptor.blockers.LearningBlocker;
import me.lucko.networkinterceptor.blockers.PluginAwareBlocker;
import me.lucko.networkinterceptor.common.CommonCommandSender;
import me.lucko.networkinterceptor.common.NetworkInterceptorPlugin;
import me.lucko.networkinterceptor.common.CommonNetworkInterceptor.InterceptMethod;
import me.lucko.networkinterceptor.interceptors.Interceptor;
import me.lucko.networkinterceptor.loggers.CompositeLogger;
import me.lucko.networkinterceptor.loggers.ConsoleLogger;
import me.lucko.networkinterceptor.loggers.EventLogger;
import me.lucko.networkinterceptor.loggers.FileLogger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NetworkInterceptorCommand<PLUGIN> {
    private static final List<String> OPTIONS = Arrays.asList("reload", "info");

    private final NetworkInterceptorPlugin<PLUGIN> plugin;

    public NetworkInterceptorCommand(NetworkInterceptorPlugin<PLUGIN> plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommonCommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.send(ChatColor.RED + "Running NetworkInterceptor v" + this.plugin.getPluginVersion());
            sender.send(ChatColor.GRAY + "Use '/networkinterceptor reload' to reload the configuration.");

            return true;
        }

        if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("networkinterceptor.command.reload")) {
            this.plugin.reload();

            sender.send(ChatColor.GOLD + "NetworkInterceptor configuration reloaded.");

            return true;
        } else if (args[0].equalsIgnoreCase("info") && sender.hasPermission("networkinterceptor.command.info")) {
            sendInfoMessage(sender);
            return true;
        }

        sender.send(ChatColor.RED + "Unknown subcommand.");

        return true;
    }

    private void sendInfoMessage(CommonCommandSender sender) {
        boolean useMetrics = plugin.getConfiguration().getBoolean("enable-metrics", true);
        sender.send(useMetrics ? "bStats metrics enabled" : "bStats metrics disabled");
        sender.send(getBlockerMessage().split("\n"));
        sender.send(getLoggerMessage());
        sender.send(getInterceptorsMessage());
    }

    public String getInterceptorsMessage() {
        List<String> methods = plugin.getConfiguration().getStringList("methods");
        if (methods.isEmpty()) {
            return "No methods are defined";
        }

        for (String method : new ArrayList<>(methods)) {
            try {
                InterceptMethod.fromString(method);
            } catch (IllegalArgumentException e) {
                methods.remove(method);
            }
        }
        Map<InterceptMethod, Interceptor> interceptors = plugin.getDelegate().getInterceptors();
        if (methods.size() != interceptors.size()) {
            return "Real interceptors: " + interceptors + "\nConfig defined: " + methods;
        }
        return "Interceptors: " + methods;
    }

    private String getLoggerMessage() {
        if (!plugin.getConfiguration().getBoolean("logging.enabled", true)) {
            return "Logging is not enabled";
        }
        EventLogger<PLUGIN> logger = plugin.getDelegate().getEventLogger();
        if (logger == null) {
            String mode = plugin.getConfiguration().getString("logging.mode", "console");
            return "Unknown logging mode: " + mode;
        }
        if (logger instanceof CompositeLogger) {
            return "Using console+file combined logger";
        } else if (logger instanceof ConsoleLogger) {
            return "Using console logger";
        } else if (logger instanceof FileLogger) {
            return "Using file logger";
        } else {
            return "Unknown logger: " + logger;
        }
    }

    private String getBlockerMessage() {
        Blocker<PLUGIN> blocker = plugin.getDelegate().getBlocker();
        String blockerMessage;
        if (blocker == null) {
            String mode = plugin.getConfiguration().getString("mode", "deny");
            if (!mode.equalsIgnoreCase("allow") && !mode.equalsIgnoreCase("deny")) {
                blockerMessage = "Unknown mode: " + mode;
            } else {
                blockerMessage = "Blocking is not enabled";
            }
        } else if (blocker instanceof LearningBlocker) {
            Blocker<PLUGIN> delegate = ((LearningBlocker<PLUGIN>) blocker).getDelegate();
            blockerMessage = getCompositeBlockerMessage(delegate) + "\nUsing a mapping blocker with timer of "
                    + ((LearningBlocker<PLUGIN>) blocker).getTimeoutMs() + "ms";
        } else {
            blockerMessage = getCompositeBlockerMessage(blocker);
        }
        return blockerMessage;
    }

    private String getCompositeBlockerMessage(Blocker<PLUGIN> blocker) {
        String blockerMessage;
        if (!(blocker instanceof CompositeBlocker)) {
            blockerMessage = "Unknown type of delegate: " + blocker;
        } else {
            CompositeBlocker<PLUGIN> compositeBlocker = (CompositeBlocker<PLUGIN>) blocker;
            Blocker<PLUGIN>[] delegates = compositeBlocker.getDelegates();
            if (delegates.length != 2) {
                blockerMessage = "Unknown delegates: " + Arrays.asList(delegates);
            } else {
                Blocker<PLUGIN> mainBlocker = delegates[0];
                Blocker<PLUGIN> pluginBlocker = delegates[1];
                if (!(pluginBlocker instanceof PluginAwareBlocker)) {
                    blockerMessage = "Miscondigured delegates: " + Arrays.asList(delegates);
                } else {
                    blockerMessage = getMainBlockerMessage(mainBlocker);
                }
            }
        }
        return blockerMessage;
    }

    private String getMainBlockerMessage(Blocker<PLUGIN> mainBlocker) {
        if (mainBlocker instanceof AllowBlocker) {
            return "Using blocking strategy allow";
        } else {
            return "Using blocking strategy deny";
        }
    }

    @SuppressWarnings("unchecked")
    public SpigotWrapper asSpigotCommand() {
        return new SpigotWrapper((NetworkInterceptorCommand<JavaPlugin>) this);
    }

    @SuppressWarnings("unchecked")
    public BungeeWrapper asBungeeCommand() {
        return new BungeeWrapper((NetworkInterceptorCommand<Plugin>) this);
    }

    public static class SpigotWrapper implements TabExecutor {
        private final NetworkInterceptorCommand<JavaPlugin> cmd;

        public SpigotWrapper(NetworkInterceptorCommand<JavaPlugin> md) {
            this.cmd = md;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], OPTIONS, new ArrayList<>());
            }

            return Collections.emptyList();
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            return cmd.onCommand(new CommonCommandSender.Spigot(sender), args);
        }
    }

    public static class BungeeWrapper extends net.md_5.bungee.api.plugin.Command
            implements net.md_5.bungee.api.plugin.TabExecutor {
        private static final String NAME = "networkinterceptorbungee";
        private static final String PERMISSION = "networkinterceptor.command";
        private static final String ALIASES = "nib";
        private static final List<String> OPTIONS = Collections.unmodifiableList(Arrays.asList("reload", "info"));
        private final NetworkInterceptorCommand<Plugin> cmd;

        public BungeeWrapper(NetworkInterceptorCommand<Plugin> cmd) {
            super(NAME, PERMISSION, ALIASES);
            this.cmd = cmd;
        }

        @Override
        public Iterable<String> onTabComplete(net.md_5.bungee.api.CommandSender sender, String[] args) {
            if (args.length == 1) {
                return OPTIONS;
            }
            return Collections.emptyList();
        }

        @Override
        public void execute(net.md_5.bungee.api.CommandSender sender, String[] args) {
            cmd.onCommand(new CommonCommandSender.Bungee(sender), args);
        }

    }
}
