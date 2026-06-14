# Revision backend Java

| Campo | Valor |
|-------|-------|
| **Fecha** | 2026-06-14 |
| **Alcance** | `services/` (Java 21 + Spring Boot 4 + microservicios) |
| **Tipo** | Evaluacion tecnica de arquitectura Java, microservicios, calidad, seguridad y mantenibilidad |
| **Revisor** | Asistente IA (perfil: experto Java/Spring y microservicios) |
| **Estado** | Borrador |

---

## Resumen ejecutivo

El backend Java presenta una arquitectura madura para un MVP de microservicios: Spring Boot 4, Java 21, gateway, servicios por contexto, seguridad JWT, persistencia separada por esquemas, Flyway, Kafka, observabilidad Actuator/Micrometer, errores `ProblemDetail`, correlacion `X-Correlation-Id` y una estrategia de tests razonable.

Respecto a revisiones anteriores, no hay disparidad relevante. La revision integral de 2026-05-31 daba a la implementacion Java un **4,0 / 5**. Desde entonces se han incorporado mejoras concretas que justifican subir la valoracion: timeouts HTTP explicitos entre servicios, auditoria de fallo parcial en la baja distribuida catalogo-media, alineacion del patron comun MVC de seguridad/error/correlacion y eliminacion de pequenas desviaciones de paquetes.

La valoracion actual queda en **4,4 / 5**. Es coherente con la evolucion observada: el diseno ya era bueno y los ultimos cambios han reducido riesgos transversales sin introducir complejidad innecesaria para el MVP.

---

## Calificacion por apartados

| Apartado | Nota | Evaluacion |
|----------|:----:|------------|
| **Arquitectura de microservicios** | **4,5** | Buena separacion por contexto: `catalog`, `media`, `notification`, `api-gateway` y esqueleto futuro de IA. Las dependencias entre servicios son comprensibles y acotadas para MVP. |
| **Implementacion Spring Boot** | **4,4** | Uso disciplinado de Spring Boot 4, Java 21, MVC, validacion, seguridad, Actuator, perfiles y configuracion por propiedades. |
| **Estructura de paquetes** | **4,4** | Patron claro `controller`, `application`, `domain`, `dto`, `exception`, `infrastructure`, `web`, `config`. `catalog-service` funciona como referencia. La carpeta `validation` ya no aparece en `media-service`. |
| **API y contrato HTTP** | **4,3** | Endpoints alineados con OpenAPI, DTOs separados de entidades, errores `ProblemDetail` y rutas publicas/protegidas diferenciadas. |
| **Seguridad backend** | **4,4** | JWT resource server, roles de realm Keycloak convertidos a `ROLE_*`, rutas por rol y defensa en profundidad con gateway + servicios. |
| **Errores y correlacion** | **4,5** | Patron comun MVC documentado: 401/403 como `ProblemDetail`, `ProblemDetailEnricher`, `ProblemHttpWriter`, `CorrelationIdFilter` y MDC. |
| **Persistencia y migraciones** | **4,4** | Flyway por servicio, esquemas separados, JPA donde aporta y SQL nativo justificado en catalogo. Buen equilibrio para MVP. |
| **Operaciones distribuidas** | **4,2** | La baja catalogo-media esta resuelta de forma simple: no hay sagas, pero si auditoria de fallo parcial y comportamiento explicito. Adecuado para MVP. |
| **Resiliencia HTTP entre servicios** | **4,1** | Ya existen timeouts explicitos catalogo-media y media-catalogo. Falta aun una politica mas completa de retries/circuit breaker, pero no es necesaria para el MVP. |
| **Kafka y asincronia** | **4,3** | Publicacion de eventos de catalogo y consumo en notificaciones con idempotencia. Buen uso de Kafka sin sobredisenar. |
| **Testing Java** | **4,2** | Hay 84 ficheros de test/IT en `services/`. Catalogo esta especialmente cubierto; media y notification tienen cobertura razonable pero menos amplia. |
| **Observabilidad** | **4,3** | Actuator, Prometheus, logs JSON, application/environment tags y correlacion. Buen nivel para entorno local/MVP. |
| **Mantenibilidad** | **4,4** | Codigo modular, reglas documentadas y patron comun claro. Riesgo principal: duplicacion futura de piezas transversales si crecen mas servicios. |
| **DevOps/backend local** | **4,1** | Maven reactor, perfiles, Compose y documentacion de arranque bien resueltos. La automatizacion CI/E2E podria ser mas estricta. |

---

## Fortalezas principales

### Separacion clara por contexto

Los servicios tienen responsabilidades reconocibles:

- `catalog-service`: ejemplares, maestros, permisos, auditoria, eventos y consultas publicas.
- `media-service`: presign, subida, confirmacion, galeria y borrado de fotos.
- `notification-service`: suscripciones, consumo de eventos, idempotencia y envio.
- `api-gateway`: entrada unica, CORS, token relay, rutas y seguridad perimetral.

