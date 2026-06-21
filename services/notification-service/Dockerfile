# notification-service (Opcion A): empaqueta el jar ya compilado en una imagen JRE.
# El jar se genera ANTES con (desde la raiz del repo):
#   mvn -f services/pom.xml -pl notification-service -am package -DskipTests
FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/*.jar app.jar

ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
