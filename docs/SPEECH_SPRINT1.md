# Guion de exposición — EcoScan, Sprint 1

Guion para leer en voz alta. Ritmo de cálculo: 130 palabras por minuto. Duración total
estimada: ver el resumen al final de cada sección y el total al pie del documento.

---

## 1. Apertura: qué es EcoScan y qué problema resuelve     ⏱ ~60 s

**Qué muestro en pantalla:** nada de código todavía. Puede quedar la app corriendo en el
emulador, en la pantalla de historial.

**Qué digo:**
Buenos días profesora. Les voy a presentar el Sprint uno de EcoScan. EcoScan es una
aplicación Android para el caso del curso: ayuda a una persona a saber qué hacer con sus
residuos, dónde llevarlos a reciclar, y cuánto ha reciclado con el tiempo. El problema que
resolvemos es que la gente no sabe separar bien sus residuos, ni conoce los puntos de
acopio cercanos. Este primer sprint deja lista la base técnica de la aplicación:
la arquitectura, la base de datos, la conexión entre capas, la navegación y el sistema de
diseño. Todavía no hay escaneo real de productos, eso llega en el Sprint tres. Hoy van a
ver una app que compila, que navega entre cuatro pantallas, y que ya muestra un historial
de reciclaje con datos de ejemplo.

**Si me preguntan:**
- ¿Por qué no hay escaneo real todavía? Porque el Sprint uno es la base técnica; el
  escaneo con cámara y la API de productos entran en el Sprint tres.
- ¿De dónde salen los datos que se ven ahora? Son datos de ejemplo que se cargan solos la
  primera vez que se crea la base de datos, para no mostrar la pantalla vacía.

---

## 2. Estructura del proyecto y regla de dependencias     ⏱ ~65 s

**Qué muestro en pantalla:** árbol de paquetes en
`app/src/main/java/pe/ecoscan/app` (carpetas `domain`, `data`, `presentation`, `core`,
`di`); `settings.gradle.kts`, línea 26.

**Qué digo:**
Ahora vamos a la estructura del proyecto. Todo vive en un solo módulo de Gradle, que se
llama app, eso se ve acá en settings punto gradle, línea veintiséis. Pero adentro, el
código está separado en capas por paquete: domain, data, presentation, core y di. La capa
domain tiene los modelos, los contratos de repositorio y los casos de uso, y es Kotlin
puro, sin nada de Android. La capa data implementa esos contratos usando Room para
guardar en el teléfono. La capa presentation tiene una pantalla, un estado y un
ViewModel por cada función: escanear, mapa, historial y perfil. La regla que seguimos es
esta: domain no depende de nadie, data y presentation dependen de domain, y presentation
nunca llama directamente a data, solo conoce los casos de uso. Así, si mañana cambiamos
Room por otra base de datos, no tocamos ni domain ni presentation.

**Si me preguntan:**
- ¿Por qué no son módulos de Gradle separados? Porque para el tamaño actual del
  proyecto alcanza con separar por paquetes; la regla de dependencias se respeta igual.
- ¿Qué pasa si presentation necesita algo de data? No debería pasar nunca: si lo
  necesita, se crea un caso de uso nuevo en domain.

---

## 3. La capa de dominio     ⏱ ~50 s

**Qué muestro en pantalla:**
`app/src/main/java/pe/ecoscan/app/domain/model/WasteRecord.kt`, líneas 1 a 12;
`app/src/main/java/pe/ecoscan/app/domain/repository/WasteRecordRepository.kt`,
líneas 1 a 9.

**Qué digo:**
Abramos la capa de dominio. Este es WasteRecord, el modelo que representa un registro de
reciclaje: categoría, peso, puntos y fecha. Miren los imports arriba del archivo: no hay
ninguno. Es una clase de Kotlin puro. La fecha se guarda como un número, no como un tipo
de Android, porque formatear esa fecha para mostrarla es trabajo de la pantalla, no del
dominio. Ahora WasteRecordRepository: es solo una interfaz con un método, llamado
getWasteRecords, que devuelve un flujo de WasteRecord. El dominio no sabe si esos datos
vienen de Room, de un archivo, o de internet. Eso lo decide la capa data, que es la que
va a implementar esta interfaz.

