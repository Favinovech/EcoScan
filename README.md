# EcoScan

Aplicación Android nativa para una empresa que opera puntos de acopio de residuos
reciclables en Lima. Permite registrar lo que una persona deposita (categoría, peso,
puntos ganados), ver su historial y ubicar puntos de acopio cercanos. Proyecto
académico que se construye en 7 sprints.

## Stack

- Kotlin + Jetpack Compose (Material 3)
- Arquitectura por capas (domain / data / presentation) con MVVM
- Hilt para inyección de dependencias
- Room para persistencia local
- Retrofit + OkHttp (consumo de Open Food Facts, a partir del Sprint 3)
- Navigation Compose
- Firebase: Crashlytics y Analytics activos; Auth, Firestore y Storage declarados para
  sprints posteriores
- JUnit4, MockK, Turbine y Compose UI Testing para pruebas
- GitHub Actions para integración continua

## Estructura de paquetes

```
app/src/main/java/pe/ecoscan/app/
├── core/
│   ├── analytics/       # CrashReporter: abstracción sobre Firebase
│   ├── common/          # DispatcherProvider, Resource, extensiones
│   └── designsystem/    # Tema Material 3 y componentes reutilizables
├── data/
│   ├── local/           # Room: entidades, DAO, converters, base de datos
│   ├── mapper/          # Conversión entidad/DTO <-> modelo de dominio
│   ├── remote/          # Retrofit: API y DTOs
│   └── repository/      # Implementaciones de los repositorios del dominio
├── di/                  # Módulos de Hilt (red, base de datos, repositorios, analytics)
├── domain/
│   ├── model/           # Modelos puros (sin Android)
│   ├── repository/      # Contratos que implementa data/
│   └── usecase/         # Casos de uso
├── presentation/
│   ├── history/ map/ profile/ scan/   # Pantalla + UiState + ViewModel por feature
│   └── navigation/      # Destinos, bottom bar, NavHost, EcoScanApp
├── EcoScanApplication.kt
└── MainActivity.kt
```

## Cómo ejecutarlo

1. Clona el repositorio.
2. **Firebase:** entra a la [consola de Firebase](https://console.firebase.google.com/),
   crea o abre el proyecto de EcoScan, registra la app Android con el
   `applicationId` `pe.ecoscan.app`, y descarga el archivo `google-services.json`.
   Colócalo en `app/google-services.json` (no se versiona; está en `.gitignore`).
   Si no lo agregas, el proyecto igual compila y corre, pero sin Crashlytics/Analytics
   ni el resto de servicios de Firebase.
3. Abre el proyecto en Android Studio o compílalo por línea de comandos:
   - `./gradlew assembleDebug` — genera el APK de debug.
   - `./gradlew test` — corre las pruebas unitarias (`app/src/test`).
   - Las pruebas instrumentadas (`app/src/androidTest`) requieren un emulador o
     dispositivo conectado: `./gradlew connectedAndroidTest`.

## Convenciones

- **Ramas:** `main` (estable) y `develop` (integración); el trabajo de cada parte se
  hace sobre `develop`.
- **Commits:** formato `tipo(alcance): descripción breve`, en español, por ejemplo
  `feat(data): persistencia con Room y configuracion de Retrofit` o
  `chore(setup): configura Gradle, version catalog e Hilt`.

## Estado del proyecto

### Sprint 1 (completo)

- Parte 1: Gradle, version catalog, Hilt, `EcoScanApplication`, `MainActivity`.
- Parte 2: sistema de diseño Material 3 en `core/designsystem`.
- Parte 3: capas domain/data/presentation con el flujo completo de ejemplo
  (`WasteRecord`, casos de uso, `HistoryViewModel`, `HistoryScreen`).
- Parte 4: persistencia con Room (con precarga de datos de ejemplo) y Retrofit
  configurado (sin consumir todavía ningún endpoint real).
- Parte 5: navegación Compose con bottom bar y cuatro destinos (Escanear, Mapa,
  Historial, Perfil).
- Parte 6: Firebase (Crashlytics + Analytics activos; Auth/Firestore/Storage
  declarados), batería de pruebas unitarias e instrumentadas, integración continua en
  GitHub Actions, y esta documentación.

### Próximos sprints

Fuera de alcance por ahora: escaneo real de productos (cámara / código de barras),
consumo del endpoint de Open Food Facts, mapa real de puntos de acopio, autenticación,
sincronización con Firestore y el resto de funcionalidad de perfil.
