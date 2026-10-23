package pe.ecoscan.app.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.data.local.dao.WasteRecordDao
import pe.ecoscan.app.data.local.entity.WasteRecordEntity
import pe.ecoscan.app.data.mapper.toDomain
import pe.ecoscan.app.data.mapper.toEntity
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.repository.WasteRecordRepository
import java.util.UUID
import javax.inject.Inject

// Offline-first (HU06): la pantalla siempre lee y escribe en Room. La subida a Firestore la
// hace WasteRecordSyncCoordinator en segundo plano cuando hay conexión.
class WasteRecordRepositoryImpl @Inject constructor(
    private val dao: WasteRecordDao,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : WasteRecordRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getWasteRecords(): Flow<List<WasteRecord>> =
        authRepository.currentUser
            .map { user -> user?.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) {
                    flowOf(emptyList<WasteRecordEntity>())
                } else {
                    flow {
                        // Los registros anteriores a HU06 no tienen dueño: los adopta este usuario.
                        dao.claimLegacyRecords(uid)
                        emitAll(dao.observeByUser(uid))
                    }
                }
            }
            .map { entities -> entities.map { it.toDomain() } }

    // Lo usa el flujo de escaneo (ScanViewModel). Sin sesión iniciada no se guarda nada.
    suspend fun insert(record: WasteRecord) {
        withContext(dispatcherProvider.io) {
            val uid = authRepository.currentUser.first()?.uid
            if (uid != null) {
                val entity = record.toEntity().copy(
                    userId = uid,
                    syncId = UUID.randomUUID().toString(),
                    isSynced = false,
                    updatedAt = System.currentTimeMillis()
                )
                dao.insert(entity)
            }
        }
    }

    override suspend fun updateWasteRecord(record: WasteRecord): Result<Unit> =
        withContext(dispatcherProvider.io) {
            val id = record.id.toLongOrNull()
                ?: return@withContext Result.failure(IllegalArgumentException("Registro inválido"))
            try {
                val rows = dao.updateEditable(
                    id = id,
                    category = record.category,
                    weightKg = record.weightKg,
                    points = record.points,
                    updatedAt = System.currentTimeMillis()
                )
                if (rows > 0) Result.success(Unit)
                else Result.failure(IllegalStateException("El registro ya no existe"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteWasteRecord(id: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            val localId = id.toLongOrNull()
                ?: return@withContext Result.failure(IllegalArgumentException("Registro inválido"))
            try {
                // No se borra la fila: se marca para propagar la eliminación a Firestore.
                dao.markDeleted(localId, System.currentTimeMillis())
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteById(id: String) = withContext(dispatcherProvider.io) {
        id.toLongOrNull()?.let { dao.deleteById(it) }
    }

    suspend fun deleteAll() = withContext(dispatcherProvider.io) {
        dao.deleteAll()
    }
}