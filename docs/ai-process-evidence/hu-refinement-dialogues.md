# Diálogos de refinamiento (evidencia)

Registro histórico de prompts de refinamiento con [hu-refinement-mtl](../../.cursor/skills/hu-refinement-mtl/SKILL.md). Índice: [README.md](README.md).

---

## HU-008 — Edición y baja de mis árboles

> *Registro histórico:* los prompts siguientes reproducen el diálogo de refinamiento de HU-008 tal como ocurrió; pueden reflejar decisiones intermedias que no coinciden con el diseño final. Fuente de verdad: [HU-008-edicion-de-mis-arboles.md](../backlog/HU-008-edicion-de-mis-arboles.md) y [HU-008-ticket-breakdown.md](../backlog/HU-008-ticket-breakdown.md).

**Prompt 1:**

Vamos a desarrollar la historia HU-008@.cursor/skills/hu-refinement-mtl

**Prompt 2:**

Vamos a revisar los puntos que quedan fuera de la historia. 
- 1.- Añade en HU-006 el ticket para incluir la posibilidad de añadir y borrar fotografías desde la pantalla de edición de Mis árboles 
- 2.- Incluye en el Backlog una nueva historia para abordar Proyección o enriquecimiento Mongo 3.- Incluye en la historia que estamos abordando la posibilidad de borrar árboles

**Prompt 3:**

El borrado será físico; cuando se borre un árbol se deben eliminar sus fotografías y su ampliación en Mongo; dado que aún no tenemos implementado en Mongo se necesitará un ticket solo para implementar esta acción que por ahora quedará como pendiente. Revisa si con estas aclaraciones podemos cerrar este punto y abordar los dos siguientes que serán Riesgos y Aclaraciones pendientes

**Prompt 4:**

Respecto al riesgo de Listado sin filtros vamos a añadir en la historia el filtro por especie y por fecha de creación: desde - hasta. Para el borrado de todas las fotos de un árbol vamos a incluir un nuevo endpoint en media-service que será consumido por el microservicio de catálogo; déjalo anotado en esta historia como ticket que debe modificar los dos microservicios. Ante un fallo parcial del borrado en cascada se producirá un Rollback (no se usará un patrón sagas en el MVP). Dime si queda algo pendiente antes de abordar el desglose de la historia en ticket, no abordes este desglose hasta que yo te lo diga

**Prompt 5:**

- 1.- Path de borrado: `DELETE /api/media/trees/{treeId}/photos` 
- 2.- Si un ejemplar tiene fotografías primero se invoca al servicio de borrado de todas las fotografías; si el servicio da error se para el proceso; si se han borrado todas las fotografías se elimina el ejemplar en PostgreSQL 3.- Fechas en formato date a ser posible en UTC 4.- Para ADMIN se añade un filtro más para poder seleccionar los ejemplares dados de alta por un usuario determinado

---

## HU-016 — Consulta de características de especie (ADMIN, MVP)

> *Registro histórico:* los prompts siguientes reproducen el diálogo de refinamiento de HU-016 tal como ocurrió; pueden reflejar decisiones intermedias que no coinciden con el diseño final. Fuente de verdad: [HU-016-consulta-admin-caracteristicas-especie-ia.md](../backlog/HU-016-consulta-admin-caracteristicas-especie-ia.md) y [HU-016-ticket-breakdown.md](../backlog/HU-016-ticket-breakdown.md).

**Prompt 1:**

/hu-refinement-mtl HU-016

**Prompt 2:**

respecto a las incoherencias detectadas: 
- 1.- usuario administrador y ADMIN son terminos equivalentes 
- 2.- las historias HU-009 y HU-010 están planificaas para versiones futudas, ver backlog, revisar texto de readme para comprobar que es coherente con lo indicado 
- 3.- la historia no aplica a la persistencia de Mongo, solo a la consulta de datos a la IA. El funcionamiento será permitir desde la pantalla de alta actual una consulta a la IA que cargará los campos de dicha pantalla. Queda fuera ded esta historia la persistencia con Mongo 
- 4.- El API a incluir es exclusivamente la consulta a la IA de datos de una especie: como entrada se pasará el nombre comun y cientifico de la especie, como respuesta se espera un json que se pueda precargar en la pantalla ya existente de edición de estos datos. Por tanto es importante fijar el formato de lo que debe devolver el LLM: datos que se puedan cargar en la pantalla de edición de caracteristicas de la especie. La historia por tanto solo invoca al servicio ai-assistant-service para obtener el JSON de enrequecimiento de especie y carga los datos en la pantalla ya existente. La funcionalidad solo estará activa si aún no hay datos de enriquecimiento en Mongo. Dime si tienes alguna duda más

**Prompt 3:**

el contrado exacto JSON se cerrará al implementar los ticker, por ahora simplemente ten en cuenta que se deba añadir un ticket al respecto al hacer el breakdown de la historia. Dime si está todo claro para pasar al breakdown en ticket de trabajo
