# Stage 1: build
FROM maven:3-amazoncorretto-17 as build

# Install all pugins and dependencies
WORKDIR /usr/local/app
COPY pom.xml /usr/local/app/
RUN mvn dependency:resolve-plugins
RUN mvn dependency:resolve

# Copy source and build app
COPY ./src /usr/local/app/src
RUN mvn package

# Stage 2: run app
FROM amazoncorretto:17
COPY --from=build /usr/local/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
