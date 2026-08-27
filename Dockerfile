FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY gradle-9.5.1-bin.zip /tmp/gradle.zip

RUN mkdir -p /opt/gradle \
    && cd /opt/gradle \
    && jar xf /tmp/gradle.zip \
    && chmod +x /opt/gradle/gradle-9.5.1/bin/gradle \
    && rm /tmp/gradle.zip

ENV PATH="/opt/gradle/gradle-9.5.1/bin:${PATH}"

COPY settings.gradle.kts build.gradle.kts ./

COPY src ./src

RUN gradle bootJar --no-daemon \
    && cp build/libs/*-SNAPSHOT.jar /workspace/app.jar \
    && cp build/datadog/dd-java-agent.jar /workspace/dd-java-agent.jar

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --create-home --shell /usr/sbin/nologin appuser

COPY --from=build /workspace/app.jar /app/app.jar
COPY --from=build /workspace/dd-java-agent.jar /opt/datadog/dd-java-agent.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS=""
ENV JAVA_TOOL_OPTIONS="-javaagent:/opt/datadog/dd-java-agent.jar"
ENV DD_SERVICE="espresso-api"
ENV DD_TRACE_ENABLED="true"
ENV DD_LOGS_INJECTION="true"
ENV DD_RUNTIME_METRICS_ENABLED="true"
ENV DD_AGENT_HOST="127.0.0.1"
ENV DD_TRACE_AGENT_PORT="8126"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
