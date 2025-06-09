# Use Maven to build the app
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run app using a lighter JDK base image
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=builder /app/target/taskmanager-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
