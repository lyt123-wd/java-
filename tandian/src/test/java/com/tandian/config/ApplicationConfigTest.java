package com.tandian.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 不依赖外部服务，校验配置文件语法与关键配置项是否存在。
 */
class ApplicationConfigTest {

    @Test
    void shouldLoadApplicationYaml() throws Exception {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<org.springframework.core.env.PropertySource<?>> sources =
                loader.load("application", new ClassPathResource("application.yaml"));

        assertFalse(sources.isEmpty());
        Object streamKey = sources.get(0).getProperty("tandian.seckill.stream-key");
        assertNotNull(streamKey);
        assertTrue(streamKey.toString().contains("stream.orders"));
        assertEquals("tandian", sources.get(0).getProperty("spring.application.name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldParseDockerCompose() throws Exception {
        try (InputStream input = Files.newInputStream(Path.of("docker-compose.yml"))) {
            Object document = new Yaml().load(input);
            assertNotNull(document);
            Map<String, Object> root = assertInstanceOf(Map.class, document);
            assertTrue(root.containsKey("services"));
            Map<String, Object> services = (Map<String, Object>) root.get("services");
            assertTrue(services.containsKey("mysql"));
            assertTrue(services.containsKey("redis"));
            assertTrue(services.containsKey("app"));
        }
    }
}
