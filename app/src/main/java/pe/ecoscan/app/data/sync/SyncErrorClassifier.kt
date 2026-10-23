package pe.ecoscan.app.data.sync

import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException

// Decide si un fallo al sincronizar merece reintento (problema de red o del servidor,
// probablemente transitorio) o si hay que darlo por perdido (datos inválidos u otro error
// permanente, en el que reintentar no cambiaría el resultado).
// Función pura para poder probarla con JUnit sin levantar un Worker.
fun isTransientSyncError(error: Throwable): Boolean = when (error) {
    is IOException -> true
    is FirebaseFirestoreException -> error.code in TRANSIENT_FIRESTORE_CODES
    else -> false
}

private val TRANSIENT_FIRESTORE_CODES = setOf(
    FirebaseFirestoreException.Code.UNAVAILABLE,
    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
    FirebaseFirestoreException.Code.ABORTED,
    FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED,
    FirebaseFirestoreException.Code.INTERNAL,
    FirebaseFirestoreException.Code.UNKNOWN
)
