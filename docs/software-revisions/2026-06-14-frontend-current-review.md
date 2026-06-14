# Revision frontend Vue

| Campo | Valor |
|-------|-------|
| **Fecha** | 2026-06-14 |
| **Alcance** | `frontend/` (Vue 3 + Vite + TypeScript) |
| **Tipo** | Evaluacion tecnica de arquitectura, calidad, UX, seguridad y mantenibilidad |
| **Revisor** | Asistente IA (perfil: experto frontend Vue) |
| **Estado** | Borrador |

---

## Resumen ejecutivo

El frontend presenta una base solida para un MVP: Vue 3, TypeScript, Vite, Pinia, Vue Router, vue-i18n, OIDC, servicios HTTP tipados y una cobertura de tests razonable. La arquitectura esta bien separada por responsabilidades (`views`, `components`, `composables`, `services`, `stores`, `types`, `router`, `i18n`) y cumple en general las reglas del proyecto para frontend.

La mejora reciente de lazy loading en `frontend/src/router/index.ts` reduce el coste inicial del bundle al cargar las vistas bajo demanda. El build de produccion (`npm run build`) se ha ejecutado correctamente y confirma la generacion de chunks separados para las vistas principales.

Los principales puntos de mejora son normales para un frontend MVP que empieza a crecer: algunos ficheros grandes, ausencia de pruebas e2e automatizadas en el flujo principal, margen de mejora en accesibilidad sistematica y futura revision de bundles cuando aumente el peso funcional.

**Nota general actual: 4,3 / 5**

---

## Calificacion por apartados

| Apartado | Nota | Evaluacion |
|----------|:----:|------------|
| **Arquitectura Vue** | **4,3** | Buena separacion por capas y estructura coherente. Las vistas consumen servicios/composables en lugar de concentrar toda la logica. |
| **TypeScript y tipado** | **4,2** | Uso solido de tipos de dominio, DTOs y errores. No se aprecia abuso real de `any`. |
| **Integracion HTTP/API** | **4,4** | `apiClient.ts` centraliza auth, errores `ProblemDetails`, blobs, llamadas publicas y reintento ante 401. Muy buen punto de consistencia. |
| **Seguridad frontend** | **4,2** | OIDC, guards por roles y silent refresh estan bien integrados. El uso de `localStorage` para OIDC es aceptable en MVP, aunque mejorable en escenarios mas estrictos. |
| **Estado y composables** | **4,1** | Buen uso de Pinia y composables. Algunos composables ya son grandes y deberian partirse si siguen creciendo. |
| **Componentizacion** | **4,0** | Base correcta. Hay vistas y componentes con bastante responsabilidad acumulada, especialmente en administracion y formularios de ejemplares. |
| **Testing frontend** | **4,3** | Hay 53 tests en `frontend/src`, cubriendo servicios, composables, router, vistas y componentes. Falta e2e/smoke automatizado para flujos criticos. |
| **UX y accesibilidad** | **4,0** | Hay buenas senales: `aria-label`, sidebar responsive, estados de auth y composables de accesibilidad. Falta una revision sistematica de foco, mensajes dinamicos y formularios criticos. |
| **i18n** | **4,1** | Copy centralizado con `vue-i18n`. Conviene dividir por dominios si sigue creciendo y verificar encoding visible en navegador. |
| **Performance y bundles** | **4,0** | El lazy loading de rutas ya esta aplicado. Proxima mejora: revisar bundles cuando crezca el frontend o aparezcan lentitudes reales. |
| **Mantenibilidad** | **4,1** | El proyecto es mantenible. El principal riesgo es acumulacion de logica en ficheros grandes. |
| **Experiencia de desarrollo** | **4,3** | Scripts claros: `build`, `typecheck`, `lint`, `format`, `test`. Stack moderno y consistente. |

---

## Fortalezas principales

### Cliente HTTP comun

El uso de `frontend/src/services/http/apiClient.ts` como punto central para llamadas autenticadas, publicas y descarga de blobs reduce duplicacion y evita que cada vista gestione auth, cabeceras y errores por su cuenta.

### Autenticacion y autorizacion

La combinacion de `oidc-client-ts`, Pinia, guards de router y roles de aplicacion ofrece un flujo suficientemente maduro para el MVP. La autorizacion real sigue dependiendo del backend, como debe ser.

### Separacion de responsabilidades

La presencia de `services`, `composables`, `stores`, `types` y `utils` evita que las vistas sean el unico lugar de logica. Esto facilita testear y evolucionar la aplicacion.

### Tests

El numero y distribucion de tests frontend es bueno para el estado actual: servicios, validaciones, composables, router, componentes y vistas tienen cobertura representativa.

### Lazy loading ya aplicado

Las vistas del router se cargan ahora con imports dinamicos. Esto evita arrastrar vistas pesadas al bundle inicial y mejora la base de performance para crecer.

---

## Riesgos y mejoras recomendadas

### 1. Dividir ficheros grandes

Prioridad media. Algunos ficheros superan el tamano recomendable para evolucionar comodos:

- `frontend/src/composables/useAdminTaxonomyMasters.ts`
- `frontend/src/views/AdminMastersView.vue`
- `frontend/src/views/EditTreeView.vue`
- `frontend/src/views/MyTreesListView.vue`

Recomendacion: partir solo cuando se toque funcionalmente cada zona, no hacer un refactor aislado sin necesidad.

### 2. Mejorar accesibilidad de forma incremental

Prioridad media. Acciones sencillas:

- foco visible comun con `:focus-visible`;
- `aria-live` o `role="alert"` en mensajes dinamicos;
- `aria-invalid` y `aria-describedby` en formularios criticos;
- revisar tamano tactil y contraste en botones secundarios, badges y errores.

### 3. Automatizar un smoke e2e minimo

Prioridad media. Bastaria con cubrir:

- carga de home/listado publico;
- login/callback simulado o flujo protegido mockeado;
- creacion/edicion basica de ejemplar;
- administracion basica si el rol es `ADMIN`.

No hace falta una suite e2e grande para el MVP.

### 4. Vigilar crecimiento del bundle

Prioridad baja tras aplicar lazy loading. El build actual genera chunks separados. La revision con visualizador de bundle puede esperar hasta que haya sintomas o aumente el peso de dependencias.

### 5. Dividir i18n por dominio

Prioridad baja/media. El fichero de traducciones centralizado funciona, pero podria crecer demasiado. Una particion por dominios (`navigation`, `treeForm`, `admin`, `subscriptions`) mejoraria mantenimiento.

---

## Verificacion realizada

- Revision de estructura de `frontend/src`.
- Revision de `package.json`, `router`, `apiClient`, OIDC, store de auth y configuracion Vite.
- Conteo de tests frontend: 53 ficheros `*.test.ts`.
- `npm run build`: correcto tras aplicar lazy loading de rutas.

---

## Conclusion

El frontend esta en buen estado para un MVP y tiene una base profesional. No parece una SPA improvisada: hay arquitectura, reglas, tipado, tests, servicios separados y autenticacion bien integrada.

La nota queda en **4,3 / 5**. Para subir hacia 4,6 o 4,7, los siguientes pasos deberian centrarse en accesibilidad sistematica, e2e minimo y reduccion progresiva de ficheros grandes.