Esta separacion es adecuada para el producto y evita mezclar dominios en un unico servicio.

### Catalog-service como referencia

`catalog-service` es el servicio mas completo y sirve bien como implementacion de referencia: estructura de paquetes, seguridad, errores, correlacion, auditoria, persistencia, Kafka, cache y tests.

### Patron comun MVC transversal

El patron de seguridad, error y correlacion esta ahora documentado y alineado entre servicios. Esto mejora mucho la mantenibilidad sin crear una libreria comun prematura.

### Manejo pragmativo de operaciones distribuidas

La baja de ejemplar evita introducir sagas en el MVP. En su lugar, usa una estrategia simple: borrar media, intentar borrar catalogo y auditar fallo parcial si catalogo falla despues de media OK. Es una decision proporcionada.

### Resiliencia minima ya incorporada

Las llamadas HTTP internas tienen timeouts explicitos:

- `catalog-service` hacia `media-service`;
- `media-service` hacia `catalog-service`.

Esto evita esperas indefinidas y sube la robustez sin meter dependencias o patrones pesados.

### Tests relevantes

La presencia de 84 ficheros de test/IT en `services/` es una buena senal. Destacan los tests de aplicacion en catalogo y los tests de integracion donde aportan valor.

---

## Riesgos y mejoras recomendadas

### 1. Evitar libreria comun prematura

Prioridad baja. Aunque hay piezas repetidas de seguridad/error/correlacion, para el MVP es mejor mantener el patron documentado que extraer una dependencia interna. La libreria comun solo tendria sentido si aparece un cuarto o quinto servicio MVC con duplicacion dolorosa.

### 2. Ampliar cobertura equilibrada en media y notification

Prioridad media. Catalogo esta mas maduro. Media y notification estan bien, pero conviene reforzar pruebas de casos limite:

- timeouts/fallos HTTP entre servicios;
- errores de almacenamiento en media;
- reintentos o estados de envio en notificaciones;
- contratos de permisos media-catalogo.

### 3. Endurecer resiliencia solo cuando haya necesidad real

Prioridad baja/media. Los timeouts son suficientes para MVP. Antes de meter circuit breakers o retries, conviene medir fallos reales y decidir por endpoint. Un retry mal puesto en operaciones no idempotentes puede empeorar el sistema.

### 4. Consolidar CI/E2E backend

Prioridad media. La estrategia de tests esta documentada y hay `system-e2e-tests`, pero la nota subiria con una ejecucion automatizada mas visible de:

- `mvn test`;
- `mvn verify` donde aplique;
- E2E del gateway contra servicios reales o entorno controlado.

### 5. Vigilar crecimiento de application services

Prioridad baja/media. Algunos servicios de aplicacion concentran bastante logica de orquestacion. Si crecen nuevas HU, convendra dividir por caso de uso y mantener metodos pequenos.

---

## Coherencia con evaluaciones anteriores

No se detecta contradiccion con la nota anterior. La diferencia se explica por alcance y fecha:

| Revision | Nota Java/backend | Motivo |
|----------|:-----------------:|--------|
| 2026-05-31, revision integral | **4,0 / 5** | Evaluacion anterior a las mejoras recientes y dentro de una revision global del proyecto. |
| 2026-06-14, revision actual Java | **4,4 / 5** | Incluye timeouts HTTP, auditoria de fallo parcial, patron MVC documentado/alineado y limpieza de paquetes. |

La subida es razonable porque las mejoras atacan precisamente puntos que antes bajaban la nota: resiliencia, consistencia transversal y operaciones distribuidas simples.

---

## Verificacion realizada

- Revision de estructura de `services/`.
- Revision de `services/README.md` y reglas backend aplicables.
- Comprobacion de paquetes principales en `catalog-service`, `media-service` y `notification-service`.
- Conteo aproximado: 342 ficheros Java y 84 ficheros de test/IT en `services/`.
- Verificacion previa en esta sesion: `mvn -f services/pom.xml -pl catalog-service,media-service test` ejecutado correctamente tras los cambios de timeouts y auditoria.

---

## Conclusion

El backend Java esta en muy buen estado para un MVP con arquitectura de microservicios. La implementacion no esta sobredisenada: evita sagas y librerias comunes prematuras, pero incorpora los minimos profesionales necesarios en seguridad, errores, correlacion, auditoria, timeouts, tests y observabilidad.

La nota actual es **4,4 / 5**. Para subir hacia **4,6 / 5**, los pasos mas rentables serian reforzar tests de resiliencia/casos limite en media y notification, automatizar mas claramente E2E/CI y seguir vigilando el crecimiento de servicios de aplicacion.
