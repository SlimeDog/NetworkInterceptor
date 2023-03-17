package me.lucko.networkinterceptor.bukkit;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import me.lucko.networkinterceptor.plugin.KeepPlugins;
import me.lucko.networkinterceptor.plugin.PluginOptions;

public class BukkitPluginOptions<T extends SlimeDogPlugin> extends PluginOptions<T> {
    private final SlimeDogPlugin owner;

    public BukkitPluginOptions(SlimeDogPlugin owner, KeepPlugins keepType, boolean allowNonPlugin,
            Set<String> plugins, boolean trust) {
        super(keepType, allowNonPlugin, plugins, trust);
        this.owner = owner;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean attemptAddPlugin(String name) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        if (plugin == null) {
            return false;
        }
        if (!(plugin instanceof JavaPlugin)) {
            owner.getLogger().warning("Plugin of unknown type (" + name + "): " + plugin);
            return false;
        }
        this.plugins.add((T) plugin); // unchecked
        return true;
    }

}
