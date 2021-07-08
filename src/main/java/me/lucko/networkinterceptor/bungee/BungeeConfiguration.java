package me.lucko.networkinterceptor.bungee;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import me.lucko.networkinterceptor.common.AbstractConfiguration;
import net.md_5.bungee.config.Configuration;

public class BungeeConfiguration implements AbstractConfiguration {
    private final Configuration config;
    private final String path;
    private final String name;

    public BungeeConfiguration(Configuration config, String path) {
        this.config = config;
        this.path = path;
        String[] split = path.split("\\.");
        this.name = split[split.length - 1]; // last part
    }

    @Override
    public Collection<String> getKeys(boolean deep) {
        return config.getKeys();
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        throw new IllegalStateException("Not implemented for bungee");
    }

    @Override
    public boolean contains(String path) {
        return config.contains(path);
    }

    @Override
    public boolean contains(String path, boolean ignoreDefault) {
        return config.contains(path);
    }

    @Override
    public boolean isSet(String path) {
        return config.contains(path);
    }

    @Override
    public String getCurrentPath() {
        return path;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object get(String path) {
        return config.get(path);
    }

    @Override
    public Object get(String path, Object def) {
        return config.get(path, def);
    }

    @Override
    public String getString(String path) {
        return config.getString(path);
    }

    @Override
    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    @Override
    public int getInt(String path) {
        return config.getInt(path);
    }

    @Override
    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    @Override
    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    @Override
    public double getDouble(String path) {
        return config.getDouble(path);
    }

    @Override
    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    @Override
    public long getLong(String path) {
        return config.getLong(path);
    }

    @Override
    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    @Override
    public List<?> getList(String path) {
        return config.getList(path);
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        return config.getList(path, def);
    }

    @Override
    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    @Override
    public AbstractConfiguration getConfigurationSection(String path) {
        Configuration section = config.getSection(path);
        return section == null ? null : new BungeeConfiguration(section, path);
    }

}