**Si me preguntan:**
- ¿Por qué el id de WasteRecord es texto y no número? Porque en domain no queremos
  amarrarnos al tipo de clave que use Room; ese detalle se resuelve en el mapper.
- ¿Qué es un Flow? Es un flujo de datos de Kotlin que va emitiendo valores nuevos con el
  tiempo, en este caso, cada vez que cambia la lista de registros guardados.

---

## 4. La capa de datos     ⏱ ~60 s

**Qué muestro en pantalla:**
`app/src/main/java/pe/ecoscan/app/data/local/entity/WasteRecordEntity.kt`, líneas 1 a 21;
`app/src/main/java/pe/ecoscan/app/data/local/dao/WasteRecordDao.kt`, líneas 13 a 23;
`app/src/main/java/pe/ecoscan/app/data/mapper/WasteRecordMapper.kt`, líneas 7 a 21.

**Qué digo:**
Ahora la capa data. Esta es WasteRecordEntity: es la fila de la tabla waste guion
records en Room. Miren los imports: aquí sí aparece androidx punto room, porque esta
clase es justamente la que conoce la base de datos. Este es el DAO, que significa objeto
de acceso a datos: getAll trae todos los registros ordenados por fecha, como un flujo.
Pero ojo: ese flujo entrega WasteRecordEntity, no WasteRecord. Por eso existe el mapper.
La función toDomain convierte una entidad de Room en un WasteRecord del dominio, y
toEntity hace el camino contrario. Esta traducción existe porque el dominio no puede
conocer una clase anotada con Room. Si mañana cambiamos de base de datos, solo tocamos el
mapper y la entidad, el dominio no se entera de nada.

**Si me preguntan:**
- ¿Por qué la entidad puede importar WasteCategory del dominio, si dijimos que domain no
  depende de nadie? Porque la dependencia va en un solo sentido: data puede mirar hacia
  domain, pero domain nunca mira hacia data.
- ¿Qué es isSynced en la entidad? Es un campo pensado para cuando se sincronice con
  Firestore en un sprint posterior; hoy no se usa todavía.

---

## 5. Inyección de dependencias con Hilt     ⏱ ~65 s

**Qué muestro en pantalla:**
`app/src/main/java/pe/ecoscan/app/di/DatabaseModule.kt`, líneas 57 a 71;
`app/src/main/java/pe/ecoscan/app/di/RepositoryModule.kt`, líneas 10 a 16;
`app/src/main/java/pe/ecoscan/app/presentation/history/HistoryViewModel.kt`, líneas 21
a 26.

**Qué digo:**
Vamos a la inyección con Hilt. Este es DatabaseModule: acá se crea la base de datos Room,
y se provee el DAO que sale de ella. Fíjense que cada función tiene arroba Provides
encima. Ahora RepositoryModule: acá le decimos a Hilt que, cuando alguien pida un
WasteRecordRepository, le entregue un WasteRecordRepositoryImpl. Esto es un bind, no un
provide, porque la implementación ya trae su propio constructor marcado con arroba
Inject. Sigamos el camino completo de una dependencia: el DAO sale del módulo de base de
datos, el repositorio lo recibe en su constructor, el caso de uso recibe el repositorio,
y por último HistoryViewModel recibe los dos casos de uso y el proveedor de dispatchers,
todos en su constructor, y la clase entera está marcada con arroba HiltViewModel.
Nosotros nunca escribimos la palabra new en ningún lado: Hilt arma toda esta cadena solo.

**Si me preguntan:**
- ¿Qué diferencia hay entre Provides y Binds? Provides se usa cuando hay que construir
  el objeto a mano, como la base de datos. Binds se usa cuando la clase ya tiene un
  constructor inyectable, y solo hay que decir qué interfaz cumple.
- ¿Qué pasa si Hilt no encuentra cómo armar una dependencia? El proyecto no compila; el
  error sale en tiempo de compilación, no cuando la app ya está corriendo.

---

## 6. La capa de presentación     ⏱ ~65 s

