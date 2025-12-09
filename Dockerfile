FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ocorrencias-reactive-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]