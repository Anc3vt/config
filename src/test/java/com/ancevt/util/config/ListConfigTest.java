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






























