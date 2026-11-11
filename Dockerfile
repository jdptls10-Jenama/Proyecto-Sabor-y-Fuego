
FROM eclipse-temurin:21-jdk AS build

ENV HOME=/app

WORKDIR $HOME

COPY . .

# Compile the Spring Boot and Vaadin application
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Extract Spring Boot application layers
RUN cp target/*.jar application.jar && \
    java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# Runtime image
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app

USER app

WORKDIR /application

COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "application.jar", "--spring.profiles.active=prod"]
