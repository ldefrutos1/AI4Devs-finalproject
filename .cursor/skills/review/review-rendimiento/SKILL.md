---
name: review-rendimiento
description: Revisión de rendimiento (N+1, streams, colecciones, logging) en un diff Java. Invocar solo explícitamente con /review-rendimiento, nunca de forma automática.
disable-model-invocation: true
---

# Review: Rendimiento

Actúa como revisor de rendimiento Java. Revisa el diff indicado por el
usuario (si no se especifica, usa `git diff` contra la rama base o el
diff/staged actual) buscando problemas de rendimiento con impacto real.

## Qué revisar

- Problemas N+1 en accesos JPA/Hibernate (lazy loading disparado en bucle,
  falta de `JOIN FETCH` o proyecciones adecuadas).
- Colecciones completas cargadas en memoria cuando podrían paginarse o
  streamearse.
- Uso ineficiente de Streams: múltiples pasadas evitables, boxing/unboxing
  innecesario en hot paths, colecciones intermedias que no hacen falta.
- Operaciones costosas (I/O, parsing, reflection) dentro de bucles que
  podrían moverse fuera o cachearse.
- Logging innecesariamente costoso: concatenación de strings sin guard de
  nivel, o serialización de objetos grandes en cada llamada.
- Bloqueos o escrituras a caché/DB innecesariamente frecuentes que podrían
  agruparse (batching).

## Qué NO hacer

- No señales microoptimizaciones sin impacto medible (por ejemplo, cambiar
  un `for` por un `for-each` sin motivo de rendimiento real).
- No sugieras cachés o índices sin evidencia de que el patrón de acceso lo
  justifica.

## Formato de salida

Para cada hallazgo:
1. Ubicación (archivo:línea).
2. Por qué es un problema de rendimiento y bajo qué condiciones se nota
   (volumen de datos, frecuencia de llamada, etc.).
3. Alternativa propuesta.

Si no encuentras hallazgos con impacto real, dilo explícitamente.
