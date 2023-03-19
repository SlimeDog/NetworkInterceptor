package me.lucko.networkinterceptor.common;

public interface PluginManager<PLUGIN> {

    /**
     * Get the name of the PLUGIN instance.
     *
     * @param plugin the instance to get the name for
     * @return the name of the plugin
     */
    String getName(PLUGIN plugin);

    /**
     * Get the PLUGIN by its name (or null).
     *
     * @param name the name to search
     * @return the PLUGIN instance, or null
     */
    PLUGIN getPlugin(String name);

}
