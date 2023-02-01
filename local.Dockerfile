FROM amazoncorretto:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Xmx256m","-XX:+UseSerialGC","-XX:+UseContainerSupport","-XX:+PrintFlagsFinal","-jar","/app.jar"]
