# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY plant-disease-server/pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY plant-disease-server/src ./src
RUN mvn -B -q -DskipTests package

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 1001 --create-home appuser

WORKDIR /app
COPY plant_disease_mobilenetv2.onnx /app/model.onnx
COPY --from=build /build/target/plant-disease-server.jar /app/app.jar

RUN mkdir -p /app/uploads && chown -R appuser:appuser /app
USER appuser

ENV MODEL_PATH=/app/model.onnx \
    UPLOAD_PATH=/app/uploads/ \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Duser.timezone=Asia/Shanghai"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=10 \
    CMD curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
