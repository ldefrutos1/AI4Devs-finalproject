# Frontend SPA (Vue 3)

Bootstrap inicial del frontend para MyTreeLibrary:

- Vue 3 + TypeScript + Vite
- Vue Router (ruta protegida para alta de ficha)
- OIDC real con Keycloak (Authorization Code + PKCE, cliente `mtl-spa`)
- Cliente HTTP base para consumir el gateway con Bearer
- Internacionalización con `vue-i18n` (locale base: `es`)

## Variables de entorno

Copiar `.env.example` a `.env` y ajustar si cambias puertos locales.

## Arranque local

```bash
npm install
npm run dev
```

El entorno de desarrollo usa proxy de Vite para evitar problemas CORS:

- `/api/*` -> `http://localhost:8080/*`
- Con `VITE_GATEWAY_BASE_URL` vacío (o sin definir), las llamadas van a rutas relativas `/api/...` y el proxy aplica; no hace falta rellenar la variable para desarrollo local típico.
- Si no usas proxy (u otro host), define `VITE_GATEWAY_BASE_URL=http://localhost:8080` (u otra URL absoluta) y configura CORS en gateway.

## Textos e internacionalización

- Archivos de propiedades en `src/i18n/locales/` (por ahora `es.ts`).
- Evitar hardcodear textos nuevos en vistas/composables; usar claves `t('...')`.
- Copy de producto puede decir «árbol/ficha»; identificadores técnicos y rutas usan _ejemplar_ ([ADR-0006](../docs/adr/0006-ejemplar-aggregate-http-kafka-naming.md)).

## Flujo de autenticación

- Ruta protegida: `/ejemplares/new`
- Callback OIDC: `/auth/callback`
- Login redirige a Keycloak con `scope=openid profile email`

## Patrón recomendado para HU-007/HU-008 (requests cancelables)

Para vistas con filtros, búsquedas o navegación rápida, usar el composable base `src/composables/useAbortableRequest.ts`:

- abortar la request previa antes de lanzar una nueva;
- pasar `signal` hasta `services/*` y `apiFetch`;
- ignorar `AbortError` en UI (no mostrar como error funcional).

Ejemplo de uso:

```ts
const { runWithAbort, isAbortError } = useAbortableRequest()

async function loadItems() {
  try {
    const data = await runWithAbort((signal) => listItems({ q: query.value }, signal))
    items.value = data
  } catch (error) {
    if (isAbortError(error)) return
    uiError.value = mapApiError(error)
  }
}
```

## Mapa en la pantalla de alta (OpenStreetMap + Leaflet)