**Qué muestro en pantalla:**
`app/src/main/java/pe/ecoscan/app/presentation/history/HistoryViewModel.kt`, líneas 35
a 53;
`app/src/main/java/pe/ecoscan/app/presentation/history/HistoryUiState.kt`, líneas 1 a 10;
`app/src/main/java/pe/ecoscan/app/presentation/history/HistoryScreen.kt`, líneas 79 a 92.

**Qué digo:**
Sigamos un dato concreto: el total de EcoPuntos que se ve en la pantalla de historial.
Todo empieza en HistoryViewModel, en la función observeWasteRecords: se suscribe al flujo
del caso de uso, y cada vez que llegan registros nuevos, llama a reduce. Ahí, en la línea
cuarenta y nueve, se calcula totalPoints llamando a getTotalPointsUseCase con los
registros que acaban de llegar. Ese resultado se guarda en HistoryUiState, que es un
estado inmutable que viaja como StateFlow. La pantalla, HistoryScreen, colecta ese estado
con una función que se llama collectAsStateWithLifecycle, que junta el estado con el
ciclo de vida de la pantalla. Y acá, en HistoryContent, hay un texto que muestra
uiState punto totalPoints directamente. Entonces el camino completo es: caso de uso,
ViewModel, estado, y pantalla. Ningún número se calcula dentro de Compose, todo llega ya
calculado desde el ViewModel.

**Si me preguntan:**
- ¿Qué es MVVM? Son las siglas de Modelo Vista Vista Modelo. Acá el Modelo es
  WasteRecord, la Vista es HistoryScreen, y la Vista Modelo es HistoryViewModel.
- ¿Por qué el estado es inmutable? Para que la pantalla nunca lo modifique por su
  cuenta; solo el ViewModel puede producir un estado nuevo.

---

## 7. Sistema de diseño     ⏱ ~65 s

**Qué muestro en pantalla:**
`app/src/main/java/pe/ecoscan/app/core/designsystem/theme/Color.kt`, líneas 6 a 33 y
36 a 62;
`app/src/main/java/pe/ecoscan/app/core/designsystem/theme/Theme.kt`, líneas 73 a 95.

**Qué digo:**
El sistema de diseño vive en Color punto kt y Theme punto kt. Acá tenemos toda la
paleta de EcoScan: un verde como color principal, un verde azulado como secundario, y un
ámbar como color de acento. Hay una paleta completa para tema claro y otra para tema
oscuro. Miren este comentario en la línea quince: el color de texto sobre el ámbar se
ajustó a mano, porque el blanco original no cumplía el contraste mínimo de
accesibilidad. En Theme punto kt, la función EcoScanTheme arma el tema de Material
Design con esa paleta, según si el sistema está en modo oscuro o no. No usamos dynamic
color, que es la función de Android que saca los colores del fondo de pantalla del
usuario. No la usamos a propósito: la identidad visual de EcoScan tiene que ser la misma
en cualquier teléfono.

**Si me preguntan:**
- ¿Qué es dynamic color? Es una función de Android doce en adelante que arma la paleta
  de la app a partir del fondo de pantalla del usuario.
- ¿Por qué ajustaron ese color a mano? Porque el blanco sobre el ámbar no se leía bien;
  el contraste no llegaba al mínimo que pide accesibilidad.

---

## 8. Navegación     ⏱ ~35 s

**Qué muestro en pantalla:**
`app/src/main/java/pe/ecoscan/app/presentation/navigation/EcoScanNavHost.kt`, líneas 1
a 28;
`app/src/main/java/pe/ecoscan/app/presentation/navigation/EcoScanApp.kt`, líneas 14
a 40.

**Qué digo:**
La navegación tiene dos piezas. EcoScanNavHost define las cuatro rutas: escanear, mapa,
historial y perfil, y arranca en escanear. EcoScanApp es la que junta ese NavHost con la
barra inferior. Miren estas líneas dentro de navigate: popUpTo, saveState en true, y
restoreState en true. Eso es lo que hace que, si estoy en historial, cambio a mapa, y
vuelvo a historial, no se recargue desde cero. Compose guarda el estado de cada pestaña
mientras no estoy en ella.

