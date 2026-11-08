package com.plant.disease.service;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClassNamesResourceTest {

    @Test
    void shouldPackage39ClassNames() throws Exception {
        InputStream input = getClass().getResourceAsStream("/class_names.txt");
        assertNotNull(input, "class_names.txt 必须打包进后端资源");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            long count = reader.lines().filter(line -> !line.isBlank()).count();
            assertEquals(39, count);
        }
    }
}