- Vista previa con teselas gratuitas de [OpenStreetMap](https://www.openstreetmap.org/); **doble clic** en el mapa rellena lat/lng (sin editar geometría arrastrando).
- Debes mostrar la **atribución** a los colaboradores de OSM (ya aparece en el mapa y en el texto bajo el mapa).
- El uso masivo de teselas públicas de `tile.openstreetmap.org` puede chocar con la [política de uso](https://operations.osmfoundation.org/policies/tiles/); en producción a alto tráfico conviene un proveedor de mapas propio o de terceros con acuerdo comercial.

## Verificación manual E2E (TASK-HU-005-10)

1. Levantar infraestructura (`infra/compose`) y servicios backend (`api-gateway` + `catalog-service`) en perfil `dev`.
2. En `frontend/`, ejecutar `npm run dev`.
3. Abrir `http://localhost:5173/ejemplares/new`; la app redirige a Keycloak.
4. Iniciar sesión con usuario de desarrollo:
   - usuario: `colaborador`
   - contraseña: `colaborador_dev`
5. Verificar que cargan especies y provincias, que latitud y longitud empiezan vacías, que el mapa inicia centrado en 40,4063 / -3,65588 sin marcador, que un doble clic en el mapa rellena coordenadas y muestra el marcador, y completar el formulario y enviar.
6. Resultado esperado: mensaje de éxito con `treeId` y respuesta `201` en la llamada `POST /api/catalog/trees`.

Notas:

- Si el token caduca, ante `401` la pantalla redirige automáticamente al login.
- Los mensajes de validación de `400` se muestran de forma legible en la UI.

## Verificación manual E2E (TASK-HU-008-16, HU-008)

Prerrequisitos: infra Compose (Postgres, Keycloak, MinIO), **api-gateway** **8080**, **catalog-service** **8081**, **media-service** **8082** en perfil `dev`; `mtl.media.base-url` en catálogo apuntando a media (véase [services/README.md](../services/README.md) apartado HU-008). En `frontend/`: `npm run dev`.

Usuario de prueba (Keycloak): `colaborador` / `colaborador_dev` (rol **COLABORADOR**); para filtro por creador y edición ajena, usuario **ADMIN** si está configurado.

1. **Listado y filtros (escenarios BDD 6–7):** abrir `http://localhost:5173/mis-ejemplares`. Comprobar paginación, filtro por especie y rango de fechas de creación. Como **COLABORADOR**, solo deben aparecer fichas propias. Como **ADMIN**, probar filtro por usuario creador si el selector está disponible.
2. **Edición (escenarios 1–3):** desde el listado, abrir una ficha propia (`/ejemplares/:id/edit`). Modificar campos válidos y **Guardar** → **PUT** `200` y datos coherentes al recargar. Intentar guardar sin especie/coordenadas válidas → **400** con Problem legible. Con otra ficha ajena (otro colaborador), **PUT** debe fallar con **403** (o no permitir abrir edición).
3. **Galería en edición (HU-006-14):** en la misma pantalla, añadir una foto (**+**) y eliminar una con confirmación; comprobar que la galería se actualiza sin errores.
4. **Baja con fotos (escenarios 4–5):** en una ficha propia **con** fotos, **Eliminar árbol** → confirmar. Resultado: **204** en `DELETE /api/catalog/trees/{treeId}`; el ejemplar desaparece del listado; fotos ya no listables para ese `treeId`. Para comprobar aborto por media: parar **media-service** y repetir → el ejemplar debe **seguir** en listado y el cliente mostrar error (p. ej. **502**).
5. **Baja sin fotos:** repetir en ficha sin fotografías → baja correcta sin depender de objetos en MinIO.
6. **Sin notificación por edición/baja (R7):** tras **PUT** o **DELETE**, no debe generarse correo de “nuevo árbol” a suscriptores (solo el alta dispara **UC-09**).

Checks automáticos previos: [devsecops-ci.md](../docs/engineering/devsecops-ci.md) (paridad CI). Opcional en local: `npm run build`; backend acotado `mvn -f services/pom.xml -pl catalog-service,media-service test`.

## Enriquecimiento Mongo (HU-015)

Bloques de UI para `especie_detalle` (popup) y `ejemplar_detalle` (panel colapsable). Contrato HTTP: [openapi.yaml](../docs/api/openapi.yaml); modelo: [mongo.md](../docs/data-model/mongo.md).

### Piezas principales

| Capa | Ficheros |
|------|----------|
| Servicio API | `src/services/catalog/enrichmentService.ts`, `enrichmentErrors.ts` |
| Composables | `useTreeFormEnrichment` (alta/edición autenticada), `usePublicTreeEnrichment` (detalle público), `enrichmentFormDraft`, `enrichmentGuidedForms`, `enrichmentSummaries` |
| Componentes | `SpeciesEnrichmentPopup.vue`, `TreeEnrichmentPanel.vue`, editores guiados (`HealthStatusFieldEditor`, `SpeciesEcologicalFieldEditor`, `SpeciesReferencesFieldEditor`) |
| Estilos | `src/styles/enrichment.css` (importado en `style.css`) |
| i18n | claves bajo `enrichment.*` en `src/i18n/locales/es.ts` |

### Superficies de UI

- **Alta** (`/ejemplares/new`): icono de especie con popup (lectura colaborador; edición **ADMIN**). Sección de ejemplar con mensaje `enrichment.tree.createUnavailable` (el enriquecimiento del ejemplar se edita tras crear la ficha).
- **Edición** (`/ejemplares/:id/edit`): popup de especie + panel colapsable de ejemplar; los cambios del panel se persisten al pulsar **Guardar ficha** (mismo flujo que el formulario SQL). Aviso no bloqueante si la respuesta trae `enrichmentWarning` tras guardar SQL.
- **Detalle público** (`/ejemplares/:id` o ruta pública equivalente): popup y panel en **solo lectura**; carga con `GET /api/catalog/public/trees/{treeId}/enrichment`. Si no hay documento Mongo del ejemplar, el panel expandido muestra solo el aviso vacío (sin campos vacíos).

### Verificación manual E2E (TASK-HU-015-14)

Prerrequisitos: Compose con **MongoDB** (`27017`), Postgres, Keycloak; **catalog-service** en perfil `dev` con `mtl.catalog.mongo.enabled=true`; **api-gateway** **8080**. En `frontend/`: `npm run dev`.

Usuarios de prueba: `colaborador` / `colaborador_dev` (**COLABORADOR**); usuario **ADMIN** si está configurado en Keycloak.

1. **Escenario 1 — Colaborador edita ejemplar (BDD §2):** crear o editar una ficha propia. Tras guardar SQL, expandir **Información ampliada del ejemplar**, rellenar medidas/etiquetas/observaciones y guardar ficha. Comprobar `PUT /api/catalog/trees/{treeId}/enrichment` **200** y datos al recargar. El popup de especie debe ser solo lectura para colaborador.
2. **Escenario 2 — ADMIN edita especie:** como **ADMIN**, abrir el icono junto al selector de especie, modificar sinónimos o datos ecológicos y **Guardar** en el popup. Comprobar `PUT /api/catalog/species/{speciesId}/enrichment` **200**. No debe aparecer acción de IA.
3. **Escenario 3 — Detalle público:** abrir una ficha **publicada** con enriquecimiento (sin login). Ver popup de especie y panel de ejemplar en solo lectura. El listado público no debe mostrar estos bloques.
4. **Escenario 4 — Aviso Mongo post-SQL:** con Mongo detenido o URI incorrecta, guardar una ficha SQL válida. La ficha debe persistir (**200**/**201** en `POST`/`PUT` trees) y la UI mostrar aviso (`enrichmentWarning` o copy de proyección Mongo). Tras restaurar Mongo, editar el panel de ejemplar y guardar debe completar el enriquecimiento.
5. **Escenario 5 — Borrado cascada:** en ficha con datos en Mongo, **Eliminar árbol** (**HU-008**). Tras **204**, el documento `ejemplar_detalle` no debe existir; `especie_detalle` permanece si la especie sigue en catálogo.
6. **Escenario 7 — Validación compartida:** enviar `measurements` con valor no numérico en el panel → **400** Problem legible; colaborador sobre ficha ajena sigue con **403** (**HU-008**).

Checks automáticos previos: [devsecops-ci.md](../docs/engineering/devsecops-ci.md) (paridad CI). Opcional en local: `npx vitest run enrichment`; backend `mvn -f services/pom.xml -pl catalog-service verify` (IT Mongo si Docker disponible).
