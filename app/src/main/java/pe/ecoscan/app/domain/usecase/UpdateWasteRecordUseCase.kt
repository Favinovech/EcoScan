package pe.ecoscan.app.domain.usecase

import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteDisposalInfo
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.repository.WasteRecordRepository
import javax.inject.Inject
import kotlin.math.round

// Valida la edición y recalcula los puntos con la misma fórmula que usa el escaneo
// (puntos por kg de la categoría x peso, mínimo 1).
class UpdateWasteRecordUseCase @Inject constructor(
    private val repository: WasteRecordRepository
) {
    suspend operator fun invoke(
        record: WasteRecord,
        newCategory: WasteCategory,
        newWeightKg: Double
    ): Result<Unit> {
        if (newWeightKg.isNaN() ||
            newWeightKg < WasteRecord.MIN_WEIGHT_KG ||
            newWeightKg > WasteRecord.MAX_WEIGHT_KG
        ) {
            return Result.failure(IllegalArgumentException("El peso debe estar entre 0.01 y 1000 kg"))
        }
        val weight = round(newWeightKg * 100) / 100
        val points = (WasteDisposalInfo.getDefaultPoints(newCategory) * weight).toInt().coerceAtLeast(1)
        return repository.updateWasteRecord(
            record.copy(category = newCategory, weightKg = weight, points = points)
        )
    }
}