package me.lucko.networkinterceptor.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AbstractConfiguration {

    public Collection<String> getKeys(boolean deep);

    public Map<String, Object> getValues(boolean deep);

    public boolean contains(String path);

    public boolean contains(String path, boolean ignoreDefault);

    public boolean isSet(String path);

    public String getCurrentPath();

    public String getName();

    public Object get(String path);

    public Object get(String path, Object def);

    public String getString(String path);

    public String getString(String path, String def);

    public int getInt(String path);

    public int getInt(String path, int def);

    public boolean getBoolean(String path);

    public boolean getBoolean(String path, boolean def);

    public double getDouble(String path);

    public double getDouble(String path, double def);

    public long getLong(String path);

    public long getLong(String path, long def);

    public List<?> getList(String path);

    public List<?> getList(String path, List<?> def);

    public List<String> getStringList(String path);

    public AbstractConfiguration getConfigurationSection(String path);

}
