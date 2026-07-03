---
name: review-bugs
description: Caza de bugs y edge cases de corrección funcional en un diff Java. Invocar solo explícitamente con /review-bugs, nunca de forma automática.
disable-model-invocation: true
---

# Review: Caza de bugs y edge cases

Ignora arquitectura, estilo y seguridad. Actúa como revisor centrado
ÚNICAMENTE en corrección funcional. Revisa el diff indicado por el usuario
(si no se especifica, usa `git diff` contra la rama base o el diff/staged
actual).

## Qué revisar

- `NullPointerException` potenciales, `Optional` mal usado, o comparaciones
  con `equals`/`==` mal aplicadas.
- Condiciones de carrera o estado mutable compartido sin sincronización
  adecuada.
- Fugas de recursos: streams, conexiones, ficheros no cerrados con
  try-with-resources.
- Manejo de excepciones que traga errores silenciosamente, pierde la causa
  original (`catch` sin `throw`/`cause`), o captura excepciones demasiado
  genéricas.
- Límites de transacción (`@Transactional`) mal ubicados, propagación
  incorrecta, o efectos secundarios fuera de la transacción esperada.
- Errores off-by-one, condiciones de borde en bucles, colecciones vacías o
  con un único elemento.
- Comparaciones de fechas, `BigDecimal` o enums mal hechas (p. ej.
  `BigDecimal.equals` en vez de `compareTo`).
- Casos límite no cubiertos por los tests existentes en el diff.

## Formato de salida

Para cada bug:
1. Ubicación (archivo:línea).
2. Escenario exacto que lo dispara (input o secuencia concreta).
3. Impacto (qué falla y cómo se manifiesta).
4. Fix propuesto.

Si no encuentras bugs, dilo explícitamente — no inventes problemas para
justificar la revisión.
