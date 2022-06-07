FROM amazoncorretto:17
# geotools fails if we use an non root user
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
