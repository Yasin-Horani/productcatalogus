# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy only the POM first so dependencies are cached in a separate layer
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Now copy the sources and build the executable jar
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8081

# Sensible container defaults; every value can be overridden at `docker run` time
ENV DB_DriverClass=org.h2.Driver \
    DB_URL=jdbc:h2:mem:productcatalogus \
    DB_USERNAME=sa \
    DB_PASSWORD= \
    JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

