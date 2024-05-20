FROM openjdk:17

COPY build/libs/aggregation-service-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8080

EXPOSE $SERVER_PORT

ENTRYPOINT ["java","-jar","/app.jar"]