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
    && cp build/libs/*-SNAPSHOT.jar /workspace/app.jar

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --create-home --shell /usr/sbin/nologin appuser

COPY --from=build /workspace/app.jar /app/app.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
