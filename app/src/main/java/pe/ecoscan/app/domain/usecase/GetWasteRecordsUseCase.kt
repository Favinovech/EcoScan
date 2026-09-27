package pe.ecoscan.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.repository.WasteRecordRepository
import javax.inject.Inject

class GetWasteRecordsUseCase @Inject constructor(
    private val repository: WasteRecordRepository
) {
    operator fun invoke(): Flow<List<WasteRecord>> = repository.getWasteRecords()
}
