/**
 * Copyright (C) 2022 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    boolean fileExists();
}
