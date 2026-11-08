package com.plant.disease.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验仓库根目录 docker-compose.yml 的语法与核心服务定义。
 */
class DeploymentConfigTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldParseDockerCompose() throws Exception {
        Path composeFile = Path.of("../docker-compose.yml");
        assertTrue(Files.exists(composeFile), "docker-compose.yml 应该位于仓库根目录");

        try (InputStream input = Files.newInputStream(composeFile)) {
            Object document = new Yaml().load(input);
            assertNotNull(document);
            Map<String, Object> root = assertInstanceOf(Map.class, document);
            Map<String, Object> services = assertInstanceOf(Map.class, root.get("services"));
            assertTrue(services.containsKey("mysql"));
            assertTrue(services.containsKey("app"));
            assertTrue(services.containsKey("web"));
        }
    }
}
