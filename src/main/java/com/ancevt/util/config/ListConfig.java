package com.ancevt.util.config;


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

    public ListConfig() {
        configChangeListeners = new ArrayList<>();
        list = new ArrayList<>();
        map = new HashMap<>();
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
    public void setProperty(String key, String value) {
        map.put(key, value);
        getPairOptional(key).ifPresentOrElse(
                kv -> kv.value = value,
                () -> list.add(new KeyValue(key, value))
        );
        configChangeListeners.forEach(l -> l.accept(key, value));
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
    public void ifContains(String key, BiConsumer<String, String> biConsumer) {
        getPairOptional(key).ifPresent(kv -> biConsumer.accept(kv.key, kv.value));
    }

    @Override
    public void ifContainsOrElse(String key, BiConsumer<String, String> biConsumer, Runnable orElseRunnable) {
        getPairOptional(key).ifPresentOrElse(kv -> biConsumer.accept(kv.key, kv.value), orElseRunnable);
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
    public String toFormattedString() {
        return null;
    }

    private Optional<KeyValue> getPairOptional(String key) {
        for (KeyValue kv : list) {
            if (kv.key.equals(key)) {
                return Optional.of(kv);
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
            StringTokenizer stringTokenizer = new StringTokenizer(line, "=");
            String key = stringTokenizer.nextToken().trim();
            String value = stringTokenizer.nextToken().trim();
            return new KeyValue(key, value);
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


























