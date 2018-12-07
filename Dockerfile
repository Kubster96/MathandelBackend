FROM openjdk:8

COPY . .
RUN ./gradlew build jar -x test
EXPOSE 8080
ENTRYPOINT ["java","-jar","./build/libs/backend-0.0.1-SNAPSHOT.jar", "--spring.config.name=docker.application"]
