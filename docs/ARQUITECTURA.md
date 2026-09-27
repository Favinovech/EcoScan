# Arquitectura de EcoScan

## Capas

El proyecto sigue una arquitectura por capas inspirada en Clean Architecture, con MVVM
en la capa de presentación.

```
presentation  →  domain  ←  data
```

- **domain**: modelos (`WasteRecord`, `WasteCategory`), contratos de repositorio
  (`WasteRecordRepository`) y casos de uso (`GetWasteRecordsUseCase`,
  `GetTotalPointsUseCase`). Kotlin puro: no importa nada de Android, Room ni Retrofit.
- **data**: implementa los contratos de `domain` usando Room (`data/local`) y, a
  partir del Sprint 3, Retrofit (`data/remote`). Los `mapper` son el único lugar donde
  se convierte entre entidades/DTOs y modelos de dominio.
- **presentation**: una pantalla Compose, un `UiState` inmutable y un `ViewModel` por
  feature (`scan`, `map`, `history`, `profile`). Los `ViewModel` dependen solo de casos
  de uso de `domain`, nunca de `data` directamente.
- **core**: código transversal reutilizable por las demás capas (`designsystem`,
  `common` con `DispatcherProvider`/`Resource`, `analytics` con `CrashReporter`).
- **di**: módulos de Hilt que conectan las capas (qué implementación concreta se
  inyecta detrás de cada contrato).

## Regla de dependencias

`domain` no depende de ninguna otra capa. `data` y `presentation` dependen de
`domain`, nunca al revés. `presentation` no depende de `data`: solo conoce los casos de
uso que expone `domain`. Las entidades de Room sí pueden importar tipos de `domain`
(por ejemplo, `WasteRecordEntity` usa el enum `WasteCategory` para tipar una columna),
porque eso no crea una dependencia hacia Android, pero el camino inverso (que `domain`
importe algo de `data`) no está permitido.

Esto permite, por ejemplo, reemplazar Room por otra fuente de datos sin tocar
`domain` ni `presentation`, o escribir pruebas de los casos de uso y los ViewModels con
un repositorio simulado (MockK), sin una base de datos real.

## Flujo de datos: ejemplo con `WasteRecord`

1. **Room** guarda cada registro como una fila en la tabla `waste_records`
   (`WasteRecordEntity`, con `WasteCategory` serializado por `WasteCategoryConverter`).
2. **`WasteRecordDao.getAll()`** expone esa tabla como `Flow<List<WasteRecordEntity>>`.
3. **`WasteRecordRepositoryImpl`** (implementa `WasteRecordRepository` de `domain`)
   colecta ese flujo y usa `WasteRecordEntity.toDomain()` para mapear cada fila a un
   `WasteRecord` de dominio, sin exponer nunca la entidad de Room hacia afuera.
4. **`GetWasteRecordsUseCase`** simplemente delega en el repositorio:
   `repository.getWasteRecords()`. `GetTotalPointsUseCase` recibe la lista de
   `WasteRecord` y suma los puntos.
5. **`HistoryViewModel`** inyecta ambos casos de uso, colecta el flujo de registros,
   calcula el total de puntos y publica un `HistoryUiState` (`isLoading`, `records`,
   `totalPoints`, `errorMessage`) como `StateFlow`.
6. **`HistoryScreen`** observa ese `StateFlow` con `collectAsStateWithLifecycle()` y
   renderiza la lista con Compose. La pantalla nunca ve `WasteRecordEntity`: solo
   conoce el modelo `WasteRecord` de `domain`.

Cada inserción futura (por ejemplo, cuando el escaneo real del Sprint 3 registre un
residuo) sigue el camino inverso: `presentation` llama a un caso de uso de `domain`,
que delega en `WasteRecordRepository`, cuya implementación en `data` mapea el
`WasteRecord` a `WasteRecordEntity` (`toEntity()`) antes de insertarlo con Room.
