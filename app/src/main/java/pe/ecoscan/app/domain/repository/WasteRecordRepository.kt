package pe.ecoscan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.ecoscan.app.domain.model.WasteRecord

// Contrato que implementa la capa data. El dominio no sabe de dónde vienen los datos.
interface WasteRecordRepository {
    fun getWasteRecords(): Flow<List<WasteRecord>>
}
