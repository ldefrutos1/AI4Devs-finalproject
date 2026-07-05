---
name: auditoria
description: Auditoría técnica externa del MVP (microservicios Spring Boot + Vue). Invocar explícitamente con /auditoria; también usable como prompt para herramientas externas (Claude, ChatGPT, etc.).
disable-model-invocation: true
---

# Auditoría externa — MVP microservicios (Spring Boot + Vue)

Rúbrica para una auditoría técnica independiente del MVP: calidad de código,
patrones de diseño, arquitectura y tests (estructura, no cobertura numérica).

Copia el bloque [PROMPT](#prompt) en tu herramienta de análisis (Claude, ChatGPT, etc.) junto con:

- El código fuente del repositorio (o acceso a él).
- Cualquier documentación existente (README, diagramas, ADRs).

---

## Prompt operativo (agente)

Cuando el usuario invoca **`/auditoria`** o adjunta esta skill:

1. Aplica la rúbrica de la sección [Prompt operativo (usar tal cual)](#prompt-operativo-usar-tal-cual) sobre el repositorio actual.
2. Revisa el código fuente real (no solo la documentación).
3. Evalúa cada microservicio de forma individual y luego el sistema en conjunto.
4. Entrega el [formato de salida esperado](#formato-de-salida-esperado).

---

## Prompt operativo (usar tal cual)

Actúa como un **arquitecto de software senior** realizando una **auditoría
técnica externa e independiente** de un MVP compuesto por varios
microservicios en **Spring Boot** (backend) y una **SPA en Vue** (frontend).

### Objetivo

Evaluar la **calidad del código, los patrones de diseño, las buenas prácticas
de arquitectura y la calidad estructural de los tests** (NO la cobertura
numérica de tests, sino si están bien diseñados, aislados y mantenibles).

### Alcance y metodología

1. Revisa el código fuente real (no solo la documentación).
2. Evalúa cada microservicio de forma individual y luego el sistema en conjunto
   (comunicación entre servicios, consistencia de criterios).
3. Para cada aspecto de la lista siguiente:
   - Da una **puntuación de 1 a 5** (1 = deficiente / ausente,
     5 = excelente / referencia de industria).
   - Justifica la puntuación con **evidencia concreta** (archivos, clases,
     líneas o patrones detectados).
   - Indica **1–3 recomendaciones accionables** de mejora, priorizadas por
     impacto/esfuerzo.
4. No penalices ni valores el **porcentaje de cobertura de tests**. Evalúa
   únicamente su **estructura, diseño e implementación**.
5. Si un aspecto no aplica al proyecto, indícalo explícitamente como **N/A**
   con la razón, en vez de forzar una puntuación.

### Aspectos a evaluar

#### 1. Arquitectura y diseño de microservicios

- Definición de límites de contexto (bounded contexts) y responsabilidad única
  por servicio.
- Grado de acoplamiento entre servicios (contratos de API, dependencias
  compartidas, bases de datos compartidas).
- Estrategia de comunicación (síncrona REST, eventos/mensajería, gRPC) y su
  idoneidad.
- Resiliencia: timeouts, retries, circuit breakers, manejo de fallos en cascada.
- Escalabilidad y statelessness de los servicios.

#### 2. Buenas prácticas y patrones en Spring Boot

- Separación en capas (Controller / Service / Repository) y responsabilidades
  bien delimitadas.
- Uso correcto de inyección de dependencias, sin anti-patrones (p. ej. `new`
  manual de beans, acoplamiento a implementaciones concretas).
- DTOs vs entidades de dominio; evitar exponer entidades JPA directamente en
  la API.
- Manejo centralizado de excepciones (`@ControllerAdvice`, códigos HTTP
  semánticos).
- Validación de entradas (Bean Validation, sanitización).
- Aplicación de principios SOLID y uso adecuado de patrones (Factory, Strategy,
  Builder, etc.) donde aporten valor real (evitar sobre-ingeniería).
- Gestión de transacciones y consistencia de datos.

#### 3. Calidad general del código

- Legibilidad y nomenclatura (clases, métodos, variables).
- Complejidad ciclomática y tamaño de métodos/clases (God classes, métodos
  largos).
- Duplicación de código (DRY).
- Gestión de configuración (externalización, perfiles `application.yml`, uso de
  variables de entorno, adherencia a 12-factor app).
- Logging: niveles adecuados, ausencia de datos sensibles en logs, trazabilidad
  (correlation IDs entre microservicios).
- Manejo de errores explícito y consistente (no silenciar excepciones, no usar
  excepciones para control de flujo).

#### 4. Estructura e implementación de los tests (sin valorar cobertura)

- Tipos de test presentes y su equilibrio (unitarios, integración, contract
  testing, e2e) según la pirámide de tests.
- Aislamiento correcto en tests unitarios (uso adecuado de mocks/stubs, sin
  dependencias externas reales).
- Claridad y mantenibilidad: patrón AAA (Arrange-Act-Assert) o
  Given-When-Then, nombres de test descriptivos.
- Independencia entre tests (no orden implícito, no estado compartido).
- Calidad de los asserts (verificaciones significativas, no solo "no lanza
  excepción").
- Uso de test containers / bases de datos embebidas para tests de integración
  realistas.
- Tests de contrato entre microservicios (si existen) y su utilidad real.
- Legibilidad y organización (estructura de carpetas, convenciones de nombres,
  builders/fixtures de datos de test).

#### 5. Frontend Vue

- Estructura del proyecto (organización de componentes, vistas, stores,
  composables/servicios).
- Gestión de estado (Vuex/Pinia): correcta separación de responsabilidades,
  evitar estado global innecesario.
- Uso idiomático de Composition API u Options API (consistencia en todo el
  proyecto).
- Reutilización de lógica (composables) vs duplicación entre componentes.
- Comunicación con el backend (capa de servicios/API centralizada, manejo de
  errores HTTP, interceptores).
- Calidad de los tests frontend (unitarios de componentes, mocking de llamadas
  HTTP) bajo los mismos criterios de estructura que el punto 4.

#### 6. Seguridad

- Autenticación y autorización (JWT, OAuth2, gestión de roles/permisos)
  implementadas correctamente entre servicios.
- Validación y sanitización de entradas para prevenir inyección (SQL, XSS,
  etc.).
- Gestión de secretos (no hardcodeados, uso de vault/variables de entorno).
- Exposición mínima de información sensible en respuestas de error.
- CORS configurado correctamente.

#### 7. Documentación y mantenibilidad

- Documentación de API (OpenAPI/Swagger) actualizada y coherente con el código
  real.
- README con instrucciones claras de arranque, configuración y despliegue.
- Comentarios útiles (explican el "por qué", no el "qué") y ausencia de código
  comentado/muerto.
- Existencia de decisiones de arquitectura documentadas (ADRs) si aplica.

#### 8. CI/CD y observabilidad (si aplica al alcance del MVP)

- Pipelines de build/test automatizados.
- Linters y análisis estático integrados (SonarQube, ESLint, Checkstyle, etc.).
- Health checks, métricas (Actuator/Micrometer) y trazabilidad distribuida.

### Formato de salida esperado

1. **Resumen ejecutivo** (máx. 10 líneas): estado general de calidad,
   principales fortalezas y riesgos.
2. **Tabla de puntuaciones** por aspecto (1–5) y por microservicio.
3. **Detalle por aspecto**: puntuación, evidencia y recomendaciones (según se
   describe arriba).
4. **Top 5 recomendaciones priorizadas** para el conjunto del sistema,
   ordenadas por impacto/esfuerzo.
5. **Nota de alcance**: aspectos no evaluables por falta de acceso/información,
   si los hubiera.

---

## Notas para ti (antes de lanzar la auditoría)

- Si el proyecto tiene **muchos microservicios**, considera dividir la auditoría
  en tandas (por dominio o por criticidad) para no perder profundidad de
  análisis.
- Puedes ajustar los pesos de cada bloque si algún aspecto es más relevante para
  el negocio (p. ej. si la seguridad es crítica, puedes pedir que se pondere más
  en el resumen ejecutivo).
- Si quieres comparabilidad entre auditorías futuras, mantén esta misma rúbrica
  sin cambios de una iteración a otra.
- Las revisiones previas en `docs/software-revisions/` pueden servir de línea
  base para comparar evolución entre auditorías.
