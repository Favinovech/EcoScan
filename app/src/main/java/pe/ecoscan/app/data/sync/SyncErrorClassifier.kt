package pe.ecoscan.app.data.sync

import com.google.firebase.firestore.FirebaseFirestoreException
import pe.ecoscan.app.domain.sync.SyncErrorType
import java.io.IOException

// Traduce una excepción concreta (de red o de Firestore) al SyncErrorType del dominio, que es
// lo único que entiende domain/sync/RetryPolicy.kt. Vive en data porque conoce tipos de
// Firebase; el dominio solo ve TRANSIENT/PERMANENT.
fun classifySyncError(error: Throwable): SyncErrorType = when (error) {
    is IOException -> SyncErrorType.TRANSIENT
    is FirebaseFirestoreException ->
        if (error.code in TRANSIENT_FIRESTORE_CODES) SyncErrorType.TRANSIENT else SyncErrorType.PERMANENT
    else -> SyncErrorType.PERMANENT
}

private val TRANSIENT_FIRESTORE_CODES = setOf(
    FirebaseFirestoreException.Code.UNAVAILABLE,
    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
    FirebaseFirestoreException.Code.ABORTED,
    FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED,
    FirebaseFirestoreException.Code.INTERNAL,
    FirebaseFirestoreException.Code.UNKNOWN
)
