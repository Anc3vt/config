package com.ancevt.util.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

public interface Config {

    Object REMOVED = new Object();

    void addConfigChangeListener(BiConsumer<String, String> listener);
    void removeConfigChangeListener(BiConsumer<String, String> listener);
    void clearConfigChangeListeners();

    void setDefaultPath(String path);
    void setDefaultPath(Path path);

    void store(OutputStream outputStream) throws IOException;
    void store(String path) throws IOException;
    void store(Path path) throws IOException;
    void store() throws IOException;

    void load(InputStream inputStream) throws IOException;
    void load(String path) throws IOException;
    void load(Path path) throws IOException;
    void load() throws IOException;

    void parse(String source);

    void setProperty(String key, String value);

    String getProperty(String key);
    String getProperty(String key, String defaultValue);

    int getInt(String key, int defaultValue);
    long getLong(String key, long defaultValue);
    boolean getBoolean(String key, boolean defaultValue);
    byte getByte(String key, byte defaultValue);
    short getShort(String key, short defaultValue);
    char getChar(String key, char defaultValue);
    float getFloat(String key, float defaultFloat);
    double getDouble(String key, double defaultFloat);

    void removeProperty(String key);

    int getPropertyCount();

    void clear();

    boolean contains(String key);

    void ifContains(String key, BiConsumer<String, String> biConsumer);

    void ifContainsOrElse(String key, BiConsumer<String, String> biConsumer, Runnable orElseRunnable);

    Map<String, String> toMap();

    String stringify();

    String toFormattedEffectiveString(boolean decorated);
}
