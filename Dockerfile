FROM openjdk:alpine

ADD build/libs/my-order-service-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=${ENVIRONMENT}", "/app.jar"]

EXPOSE 8085
