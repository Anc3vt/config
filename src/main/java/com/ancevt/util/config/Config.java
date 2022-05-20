/*
 *   Ancevt Config
 *   Copyright (C) 2022 Ancevt (me@ancevt.com)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.ancevt.util.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Config {

    String REMOVED = "\0";

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

    void setProperty(String key, Object value);

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

    void ifContains(String key, Consumer<String> valueConsumer);

    void ifContainsOrElse(String key, Consumer<String> valueConsumer, Runnable orElseRunnable);

    Map<String, String> toMap();

    String stringify();

    String toFormattedEffectiveString(boolean decorated);
}
