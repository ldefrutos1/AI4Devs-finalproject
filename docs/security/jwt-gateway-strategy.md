# Estrategia JWT y API Gateway (MyTreeLibrary)

Documento operativo alineado con [readme.md](../../readme.md) §3.5, [infra/compose/README.md](../../infra/compose/README.md) y [.cursor/rules/api-security.mdc](../../.cursor/rules/api-security.mdc).

## 1. Rol del Keycloak y del JWT

- La **SPA** obtiene **access token** (JWT) mediante **OIDC Authorization Code + PKCE** contra el realm **`mtl`**.
- Las llamadas a la API van al **API Gateway** con cabecera `Authorization: Bearer <access_token>`.
- Los roles de negocio se expresan como **roles de realm** en Keycloak: **`COLABORADOR`** y **`ADMIN`** (aparecen en el JWT bajo `realm_access.roles` salvo configuración contraria).

## 2. Validación en el Gateway (MVP)

1. El gateway actúa como **OAuth2 Resource Server** y valida el JWT (firma, `iss`, `exp`, etc.) frente al **issuer** del realm. En código, `spring.security.oauth2.resourceserver.jwt.issuer-uri` se parametriza con **`MTL_JWT_ISSUER_URI`** (por defecto `http://localhost:8180/realms/mtl` en desarrollo local; host, puerto y despliegue en contenedor: §5).
2. **Lista blanca de rutas públicas** alineada con [docs/api/openapi.yaml](../api/openapi.yaml): p. ej. `/api/catalog/public/**` (consulta pública de catálogo; JWT **opcional** sin cabecera `Authorization` o con Bearer **válido** — si el cliente envía un token válido con rol **COLABORADOR** o **ADMIN**, el gateway lo reenvía y **catalog-service** amplía filtros/visibilidad frente al visitante anónimo; un Bearer malformado, inválido o caducado puede responder **401** aunque la ruta sea pública, por el OAuth2 Resource Server del gateway — la SPA usa `publicApiFetch` sin Bearer en consultas públicas); en media, `GET /api/media/public/**` (miniatura de foto principal en listado público), `GET /api/media/trees/*/photos` (metadatos de galería) y `GET /api/media/trees/*/photos/*/content` (binario de cada foto visible; **HU-014**); `POST /api/notifications/subscriptions`; y endpoints de actuator acordados (`/actuator/health`, `/actuator/info`). En rutas de lectura de fotos el gateway **no exige** JWT válido, pero **reenvía** `Authorization` si el cliente envía un Bearer aceptado (token relay) para que **media-service** aplique visibilidad `PUBLIC` vs `PUBLIC`+`PRIVATE`. El resto de rutas bajo `/api/**` exigen JWT válido. Peticiones **fuera** de `/api/**` quedan bloqueadas; Spring Security + OAuth2 RS pueden responder **401** (`WWW-Authenticate: Bearer`) o **403** según el caso — en ambos casos no hay API anónima fuera del prefijo acordado.
3. **Autorización por roles en el gateway** (opcional en MVP): reglas por ruta para exigir `ADMIN` donde el contrato de producto lo reserve (p. ej. maestros); el resto puede delegarse en microservicios.

### 2.1. Implementación actual (`services/api-gateway`)

- Stack: **Spring Boot 4**, **Spring Cloud Gateway** en modo servidor **WebFlux**; dependencia Maven **`spring-cloud-starter-gateway-server-webflux`** (train Spring Cloud **2025.1.x** / Gateway **5.x**; el artefacto histórico `spring-cloud-starter-gateway` ya no aplica en ese train).
- Rutas proxy: definidas en **`spring.cloud.gateway.server.webflux.routes`**. URIs de destino en YAML como **`mtl.catalog.uri`**, **`mtl.media.uri`**, **`mtl.notification.uri`**, **`mtl.ai.uri`** (por defecto `http://localhost:8081` … `8084`); en despliegue suelen mapearse desde **`MTL_CATALOG_URI`**, **`MTL_MEDIA_URI`**, etc., vía *relaxed binding* de Spring Boot. Prefijos `/api/catalog/**`, `/api/media/**`, `/api/notifications/**`, `/api/ai/**`.
- Tras validar el JWT, el cliente HTTP del gateway **reenvía** `Authorization: Bearer` al upstream (**token relay**); los microservicios deben configurar su propio resource server con el mismo criterio de `issuer-uri` cuando expongan API.
- **Roadmap técnico del módulo gateway** (pendiente): timeouts configurables del proxy hacia upstreams. **Resiliencia parcial (implementada):** `DownstreamConnectErrorGlobalFilter` traduce fallo de conexión al upstream en **502** Problem. **CORS** explícito y **correlación** `X-Correlation-Id` (normalización, reenvío al upstream y Problem): **implementados** — ver §5–6.

## 3. Propagación hacia microservicios (decisión MVP)

**Modo por defecto (MVP / desarrollo): token relay**

- Tras validar el JWT, el gateway **reenvía** la misma cabecera `Authorization: Bearer` al microservicio de destino.
- Cada microservicio **revalida** el JWT con el mismo `issuer-uri` (misma clave pública / JWKS). Así se mantiene defensa en profundidad si algún servicio fuera alcanzable sin pasar por el gateway.
- **Autorización de negocio** (p. ej. comprobar `COLABORADOR` vs `ADMIN`, propiedad del árbol): se implementa en el servicio leyendo claims del token ya validado.

