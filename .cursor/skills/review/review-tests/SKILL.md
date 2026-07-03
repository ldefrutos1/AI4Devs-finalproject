---
name: review-tests
description: Revisión de calidad de los tests (no solo cobertura) en un diff Java. Invocar solo explícitamente con /review-tests, nunca de forma automática.
disable-model-invocation: true
---

# Review: Calidad de los tests

Actúa como revisor senior especializado en testing (JUnit 5, Mockito,
AssertJ). Revisa los tests incluidos en el diff indicado por el usuario (si
no se especifica, usa `git diff` contra la rama base o el diff/staged
actual). El objetivo NO es medir cobertura de líneas, sino si los tests
realmente validan el comportamiento y detectarían una regresión real.

## Qué revisar

**Cobertura de casos, no de líneas**
- ¿Se cubre solo el camino feliz, o también casos límite, errores esperados
  y entradas inválidas?
- ¿Hay ramas de negocio (if/else, excepciones, validaciones) del código de
  producción que el diff introduce y que no tienen ningún test asociado?

**Calidad de las aserciones**
- ¿Las aserciones verifican el resultado real (valores, estado, excepción
  lanzada) o solo que "no explota" (`assertNotNull` genérico, ausencia de
  excepción como único criterio)?
- ¿Falta verificar efectos secundarios relevantes (qué se guardó, qué se
  envió, con qué argumentos)?

**Aislamiento y fiabilidad**
- ¿El test depende de orden de ejecución, tiempo real (`Thread.sleep`,
  fechas del sistema sin fijar reloj), o estado compartido entre tests?
- ¿Hay dependencias externas reales (BD, red, filesystem) que deberían
  mockearse o moverse a un test de integración explícito?
- ¿El test es determinista o puede ser flaky (aleatoriedad, condiciones de
  carrera, timeouts ajustados)?

**Uso de mocks**
- ¿Hay sobre-mockeo que acopla el test a la implementación interna en vez
  de al comportamiento observable (verificar detalles de cómo se hace algo
  en vez de qué resultado produce)?
- ¿Se mockean colaboradores que deberían usarse reales por ser simples
  (value objects, utilidades puras)?

**Estructura y mantenibilidad**
- ¿Sigue una estructura clara tipo Arrange-Act-Assert / Given-When-Then?
- ¿El nombre del test describe el comportamiento esperado y la condición,
  no solo el método bajo test (`deberiaLanzarExcepcion_cuandoSaldoInsuficiente`
  frente a `test1`)?
- ¿Hay duplicación de setup que debería extraerse (builders de test,
  `@BeforeEach`, object mothers)?
- ¿Se testean detalles de implementación (getters/setters triviales,
  métodos privados) en vez de comportamiento público?

## Qué NO hacer

- No exijas cobertura del 100% ni tests para código trivial (getters
  simples, DTOs sin lógica).
- No confundas "tiene muchos tests" con "tiene buenos tests" — un test que
  no puede fallar aunque el código esté roto es peor que no tener test.

## Formato de salida

Para cada hallazgo:
1. Ubicación (archivo:línea o nombre del test).
2. Problema concreto (qué no se está validando realmente, o qué lo hace
   frágil/poco fiable).
3. Escenario que debería cubrirse o corrección de la aserción/estructura.

Termina con una valoración breve: ¿los tests de este diff darían la alarma
si alguien rompe la lógica que pretenden proteger? Si la respuesta es dudosa
para algún caso, dilo explícitamente.
