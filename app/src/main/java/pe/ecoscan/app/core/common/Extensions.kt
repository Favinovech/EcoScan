package pe.ecoscan.app.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Adapta cualquier Flow<T> a un Flow<Resource<T>>, emitiendo Loading al iniciar
// y Error si la colección del flujo original falla.
fun <T> Flow<T>.asResource(): Flow<Resource<T>> =
    map<T, Resource<T>> { Resource.Success(it) }
        .onStart { emit(Resource.Loading) }
        .catch { throwable -> emit(Resource.Error(throwable.message ?: "Error desconocido")) }

private val defaultDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-PE"))

// Formatea una marca de tiempo (epoch millis) a texto legible para la UI.
// Vive en presentación/común porque el dominio solo trabaja con Long puro.
fun Long.toFormattedDate(formatter: DateTimeFormatter = defaultDateFormatter): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
