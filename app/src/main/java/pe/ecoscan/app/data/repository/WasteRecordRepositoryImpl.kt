package pe.ecoscan.app.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.repository.WasteRecordRepository
import javax.inject.Inject

// Implementación en memoria para el Sprint 1. En la Parte 4 se reemplaza por una
// fuente de datos Room, sin que domain ni presentation necesiten cambiar.
class WasteRecordRepositoryImpl @Inject constructor() : WasteRecordRepository {

    override fun getWasteRecords(): Flow<List<WasteRecord>> = flow {
        delay(800)
        emit(sampleRecords)
    }

    private companion object {
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        private val now = System.currentTimeMillis()

        val sampleRecords = listOf(
            WasteRecord("1", WasteCategory.PLASTIC, weightKg = 2.5, points = 25, recordedAt = now - 0 * DAY_MILLIS),
            WasteRecord("2", WasteCategory.GLASS, weightKg = 5.0, points = 30, recordedAt = now - 1 * DAY_MILLIS),
            WasteRecord("3", WasteCategory.PAPER, weightKg = 3.2, points = 16, recordedAt = now - 2 * DAY_MILLIS),
            WasteRecord("4", WasteCategory.METAL, weightKg = 1.8, points = 36, recordedAt = now - 3 * DAY_MILLIS),
            WasteRecord("5", WasteCategory.ORGANIC, weightKg = 4.5, points = 9, recordedAt = now - 4 * DAY_MILLIS),
            WasteRecord("6", WasteCategory.HAZARDOUS, weightKg = 0.5, points = 50, recordedAt = now - 5 * DAY_MILLIS),
            WasteRecord("7", WasteCategory.PLASTIC, weightKg = 1.2, points = 12, recordedAt = now - 6 * DAY_MILLIS),
            WasteRecord("8", WasteCategory.PAPER, weightKg = 6.0, points = 30, recordedAt = now - 7 * DAY_MILLIS)
        )
    }
}
