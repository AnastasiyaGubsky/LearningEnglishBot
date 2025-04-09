FROM openjdk:21-jdk-slim AS builder

RUN apt-get update && apt-get install -y maven

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests -B

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=builder /app/target/LearningEnglishBot-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]