**Mapeo en `catalog-service` (OAuth2 Resource Server):** el servicio revalida el JWT con el mismo `issuer-uri` que el gateway y convierte **`realm_access.roles`** en `GrantedAuthority` con prefijo **`ROLE_`** (p. ej. `COLABORADOR` → `ROLE_COLABORADOR`) mediante un `JwtAuthenticationConverter` dedicado, alineado con `hasRole("COLABORADOR")` / `hasRole("ADMIN")` en Spring Security.

**Alternativa (solo con red de confianza en producción): cabeceras internas**

- El gateway valida el JWT y **no** reenvía el Bearer; inyecta cabeceras internas (`X-User-Sub`, roles) y un secreto compartido o política de red que impida saltarse el gateway.
- Los microservicios **no** ejecutan resource server JWT; confían en esas cabeceras solo para tráfico interno. Requiere **aislamiento de red** (p. ej. NetworkPolicy en Kubernetes) para no ser explotable.

El código del gateway sigue el **modo token relay** por defecto; cualquier cambio (p. ej. solo cabeceras internas) requiere decisión explícita en ADR o en este fichero.

## 4. Front (Vue)

- Usuarios de prueba del realm (solo desarrollo, ver [infra/compose/README.md](../../infra/compose/README.md)): `colaborador` / `colaborador_dev` (rol `COLABORADOR`), `admin_mtl` / `admin_mtl_dev` (roles `ADMIN` y `COLABORADOR`).
- Variables de entorno `VITE_*` en Vue ([frontend/.env.example](../../frontend/.env.example)): **`VITE_OIDC_ISSUER`** (URL **completa** del issuer, p. ej. `http://localhost:8180/realms/mtl` si `KEYCLOAK_PORT=8180`), **`VITE_OIDC_CLIENT_ID`** (`mtl-spa`), **`VITE_OIDC_SCOPE`** (`openid profile email`).
- **Redirect URIs** y **Web origins** deben coincidir con el origen real de la SPA (`http://localhost:5173` con Vite en host; `http://localhost:8088` con overlay Compose apps; ver [infra/compose/README.md](../../infra/compose/README.md)).
- Usar el **access token** como Bearer hacia el gateway; no usar el ID token como sustituto del access token para la API REST.

## 5. Back (Spring Boot)

- **Gateway y microservicios** con JWT: `spring.security.oauth2.resourceserver.jwt.issuer-uri` debe coincidir con el claim **`iss`** del access token que emite Keycloak al navegador (realm **`mtl`**).
  - **Servicios en el host (Maven, perfil `dev`):** `MTL_JWT_ISSUER_URI=http://localhost:8180/realms/mtl` (ajustar host/puerto si cambia `KEYCLOAK_PORT` en Compose; Spring **no** enlaza `KEYCLOAK_PORT` automáticamente con `issuer-uri`).
  - **Servicios en contenedor** ([`docker-compose.apps.yml`](../../infra/compose/docker-compose.apps.yml), ancla `x-backend-jwt`): mantener el mismo `iss` que ve el navegador, `MTL_JWT_ISSUER_URI=http://localhost:${KEYCLOAK_PORT:-8180}/realms/mtl`, y obtener JWKS desde el contenedor con `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=http://host.docker.internal:${KEYCLOAK_PORT:-8180}/realms/mtl/protocol/openid-connect/certs`.
  - **No** usar `http://keycloak:8080/realms/mtl` como `issuer-uri` en el stack `mtl` salvo reconfigurar Keycloak y la SPA para que el token lleve ese mismo `iss` (no es el caso del MVP local documentado en [infra/compose/README.md](../../infra/compose/README.md)).
- El `iss` del token y `issuer-uri` deben ser idénticos; la URL de JWKS puede diferir si el runtime no alcanza el host publicado del IdP (p. ej. `host.docker.internal` desde contenedor).
- **CORS (implementado en gateway):** política explícita para `/api/**` en `services/api-gateway/src/main/resources/application.yml`: orígenes `http://localhost:5173`, `http://127.0.0.1:5173` (Vite en host), `http://localhost:8088` y `http://127.0.0.1:8088` (SPA en [docker-compose.apps.yml](../../infra/compose/docker-compose.apps.yml)); métodos `GET, POST, PUT, PATCH, DELETE, OPTIONS`; cabeceras `Authorization, Content-Type, Accept, X-Correlation-Id`; `allowCredentials=false`.

## 6. Correlación y logs

- **`CorrelationIdWebFilter`** en el gateway: lee o genera `X-Correlation-Id`, lo fija en la petición reenviada al upstream (proxy), en la cabecera de respuesta y en atributos del exchange para respuestas Problem (`correlationId`).
- Los microservicios MVC (`catalog-service`, `media-service`, `notification-service`, `ai-assistant-service`) leen la misma cabecera en **`CorrelationIdFilter`** (MDC `correlationId` para logs y Problem).
- No registrar tokens ni PII en logs ([logging.mdc](../../.cursor/rules/logging.mdc)).
