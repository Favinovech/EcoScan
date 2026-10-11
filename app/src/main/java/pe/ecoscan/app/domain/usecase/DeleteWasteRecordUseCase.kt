package pe.ecoscan.app.domain.usecase

import pe.ecoscan.app.domain.repository.WasteRecordRepository
import javax.inject.Inject

class DeleteWasteRecordUseCase @Inject constructor(
    private val repository: WasteRecordRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.deleteWasteRecord(id)
}