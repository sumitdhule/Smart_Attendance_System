# ===============================
# Build stage
# ===============================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests


# ===============================
# Run stage
# ===============================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Copy haarcascade file (if required externally)
COPY src/main/resources/haarcascade_frontalface_default.xml /app/haarcascade_frontalface_default.xml

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"


ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
