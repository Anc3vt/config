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


import com.ancevt.util.texttable.TextTable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.lang.Byte.parseByte;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Short.parseShort;

public class ListConfig implements Config {

    private final List<BiConsumer<String, String>> configChangeListeners;

    private final List<KeyValue> list;
    private final Map<String, String> map;
    private Path defaultPath;

    public ListConfig() {
        configChangeListeners = new ArrayList<>();
        list = new ArrayList<>();
        map = new HashMap<>();
    }

    public ListConfig(Path defaultPath) {
        this();
        this.defaultPath = defaultPath;
    }

    public ListConfig(String defaultPath) {
        this();
        this.defaultPath = Path.of(defaultPath);
    }

    @Override
    public void addConfigChangeListener(BiConsumer<String, String> listener) {
        configChangeListeners.add(listener);
    }

    @Override
    public void removeConfigChangeListener(BiConsumer<String, String> listener) {
        configChangeListeners.remove(listener);
    }

    @Override
    public void clearConfigChangeListeners() {
        configChangeListeners.clear();
    }

    @Override
    public void setDefaultPath(Path path) {
        this.defaultPath = path;
    }

    @Override
    public void setDefaultPath(String path) {
        this.defaultPath = Path.of(path);
    }

    @Override
    public void store(@NotNull OutputStream outputStream) throws IOException {
        outputStream.write(stringify().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void store(String path) throws IOException {
        store(Path.of(path));
    }

    @Override
    public void store(Path path) throws IOException {
        Files.write(path, stringify().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    @Override
    public void store() throws IOException {
        if (defaultPath == null) throw new ConfigException("default path not defined");
        store(defaultPath);
    }

    @Override
    public void load(@NotNull InputStream inputStream) throws IOException {
        parse(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
    }

    @Override
    public void load(String path) throws IOException {
        load(Path.of(path));
    }

    @Override
    public void load(Path path) throws IOException {
        parse(Files.readString(path));
    }

    @Override
    public void load() throws IOException {
        if (defaultPath == null) throw new ConfigException("default path not defined");
        load(defaultPath);
    }

    @Override
    public void parse(@NotNull String source) {
        source.lines().forEach(line -> {
            // comment
            if (line.trim().startsWith("#")) {
                KeyValue commentKV = new KeyValue(line);
                list.add(commentKV);
            } else
                // empty line
                if (line.trim().equals("")) {
                    KeyValue emptyLineKV = new KeyValue();
                    list.add(emptyLineKV);
                } else {
                    KeyValue kv = KeyValue.parse(line);
                    setProperty(kv.key, kv.value);
                }
        });
    }

    @Override
    public void clear() {
        map.clear();
        list.clear();
    }

    @Override
    public void setProperty(String key, Object value) {
        map.put(key, String.valueOf(value));
        getPairOptional(key).ifPresentOrElse(
                kv -> kv.value = String.valueOf(value),
                () -> list.add(new KeyValue(key, String.valueOf(value)))
        );
        configChangeListeners.forEach(l -> l.accept(key, String.valueOf(value)));
    }

    @Override
    public String getProperty(String key) {
        return map.get(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        try {
            return parseInt(getProperty(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public long getLong(String key, long defaultValue) {
        try {
            return parseLong(getProperty(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return getProperty(key).equals("true");
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        try {
            return parseByte(getProperty(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public short getShort(String key, short defaultValue) {
        try {
            return parseShort(getProperty(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public char getChar(String key, char defaultValue) {
        try {
            return getProperty(key).charAt(0);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        try {
            return parseFloat(getProperty(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        try {
            return parseDouble(getProperty(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public void removeProperty(String key) {
        map.remove(key);
        for (int i = 0; i < list.size(); i++) {
            KeyValue kv = list.get(i);
            if (key.equals(kv.key)) {
                list.remove(i);
                configChangeListeners.forEach(l -> l.accept(key, REMOVED));
                break;
            }
        }
    }

    @Override
    public int getPropertyCount() {
        return list.size();
    }

    @Override
    public boolean contains(String key) {
        return map.containsKey(key);
    }

    @Override
    public void ifContains(String key, Consumer<String> valueConsumer) {
        getPairOptional(key).ifPresent(kv -> valueConsumer.accept(kv.value));
    }

    @Override
    public void ifContainsOrElse(String key, Consumer<String> valueConsumer, Runnable orElseRunnable) {
        getPairOptional(key).ifPresentOrElse(kv -> valueConsumer.accept(kv.value), orElseRunnable);
    }

    @Override
    public Map<String, String> toMap() {
        return Map.copyOf(map);
    }

    @Override
    public String stringify() {
        StringBuilder stringBuilder = new StringBuilder();

        list.forEach(kv -> {
            if (kv.isComment()) {
                stringBuilder.append(kv.comment).append(System.lineSeparator());
            } else if (kv.isEmpty()) {
                stringBuilder.append(System.lineSeparator());
            } else {
                stringBuilder.append(kv.key).append(" = ").append(kv.value).append(System.lineSeparator());
            }
        });

        return stringBuilder.toString();
    }

    @Override
    public String toFormattedEffectiveString(boolean decorated) {
        TextTable textTable = new TextTable(decorated, "Key", "Value");
        list.forEach(kv -> {
            if (!kv.isComment() && !kv.isEmpty()) textTable.addRow(kv.key, kv.value);
        });
        return textTable.render();
    }

    @Override
    public boolean fileExists() {
        return Files.exists(defaultPath);
    }

    private Optional<KeyValue> getPairOptional(String key) {
        for (KeyValue kv : list) {
            if (!kv.isComment() && !kv.isEmpty()) {
                if (kv.key.equals(key)) {
                    return Optional.of(kv);
                }
            }
        }
        return Optional.empty();
    }

    private static class KeyValue {
        String key;
        String value;
        String comment;

        // regular key value pair
        KeyValue(String kev, String value) {
            this.key = kev;
            this.value = value;
        }

        // comment line
        KeyValue(String comment) {
            this.comment = comment;
        }

        // empty line
        KeyValue() {

        }

        boolean isComment() {
            return comment != null;
        }

        boolean isEmpty() {
            return comment == null && key == null && value == null;
        }

        static @NotNull KeyValue parse(String line) {
            if (line.contains("=")) {
                StringTokenizer stringTokenizer = new StringTokenizer(line, "=");
                String key = stringTokenizer.nextToken().trim();
                String value = stringTokenizer.nextToken().trim();
                return new KeyValue(key, value);
            } else {
                throw new ConfigException("Parse error, line: " + line);
            }
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "key='" + key + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}


























