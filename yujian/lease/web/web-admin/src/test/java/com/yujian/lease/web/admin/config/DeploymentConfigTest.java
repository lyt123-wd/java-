package com.yujian.lease.web.admin.config;

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
 * 校验仓库根目录的 docker-compose.yml 语法和核心服务定义。
 * surefire 的工作目录是模块目录，因此通过 ../../../ 回到仓库根目录。
 */
class DeploymentConfigTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldParseDockerCompose() throws Exception {
        Path composeFile = Path.of("../../../docker-compose.yml");
        assertTrue(Files.exists(composeFile), "docker-compose.yml 应该位于仓库根目录");

        try (InputStream input = Files.newInputStream(composeFile)) {
            Object document = new Yaml().load(input);
            assertNotNull(document);
            Map<String, Object> root = assertInstanceOf(Map.class, document);
            Map<String, Object> services = assertInstanceOf(Map.class, root.get("services"));
            assertTrue(services.containsKey("mysql"));
            assertTrue(services.containsKey("redis"));
            assertTrue(services.containsKey("minio"));
            assertTrue(services.containsKey("web-admin"));
            assertTrue(services.containsKey("web-app"));
            assertTrue(services.containsKey("admin-web"));
            assertTrue(services.containsKey("h5-web"));
        }
    }
}
