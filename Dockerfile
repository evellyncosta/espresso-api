FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

COPY src ./src

RUN ./gradlew bootJar --no-daemon \
    && cp build/libs/*-SNAPSHOT.jar /workspace/app.jar \
    && cp build/opentelemetry/opentelemetry-javaagent.jar /workspace/opentelemetry-javaagent.jar

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --create-home --shell /usr/sbin/nologin appuser

COPY --from=build /workspace/app.jar /app/app.jar
COPY --from=build /workspace/opentelemetry-javaagent.jar /opt/opentelemetry/opentelemetry-javaagent.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS=""
ENV JAVA_TOOL_OPTIONS="-javaagent:/opt/opentelemetry/opentelemetry-javaagent.jar"
ENV OTEL_SERVICE_NAME="espresso-api"
ENV OTEL_RESOURCE_ATTRIBUTES="deployment.environment.name=production"
ENV OTEL_EXPORTER_OTLP_PROTOCOL="grpc"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://espresso-app-otel-collector:4317"
ENV OTEL_TRACES_EXPORTER="otlp"
ENV OTEL_METRICS_EXPORTER="otlp"
ENV OTEL_LOGS_EXPORTER="none"
ENV OTEL_PROPAGATORS="tracecontext,baggage"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
