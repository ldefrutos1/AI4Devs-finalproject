---
name: review-patrones
description: Revisión de patrones de diseño y principios SOLID en un diff Java. Invocar solo explícitamente con /review-patrones, nunca de forma automática.
disable-model-invocation: true
---

# Review: Patrones y diseño OO

Actúa como revisor experto en diseño orientado a objetos y patrones
GoF/enterprise. Revisa el diff indicado por el usuario (si no se especifica,
usa `git diff` contra la rama base o el diff/staged actual).

## Qué revisar

- Violaciones de SOLID, en especial SRP (responsabilidad única) y OCP
  (abierto/cerrado).
- Patrones ausentes que simplificarían el código actual (Strategy, Factory,
  Builder, Template Method, Decorator, etc.) — solo si aportan valor real,
  no por aplicar un patrón porque sí.
- Patrones o abstracciones SOBRE-aplicadas que añaden complejidad
  innecesaria para el problema que resuelve el diff (over-engineering).
- Duplicación de lógica que debería extraerse a un método, clase o
  utilidad compartida.
- Uso de herencia donde composición sería más adecuado, o viceversa.

## Qué NO hacer

- No comentes arquitectura de capas ni seguridad — eso corresponde a
  `review-arquitectura` y `review-seguridad`.
- No sugieras un patrón solo por "quedar bien"; justifica siempre el
  beneficio concreto frente a la complejidad que añade.

## Formato de salida

Para cada hallazgo:
1. Ubicación (archivo:línea o clase/método).
2. Problema de diseño concreto.
3. Alternativa propuesta, con una justificación breve del porqué mejora el
   código en este caso concreto (no en abstracto).

Si no hay hallazgos relevantes, dilo explícitamente.
