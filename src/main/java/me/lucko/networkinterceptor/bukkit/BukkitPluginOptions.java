package me.lucko.networkinterceptor.bukkit;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.lucko.networkinterceptor.plugin.KeepPlugins;
import me.lucko.networkinterceptor.plugin.PluginOptions;

public class BukkitPluginOptions<T extends JavaPlugin> extends PluginOptions<T> {
    private final JavaPlugin owner;
    private final Set<JavaPlugin> trustedPlugins = new HashSet<>();

    public BukkitPluginOptions(JavaPlugin owner, KeepPlugins keepType, boolean allowNonPlugin, Set<String> trustedPlugins) {
        super(keepType, allowNonPlugin, trustedPlugins);
        this.owner = owner;
    }

    @Override
    protected boolean attemptAddPlugin(String name) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        if (plugin == null) {
            return false;
        }
        if (!(plugin instanceof JavaPlugin)) {
            owner.getLogger().warning("Plugin of unknown type (" + name + "): " + plugin);
            return false;
        }
        this.trustedPlugins.add((JavaPlugin) plugin);
        return true;
    }

    @Override
    public boolean isTrusted(T plugin) {
        return trustedPlugins.contains(plugin);
    }
    
}
