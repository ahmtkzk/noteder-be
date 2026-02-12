# --- BUILD STAGE ---
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- RUN STAGE ---
FROM bellsoft/liberica-openjre-alpine:21
WORKDIR /app
COPY --from=build /app/target/*.jar /app/noteder.jar
ENTRYPOINT ["java", "-Duser.timezone=Europe/Istanbul", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75", "-jar", "/app/noteder.jar"]