package pe.ecoscan.app.domain.sync

// Sincroniza los registros de residuos de un usuario entre el almacenamiento local y la nube.
// Interfaz (en vez de depender directamente de la implementación) para poder sustituirla por un
// fake en las pruebas de SyncWorker; la implementación real vive en data/sync porque depende de
// Firestore y Room.
interface WasteRecordSyncer {
    suspend fun sync(uid: String): Result<Unit>
}
