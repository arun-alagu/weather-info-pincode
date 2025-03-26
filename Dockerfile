FROM maven:3-openjdk-17 AS build
WORKDIR /app
COPY .env .
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/weather-info-pincode-0.0.1-SNAPSHOT.jar weather-info-pincode.jar
COPY .env .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "weather-info-pincode.jar"]