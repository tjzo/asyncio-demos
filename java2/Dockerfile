FROM openjdk:8

WORKDIR /app

ADD target/test2.jar /app

EXPOSE 8080

CMD ["java", "-jar", "test2.jar", "-DREMOTE_ADDR=$REMOTE_ADDR"]