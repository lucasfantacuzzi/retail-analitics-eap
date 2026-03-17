## Stage 1: build the WAR with Maven
FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -e -DskipTests clean package

## Stage 2: run on WildFly with PostgreSQL driver and config
# jboss/wildfly on Docker Hub stops at 25; use Quay.io for WildFly 26+ (Jakarta EE)
FROM quay.io/wildfly/wildfly:39.0.0.Final-jdk21

ENV JBOSS_HOME=/opt/jboss/wildfly

# PostgreSQL driver module
RUN curl -sL -o /tmp/postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.7.3.jar
RUN mkdir -p ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main
COPY wildfly/module.xml ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/
RUN mv /tmp/postgresql.jar ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/postgresql-42.7.3.jar

COPY --from=build /build/target/retail-analytics.war ${JBOSS_HOME}/standalone/deployments/retail-analytics.war

COPY wildfly/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080 9990

ENTRYPOINT ["/entrypoint.sh"]
