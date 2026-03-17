## Stage 1: build the WAR with Maven
FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -e -DskipTests clean package

## Stage 2: run on WildFly (EAP-like) application server
FROM jboss/wildfly:30.0.0.Final

ENV JBOSS_HOME=/opt/jboss/wildfly

COPY --from=build /build/target/retail-analytics.war ${JBOSS_HOME}/standalone/deployments/retail-analytics.war

EXPOSE 8080

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]