**Si me preguntan:**
- ¿Qué pasa si saco esas tres líneas? Cada vez que cambio de pestaña, la pantalla se
  reconstruiría desde cero y perdería el scroll o lo que estuviera cargando.
- ¿Por qué arranca en escanear y no en historial? Porque escanear es la acción principal
  de la app; es lo primero que un usuario nuevo debería usar.

---

## 9. Pruebas     ⏱ ~40 s

**Qué muestro en pantalla:**
`app/src/test/java/pe/ecoscan/app/data/mapper/WasteRecordMapperTest.kt`, líneas 1 a 71.

**Qué digo:**
Para las pruebas, miremos WasteRecordMapperTest. Son pruebas unitarias con JUnit, no
necesitan un teléfono ni un emulador, corren en segundos. La primera prueba verifica que,
al convertir una entidad a dominio, no se pierda ningún campo. La segunda verifica el
camino contrario, de dominio a entidad. Y la tercera hace ida y vuelta completa: entidad
a dominio, y de nuevo a entidad, y compara que el resultado sea igual al original. Esto
es justamente lo que expliqué antes: que el mapper no pierda datos al traducir entre
capas.

**Si me preguntan:**
- ¿Por qué probar el mapper y no la base de datos completa? Porque el mapper es lógica
  pura, corre rápido y sin dependencias; probar Room completo se deja para las pruebas
  instrumentadas, que sí existen en el proyecto pero corren más lento.
- ¿Cuántas pruebas hay en total en el proyecto? Hay pruebas unitarias para los mappers,
  los casos de uso, el repositorio y cada ViewModel, más pruebas instrumentadas para el
  DAO y la navegación.

---

## 10. Control de versiones     ⏱ ~60 s

**Qué muestro en pantalla:** `.github/workflows/android.yml`, líneas 1 a 51; historial de
commits en Android Studio o en GitHub.

**Qué digo:**
Para el control de versiones trabajamos con dos ramas principales: main y develop. El
trabajo se hace sobre develop, y cada commit tiene un prefijo que dice qué tipo de cambio
es. Por ejemplo, un commit dice feat, de navegación, y explica que se agregó la
navegación con Compose y la barra inferior. Otro dice feat, de datos, y explica que se
agregó Room y la configuración de Retrofit. Eso deja un historial que se lee como una
lista de entregas, no como una lista de arreglos sueltos. Y acá está el archivo de
integración continua, android punto yml: cada vez que alguien hace push a main o a
develop, o abre un pull request, se compila la app y se corren todas las pruebas
automáticamente.

**Si me preguntan:**
- ¿Qué pasa si una prueba falla en integración continua? El flujo sigue corriendo y
  publica el reporte de pruebas como artefacto, para poder revisar qué falló.
- ¿Por qué dos ramas y no una sola? Main queda como la rama estable, y develop es donde
  se integra el trabajo de cada sprint antes de pasar a main.

---

## 11. Cierre: qué queda listo y qué sigue en el Sprint 2     ⏱ ~50 s

**Qué muestro en pantalla:** nada de código, vuelvo a la app corriendo.

**Qué digo:**
Para cerrar: el Sprint uno deja lista toda la base de EcoScan. Tenemos la arquitectura en
capas, con un dominio puro. Tenemos Room guardando datos localmente, con su mapper.
Tenemos Hilt conectando todo. Tenemos navegación entre cuatro pantallas que no pierde
estado. Tenemos un sistema de diseño propio, con tema claro y oscuro. Y tenemos pruebas
automáticas corriendo en integración continua. Lo que no tenemos todavía, y es justo lo
que entra en el Sprint dos, es la pantalla de mapa con datos reales y la lógica de puntos
ligada a acciones reales del usuario, no a datos de ejemplo. Eso es todo, muchas gracias.

**Si me preguntan:**
- ¿Cuál es el mayor riesgo para el Sprint dos? Conseguir datos reales de puntos de
  acopio para el mapa; hoy esa pantalla todavía no tiene esa información.
- ¿Se puede probar la app ahora mismo? Sí, compila y corre; lo único simulado es el
  contenido del historial, que viene precargado.

---

## Orden de pestañas para abrir antes de empezar

