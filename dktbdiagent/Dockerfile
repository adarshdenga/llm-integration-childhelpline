FROM maven:3.8.3-openjdk-16 AS build
COPY . .
RUN mvn clean package -Dmaven.test.skip


FROM openjdk:16 AS run
COPY files files
COPY --from=build target/agent-0.0.1-SNAPSHOT.jar agent-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "agent-0.0.1-SNAPSHOT.jar"]
EXPOSE 8080