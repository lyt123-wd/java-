# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 先复制 pom，利用 Docker 层缓存加速依赖下载
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests clean package

# ---------- 运行阶段 ----------
FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 1001 --create-home appuser

WORKDIR /app
COPY --from=build --chown=appuser:appuser /build/target/tandian.jar /app/app.jar
RUN mkdir -p /app/uploads && chown -R appuser:appuser /app

USER appuser
EXPOSE 8081

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Duser.timezone=Asia/Shanghai"

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=10 \
    CMD curl -fsS http://localhost:8081/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
