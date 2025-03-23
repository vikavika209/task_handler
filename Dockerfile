FROM maven:3.9.1-eclipse-temurin-17 as builder

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk

WORKDIR /app

COPY --from=builder /build/target/task-manager.jar app.jar

CMD ["java", "-jar", "app.jar"]
