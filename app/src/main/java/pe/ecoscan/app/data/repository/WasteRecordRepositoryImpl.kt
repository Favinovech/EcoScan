package pe.ecoscan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.data.local.dao.WasteRecordDao
import pe.ecoscan.app.data.mapper.toDomain
import pe.ecoscan.app.data.mapper.toEntity
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.repository.WasteRecordRepository
import javax.inject.Inject

class WasteRecordRepositoryImpl @Inject constructor(
    private val dao: WasteRecordDao,
    private val dispatcherProvider: DispatcherProvider
) : WasteRecordRepository {

    override fun getWasteRecords(): Flow<List<WasteRecord>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun insert(record: WasteRecord) = withContext(dispatcherProvider.io) {
        dao.insert(record.toEntity())
    }

    suspend fun deleteById(id: String) = withContext(dispatcherProvider.io) {
        id.toLongOrNull()?.let { dao.deleteById(it) }
    }

    suspend fun deleteAll() = withContext(dispatcherProvider.io) {
        dao.deleteAll()
    }
}
