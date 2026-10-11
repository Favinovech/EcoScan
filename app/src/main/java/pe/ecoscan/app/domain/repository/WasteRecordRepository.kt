package pe.ecoscan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.ecoscan.app.domain.model.WasteRecord

// Contrato que implementa la capa data. El dominio no sabe de dónde vienen los datos.
interface WasteRecordRepository {

    // Registros del usuario con sesión, más recientes primero. La fuente es Room, así que
    // funciona sin conexión (offline-first).
    fun getWasteRecords(): Flow<List<WasteRecord>>

    // Actualiza categoría, peso y puntos de un registro. Queda pendiente de sincronizar.
    suspend fun updateWasteRecord(record: WasteRecord): Result<Unit>

    // Elimina un registro. Se oculta de inmediato y se borra de Firestore al sincronizar.
    suspend fun deleteWasteRecord(id: String): Result<Unit>
}