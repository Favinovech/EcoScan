package pe.ecoscan.app.data.sync

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.data.local.dao.WasteRecordDao
import pe.ecoscan.app.data.local.entity.WasteRecordEntity
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.sync.WasteRecordSyncer
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val USERS_COLLECTION = "users"
private const val RECORDS_COLLECTION = "waste_records"

// Firestore limita cada lote a 500 operaciones.
private const val BATCH_SIZE = 400
private const val REMOTE_TIMEOUT_MS = 15_000L

// Implementación real de WasteRecordSyncer (domain/sync): sincroniza los registros de residuos
// de un usuario entre Room y Firestore (users/{uid}/waste_records/{syncId}). Primero sube los
// cambios locales y luego descarga los remotos. Ante un conflicto gana la modificación más
// reciente (updatedAt).
@Singleton
class WasteRecordSyncerImpl @Inject constructor(
    private val dao: WasteRecordDao,
    private val firestore: FirebaseFirestore,
    private val dispatcherProvider: DispatcherProvider
) : WasteRecordSyncer {

    // Evita dos sincronizaciones al mismo tiempo.
    private val mutex = Mutex()

    override suspend fun sync(uid: String): Result<Unit> = mutex.withLock {
        withContext(dispatcherProvider.io) {
            try {
                push(uid)
                pull(uid)
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun collection(uid: String) =
        firestore.collection(USERS_COLLECTION).document(uid).collection(RECORDS_COLLECTION)

    // ---- Subida: altas, ediciones y eliminaciones pendientes ----
    private suspend fun push(uid: String) {
        val pending = dao.getPendingByUser(uid).filter { it.syncId.isNotEmpty() }
        pending.chunked(BATCH_SIZE).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { entity ->
                val document = collection(uid).document(entity.syncId)
                if (entity.isDeleted) batch.delete(document) else batch.set(document, entity.toRemoteMap())
            }
            awaitRemote {
                batch.commit().await()
                Unit // commit() devuelve null (Void); con Unit el tiempo límite no lo confunde con un fallo
            }

            chunk.forEach { entity ->
                if (entity.isDeleted) dao.deleteById(entity.id)
                else dao.markSyncedIfUnchanged(entity.id, entity.updatedAt)
            }
        }
    }

    // ---- Descarga: registros nuevos o modificados en otros dispositivos ----
    private suspend fun pull(uid: String) {
        // Source.SERVER: sin conexión falla en vez de devolver una copia incompleta de la
        // caché de Firestore (con ella creeríamos que se borraron registros remotos).
        val snapshot = awaitRemote { collection(uid).get(Source.SERVER).await() }
        val remoteIds = HashSet<String>()
        val localBySyncId = dao.getAllByUser(uid).associateBy { it.syncId }

        snapshot.documents.forEach { document ->
            remoteIds += document.id
            val remote = document.toEntity(uid) ?: return@forEach
            val local = localBySyncId[document.id]
            when {
                local == null -> dao.insert(remote.copy(isSynced = true))
                local.isDeleted || !local.isSynced -> Unit // el cambio local pendiente tiene prioridad
                remote.updatedAt > local.updatedAt -> dao.applyRemote(
                    id = local.id,
                    category = remote.category,
                    weightKg = remote.weightKg,
                    points = remote.points,
                    recordedAt = remote.recordedAt,
                    updatedAt = remote.updatedAt
                )
                else -> Unit
            }
        }

        // Si un registro ya sincronizado no está en Firestore, se eliminó desde otro dispositivo.
        localBySyncId.values
            .filter { it.isSynced && !it.isDeleted && it.syncId !in remoteIds }
            .forEach { dao.deleteById(it.id) }
    }

    // Sin conexión las tareas de Firestore no fallan: esperan. Con el tiempo límite se
    // corta la espera y la sincronización se reintenta cuando vuelva la red.
    private suspend fun <T : Any> awaitRemote(block: suspend () -> T): T =
        withTimeoutOrNull(REMOTE_TIMEOUT_MS) { block() }
            ?: throw IOException("Tiempo de espera agotado al sincronizar")

    private fun WasteRecordEntity.toRemoteMap(): Map<String, Any> = mapOf(
        "category" to category.name,
        "weightKg" to weightKg,
        "points" to points,
        "recordedAt" to recordedAt,
        "updatedAt" to updatedAt
    )

    private fun DocumentSnapshot.toEntity(uid: String): WasteRecordEntity? {
        val category = getString("category")
            ?.let { name -> WasteCategory.entries.firstOrNull { it.name == name } }
            ?: return null
        return WasteRecordEntity(
            category = category,
            weightKg = getDouble("weightKg") ?: return null,
            points = getLong("points")?.toInt() ?: return null,
            recordedAt = getLong("recordedAt") ?: return null,
            userId = uid,
            syncId = id,
            updatedAt = getLong("updatedAt") ?: 0L
        )
    }
}