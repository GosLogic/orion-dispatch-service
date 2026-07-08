# language: es
Característica: US15 - Asignación Dinámica
  Como gestor de operaciones
  Quiero asignar y reasignar despachos de forma dinámica
  Para mantener continuidad operativa y evitar conflictos de recursos

  Escenario: Asignar despacho válido
    DADO conductores y vehículos disponibles
    CUANDO se registra una nueva asignación de despacho
    ENTONCES el sistema registra el despacho exitosamente

  Escenario: Bloquear conductor no disponible
    DADO conductor fuera de turno
    CUANDO se intenta asignar el despacho al conductor
    ENTONCES el sistema impide la asignación

  Escenario: Detectar conflicto de recurso
    DADO un vehículo ocupado
    CUANDO se intenta usar el mismo vehículo en otro despacho simultáneo
    ENTONCES el sistema muestra conflicto de solapamiento

  Escenario: Reasignar recurso
    DADO un despacho previamente asignado
    CUANDO el gestor cambia conductor o unidad
    ENTONCES actualiza el despacho

  Escenario: Mantener trazabilidad
    DADO un cambio de asignación realizado
    CUANDO el sistema persiste la modificación
    ENTONCES el sistema registra usuario, fecha y motivo del cambio
