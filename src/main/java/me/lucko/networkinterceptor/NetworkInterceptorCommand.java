package me.lucko.networkinterceptor;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import me.lucko.networkinterceptor.NetworkInterceptor.InterceptMethod;
import me.lucko.networkinterceptor.blockers.AllowBlocker;
import me.lucko.networkinterceptor.blockers.Blocker;
import me.lucko.networkinterceptor.blockers.CompositeBlocker;
import me.lucko.networkinterceptor.blockers.LearningBlocker;
import me.lucko.networkinterceptor.blockers.PluginAwareBlocker;
import me.lucko.networkinterceptor.interceptors.Interceptor;
import me.lucko.networkinterceptor.loggers.CompositeLogger;
import me.lucko.networkinterceptor.loggers.ConsoleLogger;
import me.lucko.networkinterceptor.loggers.EventLogger;
import me.lucko.networkinterceptor.loggers.FileLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NetworkInterceptorCommand implements TabExecutor {
    private static final List<String> OPTIONS = Arrays.asList("reload", "info");

    private final NetworkInterceptor plugin;

    public NetworkInterceptorCommand(NetworkInterceptor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Running NetworkInterceptor v" + this.plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "Use '/networkinterceptor reload' to reload the configuration.");

            return true;
        }

        if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("networkinterceptor.command.reload")) {
            this.plugin.reload();

            sender.sendMessage(ChatColor.GOLD + "NetworkInterceptor configuration reloaded.");

            return true;
        } else if (args[0].equalsIgnoreCase("info") && sender.hasPermission("networkinterceptor.command.info")) {
            sendInfoMessage(sender);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand.");

        return true;
    }

    private void sendInfoMessage(CommandSender sender) {
        sender.sendMessage(getBlockerMessage());
        sender.sendMessage(getLoggerMessage());
        sender.sendMessage(getInterceptorsMessage());
    }

    public String getInterceptorsMessage() {
        // [07:53:17] [Server thread/INFO]: [NetworkInterceptor] Interceptors: [security-manager, proxy-selector]
        List<String> methods = plugin.getConfig().getStringList("methods");
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
        Map<InterceptMethod, Interceptor> interceptors = plugin.getInterceptors();
        if (methods.size() != interceptors.size()) {
            return "Real interceptors: " + interceptors + "\nConfig defined: " + methods; 
        }
        return "Interceptors: " + methods;
    }

    private String getLoggerMessage() {
        // [07:53:17] [Server thread/INFO]: [NetworkInterceptor] Using console logger
        if (!plugin.getConfig().getBoolean("logging.enabled", true)) {
            return "Logging is not enabled";
        }
        EventLogger logger = plugin.getEventLogger();
        if (logger == null) {
            String mode = plugin.getConfig().getString("logging.mode", "console");
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
        // [07:53:17] [Server thread/INFO]: [NetworkInterceptor] Using blocking strategy deny
        // [07:53:17] [Server thread/INFO]: [NetworkInterceptor] Using a mapping blocker
        Blocker blocker = plugin.getBlocker();
        String blockerMessage;
        if (blocker == null) {
            String mode = plugin.getConfig().getString("mode", "deny");
            if (!mode.equalsIgnoreCase("allow") && !mode.equalsIgnoreCase("deny")) {
                blockerMessage = "Unknown mode: " + mode;
            } else {
                blockerMessage = "Blocking is not enabled";
            }
        } else if (blocker instanceof LearningBlocker) {
            Blocker delegate = ((LearningBlocker) blocker).getDelegate();
            blockerMessage = getCompositeBlockerMessage(delegate) + "\nUsing a mapping blocker";
        } else {
            blockerMessage = getCompositeBlockerMessage(blocker);
        }
        return blockerMessage;
    }

    private String getCompositeBlockerMessage(Blocker blocker) {
        String blockerMessage;
        if (!(blocker instanceof CompositeBlocker)) {
            blockerMessage = "Unknown type of delegate: " + blocker;
        } else {
            CompositeBlocker compositeBlocker = (CompositeBlocker) blocker;
            Blocker[] delegates = compositeBlocker.getDelegates();
            if (delegates.length != 2) {
                blockerMessage = "Unknown delegates: " + Arrays.asList(delegates);
            } else {
                Blocker mainBlocker = delegates[0];
                Blocker pluginBlocker = delegates[1];
                if (!(pluginBlocker instanceof PluginAwareBlocker)) {
                    blockerMessage = "Miscondigured delegates: " + Arrays.asList(delegates);
                } else {
                    blockerMessage = getMainBlockerMessage(mainBlocker);
                }
            }
        }
        return blockerMessage;
    }

    private String getMainBlockerMessage(Blocker mainBlocker) {
        if (mainBlocker instanceof AllowBlocker) {
            return "Using blocking strategy allow";
        } else {
            return "Using blocking strategy deny";
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], OPTIONS, new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
