package me.lucko.networkinterceptor.bukkit;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import dev.ratas.slimedogcore.api.config.SDCConfiguration;
import dev.ratas.slimedogcore.api.config.SDCCustomConfig;
import me.lucko.networkinterceptor.common.AbstractConfiguration;

public class BukkitConfiguration implements AbstractConfiguration {
    private final SDCConfiguration config;

    public BukkitConfiguration(SDCCustomConfig config) {
        this(config.getConfig());
    }

    public BukkitConfiguration(SDCConfiguration config) {
        this.config = config;
    }

    @Override
    public Collection<String> getKeys(boolean deep) {
        return config.getKeys(deep);
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        return config.getValues(deep);
    }

    @Override
    public boolean contains(String path) {
        return config.contains(path);
    }

    @Override
    public boolean contains(String path, boolean ignoreDefault) {
        return config.contains(path, ignoreDefault);
    }

    @Override
    public boolean isSet(String path) {
        return config.isSet(path);
    }

    @Override
    public String getCurrentPath() {
        return config.getCurrentPath();
    }

    @Override
    public String getName() {
        return config.getName();
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
        SDCConfiguration section = config.getConfigurationSection(path);
        return section == null ? null : new BukkitConfiguration(section);
    }

}
