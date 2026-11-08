# ---------- 构建阶段 ----------
# 用法：docker build --build-arg MODULE=web/web-admin -t yujian-admin .
FROM maven:3.9-eclipse-temurin-17 AS build
ARG MODULE=web/web-admin
WORKDIR /build

COPY . .
RUN mvn -B -q -DskipTests -pl ${MODULE} -am package \
    && cp ${MODULE}/target/*.jar /build/app.jar

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 1001 --create-home appuser

WORKDIR /app
COPY --from=build --chown=appuser:appuser /build/app.jar /app/app.jar
USER appuser

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Duser.timezone=Asia/Shanghai"

EXPOSE 8080 8081

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
