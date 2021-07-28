package me.lucko.networkinterceptor.velocity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;

import me.lucko.networkinterceptor.common.AbstractConfiguration;

public class VelocityConfiguration implements AbstractConfiguration {
    private static final String DELIMITER = ".";
    private final String currentPath;
    private final String name;
    private final Map<String, Object> map;

    public VelocityConfiguration(File file) throws FileNotFoundException {
        this(new Yaml().load(new FileInputStream(file)), "");
    }

    private VelocityConfiguration(Map<String, Object> map, String currentPath) {
        this.map = map;
        this.currentPath = currentPath;
        String[] split = currentPath.split(Pattern.quote(DELIMITER));
        this.name = split[split.length - 1]; // last part
    }

    @Override
    public Collection<String> getKeys(boolean deep) {
        if (deep) {
            throw new IllegalStateException("Deep keys are not supported at this time");
        }
        return map.keySet();
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        if (!deep) {
            throw new IllegalStateException("Shallow values are not supported at this time");
        }
        return map; // TODO - copy?
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(String path) {
        Map<String, Object> curMap = map;
        Object result = null;
        String[] split = path.split(Pattern.quote(DELIMITER));
        int counter = 0;
        for (String curPath : split) {
            counter++;
            result = curMap.get(curPath);
            if (!(result instanceof Map)) {
                if (counter < split.length) {
                    return false; // not found
                }
                return true; // contains
            }
            curMap = (Map<String, Object>) result; // unchecked
        }
        return true; // result is a map
    }

    @Override
    public boolean contains(String path, boolean ignoreDefault) {
        return contains(path); // TODO - better
    }

    @Override
    public boolean isSet(String path) {
        return contains(path); // TODO - better
    }

    @Override
    public String getCurrentPath() {
        return currentPath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object get(String path) {
        Map<String, Object> curMap = map;
        Object result = null;
        String[] split = path.split(Pattern.quote(DELIMITER));
        int counter = 0;
        for (String curPath : split) {
            counter++;
            result = curMap.get(curPath);
            if (!(result instanceof Map)) {
                if (counter < split.length) {
                    return null; // not found
                }
                return result;
            }
            curMap = (Map<String, Object>) result; // unchecked
        }
        return result;
    }

    @Override
    public Object get(String path, Object def) {
        Object o = get(path);
        return o == null ? def : o;
    }

    @Override
    public String getString(String path) {
        Object o = get(path);
        return o == null ? null : String.valueOf(o);
    }

    @Override
    public String getString(String path, String def) {
        String str = getString(path);
        return str == null ? def : str;
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public int getInt(String path, int def) {
        if (!contains(path)) {
            return def;
        }
        try {
            return Integer.parseInt(getString(path));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Override
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        if (!contains(path)) {
            return def;
        }
        try {
            return Boolean.parseBoolean(getString(path));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0.0D);
    }

    @Override
    public double getDouble(String path, double def) {
        if (!contains(path)) {
            return def;
        }
        try {
            return Double.parseDouble(getString(path));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Override
    public long getLong(String path) {
        return getLong(path, 0L);
    }

    @Override
    public long getLong(String path, long def) {
        if (!contains(path)) {
            return def;
        }
        try {
            return Long.parseLong(getString(path));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Override
    public List<?> getList(String path) {
        if (!contains(path)) {
            return null;
        }
        Object o = get(path);
        if (o instanceof List) {
            return (List<?>) o;
        }
        return null;
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        List<?> list = getList(path);
        return list == null ? def : list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        List<?> list = getList(path);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>(); // empty list
        }
        if (list.get(0) instanceof String) {
            return (List<String>) list; // unchecked
        }
        return new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public AbstractConfiguration getConfigurationSection(String path) {
        Object res = get(path);
        if (res instanceof Map) {
            return new VelocityConfiguration((Map<String, Object>) res, currentPath + DELIMITER + path); // unchecked
        }
        return null;
    }

}
