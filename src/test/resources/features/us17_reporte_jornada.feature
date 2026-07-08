# language: es
Característica: US17 - Reporte de Jornada
  Como conductor autenticado
  Quiero iniciar y finalizar mi jornada
  Para que el sistema registre tiempos operativos confiables

  Escenario: Iniciar jornada
    DADO conductor autenticado
    CUANDO presiona Iniciar jornada
    ENTONCES registra la hora de inicio

  Escenario: Finalizar jornada
    DADO una jornada iniciada
    CUANDO presiona Finalizar jornada
    ENTONCES registra hora de cierre y duración

  Escenario: Evitar cierre inválido
    DADO que no existe jornada iniciada
    CUANDO intenta finalizar jornada
    ENTONCES bloquea la acción e informa el motivo
