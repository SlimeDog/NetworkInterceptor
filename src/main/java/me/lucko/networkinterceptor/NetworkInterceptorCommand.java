package me.lucko.networkinterceptor;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.Collections;
import java.util.List;

public class NetworkInterceptorCommand implements TabExecutor {

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
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand.");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && StringUtil.startsWithIgnoreCase("reload", args[0])) {
            return Collections.singletonList("reload");
        }

        return Collections.emptyList();
    }
}
