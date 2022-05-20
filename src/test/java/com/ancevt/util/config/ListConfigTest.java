package com.ancevt.util.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class ListConfigTest {

    private static final String TEST_CONFIG =
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
        new ListConfig().parse(TEST_CONFIG);
    }

    @Test
    void testTypeCast() {
        ListConfig listConfig = new ListConfig();
        listConfig.parse(TEST_CONFIG);
        float result = listConfig.getFloat("float", 0f);
        assertThat(result, is(3f));
    }

    @Test
    void testStoreLoad() throws IOException {
        ListConfig listConfigToStore = new ListConfig();
        listConfigToStore.parse(TEST_CONFIG);
        listConfigToStore.store("test.tmp");

        ListConfig listConfigToLoad = new ListConfig();
        listConfigToLoad.load("test.tmp");

        assertThat(listConfigToLoad.getProperty("second.key"), is("localhost:2255"));

        Files.deleteIfExists(Path.of("test.tmp"));

        System.out.println(listConfigToLoad.toFormattedEffectiveString(false));
    }

}
