# Modelo de casos de uso — MyTreeLibrary

Documento de análisis alineado con la descripción del producto en [readme.md](../../readme.md) y con las reglas de negocio acordadas.

## Diagrama (PlantUML)

Fichero fuente: [use-case-model.puml](use-case-model.puml).

Para visualizarlo: extensión PlantUML en el IDE, [plantuml.com](https://www.plantuml.com/plantuml) o CLI `plantuml use-case-model.puml`.

## Actores


| Actor                       | Descripción                                                                                                                       |
| --------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| **Público**                 | Visitante sin sesión en la plataforma.                                                                                            |
| **Colaborador**             | Usuario autenticado que participa en el catálogo de árboles. Generaliza a Público. |
| **ADMIN**                   | Usuario autenticado con permisos de gestión. Generaliza a Colaborador.                                                            |
| **Sistema (MyTreeLibrary)** | Ejecución automática interna (p. ej. envío de correos tras eventos de negocio).                                                   |
| **Proveedor de IA**         | Sistema externo: consulta orientativa de características de especie (MVP); identificación por imagen y chat (futuro).                                                                               |
| **Servidor SMTP**           | Sistema externo para entrega de correo.                                                                                           |


## Resumen de casos de uso


| ID    | Nombre                                              | Actor principal | Autenticación | Objetivo / resultado                                                                                                                                                                                                       |
| ----- | --------------------------------------------------- | --------------- | ------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| UC-01 | Consultar árboles publicados y mapa                 | Público         | No            | Visualizar fichas y localización de ejemplares públicos.                                                                                                                                                                   |
| UC-02 | Registrarse para recibir notificaciones (e-mail)    | Público         | No            | Alta de suscripción por correo (sin cuenta de Colaborador); en el **MVP** la suscripción queda **ACTIVA** al validar el correo, sin confirmación por e-mail. |
| UC-03 | Registrar árbol                                     | Colaborador     | **Sí**        | Crear ficha con datos, fotos y ubicación; opcionalmente publicar para consulta pública.                                                                                                                                    |
| UC-04 | Modificar y eliminar árboles del colaborador        | Colaborador     | **Sí**        | Modificación y eliminación de ejemplares: **COLABORADOR** solo los propios; **ADMIN**.                                                                                                                                    |
| UC-05 | Identificar árbol asistido por IA (imagen)          | Colaborador     | **Sí**        | Extensión opcional en UC-03 / UC-04: sugerencia orientativa de especie a partir de imagen.                                                                                               |
| UC-06 | Consultar asistente IA (chat)                       | Colaborador     | **Sí**        | Interacción conversacional con el asistente.                                                                                                                                                                               |
| UC-07 | Gestionar maestros taxonómicos | ADMIN           | **Sí**        | Administrar **familia**, **género** y **especie** que alimentan el catálogo.                                      |
| UC-08 | Gestionar solicitudes de notificación               | ADMIN           | **Sí**        | Cambiar el estado de la suscripción entre **ACTIVA** y **CANCELADA** (baja lógica); **no** se eliminan filas de suscriptor en el MVP.                                |
| UC-09 | Notificar por correo a suscriptores                 | Sistema         | N/A           | Tras la **alta** (creación) de ficha de árbol, informar por e-mail a suscriptores activos. En el MVP **no** se notifica por modificaciones posteriores.                                                         |
| UC-10 | Consultar asistente IA características especie | ADMIN           | **Sí**        | Obtener datos orientativos de enriquecimiento de especie desde el proveedor IA (hábitat, distribución, referencias, etc.) para apoyar la gestión de maestros; la respuesta no sustituye criterio experto.                                      |

## Relaciones UML aplicadas


| Relación                      | Uso en el modelo                                                                                                                                                                                                           |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Generalización de actores** | Colaborador → Público; ADMIN → Colaborador (el especializado hereda el comportamiento del general).                                                                                                                        |
| **«extend»**                  | UC-05 extiende UC-03 y UC-04 en puntos de extensión donde el usuario aporta o revisa imagen para identificación asistida.                                                        |
| **«include»**                 | UC-03 incluye UC-09: tras el Alta de ejemplar se dispara el proceso de notificación (puede ser no-op si no hay destinatarios). UC-04 **no** incluye UC-09 en el MVP (las modificaciones no generan correo a suscriptores). |


## Reglas y supuestos explícitos

1. **Autenticación:** UC-03 … UC-08 y **UC-10** requieren usuario autenticado (Keycloak / JWT en la arquitectura prevista). UC-01 y UC-02 son anónimos. Para **UC-02**, el endpoint de alta **no** incorpora en el MVP rate limiting ni captcha; el riesgo queda explícito en [data-model.md](../data-model/data-model.md) §2.
2. **UC-09 y modificaciones:** en el MVP **solo el alta** (UC-03) dispara notificación a suscriptores; las ediciones y bajas (UC-04) **no** lo hacen (regla R7 en [data-model.md](../data-model/data-model.md)). Implementación UC-04: [HU-008](../backlog/HU-008-edicion-de-mis-arboles.md) (**Cerrada**).
3. **UC-07 “Gestionar maestros taxonómicos”:** en el MVP son los maestros **taxonómicos** (**familia**, **género**, **especie**) gestionados por **ADMIN**; no incluye mantenimiento de **provincias** (semillas Flyway). No confundir con el CRUD de árboles del colaborador (UC-03 / UC-04). La consulta IA de enriquecimiento de especie (**UC-10**) se activa en ese contexto de maestros, no sustituye el CRUD de UC-07.
4. **UC-05 y UC-06:** modelados en el diagrama; implementación fuera del MVP (**HU-009**, **HU-010** — próxima versión en [backlog.md](../backlog/backlog.md) §3).

