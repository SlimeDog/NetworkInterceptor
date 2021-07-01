package me.lucko.networkinterceptor.blockers;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.lucko.networkinterceptor.InterceptEvent;
import me.lucko.networkinterceptor.plugin.ManualPluginOptions;
import me.lucko.networkinterceptor.plugin.PluginOptions;

/**
 * This class is used to manually check stack trace for allowed plugins.
 * However, it is only meant to be used before the server is properly
 * initialized.The reason for its existance has to do with the fact that classes
 * are not properly registered with the class loader before the server is
 * initialized. This gives rise to the situation where a plugin is enabled and
 * using a network call, however, the class is unable to be identified. Which
 * leads to the plugin it is providing to be unable to be identified.
 *
 * PS: This process is not too performant (for a large number of manually
 * detected packages). It relies on iterating through all the packages defined
 * within config. It also somewhat redoes what is already done within
 * InterceptorEvent (going through the stack trace to find plguins). This is why
 * it should not be used when the server is fully operational.
 */
public class ManualPluginDetectingBlocker implements Blocker {
    private final PluginOptions pluginOptions;
    private final ManualPluginOptions manualPluginOptions;

    public ManualPluginDetectingBlocker(PluginOptions pluginOptions, ManualPluginOptions manualPluginOptions) {
        this.pluginOptions = pluginOptions;
        this.manualPluginOptions = manualPluginOptions;
    }

    @Override
    public boolean shouldBlock(InterceptEvent event) {
        String pluginName = findFirstPluginName(event);
        if (pluginName == null) { // none found
            return true; // block
        }
        boolean shouldBlock = !pluginOptions.isListedAsTrustedPluginName(pluginName);
        if (!shouldBlock) { // try to add trusted plugin
            JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(pluginName);
            if (plugin != null) {
                event.setTrustedPlugin(plugin);
            }
        }
        return shouldBlock;
    }

    private String findFirstPluginName(InterceptEvent event) {
        for (Map.Entry<StackTraceElement, JavaPlugin> entry : event.getNonInternalStackTraceWithPlugins().entrySet()) {
            if (entry.getValue() != null) {
                continue; // already found
            }
            StackTraceElement trace = entry.getKey();
            String pluginName = manualPluginOptions.getPluginNameFor(trace.getClassName());
            if (pluginName != null) {
                JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(pluginName);
                if (plugin != null) {
                    event.updateTraceElement(trace, plugin);
                }
                return pluginName;
            }
        }
        return null;
    }

}
