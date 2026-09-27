package pe.ecoscan.app.domain.usecase

import pe.ecoscan.app.domain.model.WasteRecord
import javax.inject.Inject

class GetTotalPointsUseCase @Inject constructor() {
    operator fun invoke(records: List<WasteRecord>): Int = records.sumOf { it.points }
}
