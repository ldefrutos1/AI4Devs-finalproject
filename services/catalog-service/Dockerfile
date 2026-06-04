# catalog-service (Opcion A): empaqueta el jar ya compilado en una imagen JRE.
# El jar se genera ANTES con (desde la raiz del repo):
#   mvn -f services/pom.xml -pl catalog-service -am package -DskipTests
FROM eclipse-temurin:21-jre

# curl: lo usa el healthcheck del docker-compose (actuator/health).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Spring Boot deja el jar ejecutable como target/<artifactId>-<version>.jar
# (el *.jar.original no coincide con el patron *.jar).
COPY target/*.jar app.jar

# Puerto efectivo dentro de la red E2E (el base usa 8081; aqui homogeneizamos a 8080).
ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
