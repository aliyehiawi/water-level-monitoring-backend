FROM gradle:8.5-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle
RUN chmod +x ./gradlew && ./gradlew --no-daemon dependencies || true

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S -u 1001 spring -G spring && chown -R spring:spring /app
USER spring:spring

COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