| # | Archivo | Líneas a tener listas |
|---|---------|------------------------|
| 1 | `settings.gradle.kts` | 26 |
| 2 | `docs/ARQUITECTURA.md` (opcional, de apoyo) | 1–33 |
| 3 | `app/src/main/java/pe/ecoscan/app/domain/model/WasteRecord.kt` | 1–12 |
| 4 | `app/src/main/java/pe/ecoscan/app/domain/repository/WasteRecordRepository.kt` | 1–9 |
| 5 | `app/src/main/java/pe/ecoscan/app/data/local/entity/WasteRecordEntity.kt` | 1–21 |
| 6 | `app/src/main/java/pe/ecoscan/app/data/local/dao/WasteRecordDao.kt` | 13–23 |
| 7 | `app/src/main/java/pe/ecoscan/app/data/mapper/WasteRecordMapper.kt` | 7–21 |
| 8 | `app/src/main/java/pe/ecoscan/app/di/DatabaseModule.kt` | 57–71 |
| 9 | `app/src/main/java/pe/ecoscan/app/di/RepositoryModule.kt` | 10–16 |
| 10 | `app/src/main/java/pe/ecoscan/app/presentation/history/HistoryViewModel.kt` | 21–53 |
| 11 | `app/src/main/java/pe/ecoscan/app/presentation/history/HistoryUiState.kt` | 1–10 |
| 12 | `app/src/main/java/pe/ecoscan/app/presentation/history/HistoryScreen.kt` | 79–92 |
| 13 | `app/src/main/java/pe/ecoscan/app/core/designsystem/theme/Color.kt` | 6–62 |
| 14 | `app/src/main/java/pe/ecoscan/app/core/designsystem/theme/Theme.kt` | 73–95 |
| 15 | `app/src/main/java/pe/ecoscan/app/presentation/navigation/EcoScanNavHost.kt` | 1–28 |
| 16 | `app/src/main/java/pe/ecoscan/app/presentation/navigation/EcoScanApp.kt` | 14–40 |
| 17 | `app/src/test/java/pe/ecoscan/app/data/mapper/WasteRecordMapperTest.kt` | 1–71 |
| 18 | `.github/workflows/android.yml` | 1–51 |

## Versión de respaldo de 3 minutos

Si la profesora corta el tiempo, decir completas solo estas secciones, en este orden:

1. **Sección 1 — Apertura** (completa, ~40 s).
2. **Sección 2 — Estructura del proyecto**, pero solo la primera mitad: hasta "es
   Kotlin puro, sin nada de Android" (~25 s).
3. **Sección 6 — La capa de presentación** (completa, ~95 s): es la que mejor conecta
   domain, data y Hilt en un solo ejemplo, así que cubre casi todo el sprint en una sola
   explicación.
4. **Sección 11 — Cierre** (completa, ~35 s).

Saltar por completo: secciones 3, 4, 5, 7, 8, 9 y 10. Si sobra medio minuto, agregar la
primera pregunta de la sección 6 sobre MVVM, porque suele ser la que más preguntan.

## Términos para pronunciar con cuidado

| Término | Cómo decirlo |
|---------|--------------|
| MVVM | deletrear "eme-uve-uve-eme", después de haber dicho "Modelo Vista Vista Modelo" una vez |
| Hilt | como suena en inglés, "jilt" |
| ViewModel | "viu-módel", nunca traducirlo a mitad de frase |
| StateFlow | "esteit-flou" |
| DAO | deletrear "de-a-o" |
| collectAsStateWithLifecycle | decirlo en tres partes: "collect as state", pausa, "with lifecycle" |
| WasteRecordRepositoryImpl | decir "Waste Record Repository Impl", con una pausa antes de "Impl" |
| dynamic color | "dai-námic cólor" |
| popUpTo / saveState / restoreState | leerlas como tres palabras sueltas, no pegadas |
| Retrofit | "rétrofit", el acento va en la primera sílaba |

---

**Duración total estimada:** sumando las once secciones (unas 1 339 palabras habladas),
alrededor de 10 minutos y 20 segundos, hablando a ritmo normal (130 palabras por minuto).
