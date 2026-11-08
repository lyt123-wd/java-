package com.yujian.lease.web.admin.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ApplicationConfigTest {

    @Test
    void shouldLoadAdminYaml() throws Exception {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yml"));

        assertFalse(sources.isEmpty());
        assertEquals("yujian-admin", sources.get(0).getProperty("spring.application.name"));
    }
}
