package pe.ecoscan.app.domain.sync

// Clasificación de un fallo de sincronización. La capa data decide, a partir de la excepción
// real (IOException, FirebaseFirestoreException, etc.), a cuál de estos dos tipos corresponde;
// domain/sync/RetryPolicy.kt solo necesita saber esto para decidir si reintentar.
enum class SyncErrorType {
    TRANSIENT,
    PERMANENT
}
