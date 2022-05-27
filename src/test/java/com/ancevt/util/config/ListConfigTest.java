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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListConfigTest {

    private static final String TEST_CONFIG_TEXT =
            "# this is first comment\n" +
                    "# this is second comment\n" +
                    "\n" +
                    "\n" +
                    "first.key=some value" +
                    "\n" +
                    "float = 3f\n" +
                    "\n" +
                    "#one more comment\n" +
                    "second.key = localhost:2255\n" +
                    "third.key   =  3333\\3333//\n" +
                    "\n" +
                    "\n";


    @Test
    void testParse() {
        new ListConfig().parse(TEST_CONFIG_TEXT);
    }

    @Test
    void testTypeCast() {
        ListConfig listConfig = new ListConfig();
        listConfig.parse(TEST_CONFIG_TEXT);
        float result = listConfig.getFloat("float", 0f);
        assertThat(result, is(3f));
    }

    @Test
    void testStoreLoad() throws IOException {
        ListConfig listConfigToStore = new ListConfig();
        listConfigToStore.parse(TEST_CONFIG_TEXT);
        listConfigToStore.store("test.tmp");

        ListConfig listConfigToLoad = new ListConfig();
        listConfigToLoad.load("test.tmp");

        assertThat(listConfigToLoad.getProperty("second.key"), is("localhost:2255"));

        Files.deleteIfExists(Path.of("test.tmp"));

        System.out.println(listConfigToLoad.toFormattedEffectiveString(false));
    }

    @Test
    void testChangeListenerSet() {
        ListConfig listConfig = new ListConfig();
        listConfig.parse(TEST_CONFIG_TEXT);

        AtomicBoolean changeSetValue = new AtomicBoolean();

        listConfig.addConfigChangeListener((k, v) -> {
            changeSetValue.set(k.equals("second.key") && v.equals("123"));
        });

        listConfig.setProperty("second.key", "123");
        assertTrue(changeSetValue.get());
    }

    @Test
    void testChangeListenerRemove() {
        ListConfig listConfig = new ListConfig();
        listConfig.parse(TEST_CONFIG_TEXT);

        AtomicBoolean changeSetValue = new AtomicBoolean();

        listConfig.addConfigChangeListener((k, v) -> {
            changeSetValue.set(k.equals("second.key") && v.equals(Config.REMOVED));
        });

        listConfig.removeProperty("second.key");
        assertFalse(listConfig.contains("second.key"));
    }

}






























