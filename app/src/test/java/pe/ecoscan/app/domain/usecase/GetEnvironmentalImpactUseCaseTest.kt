package pe.ecoscan.app.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.ecoscan.app.domain.model.TimeRangeFilter
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.repository.WasteRecordRepository

class GetEnvironmentalImpactUseCaseTest {

    @Test
    fun `calcula metricas de impacto ambiental correctamente a partir de los registros`() = runBlocking {
        val fakeRecords = listOf(
            WasteRecord(
                id = "1",
                category = WasteCategory.PLASTIC,
                weightKg = 2.0,
                points = 20,
                recordedAt = 1700000000000L
            ),
            WasteRecord(
                id = "2",
                category = WasteCategory.PAPER,
                weightKg = 1.0,
                points = 10,
                recordedAt = 1700000000000L
            )
        )
        val repository = mockk<WasteRecordRepository>()
        every { repository.getWasteRecords() } returns flowOf(fakeRecords)

        val useCase = GetEnvironmentalImpactUseCase(repository)
        val result = useCase(TimeRangeFilter.WEEKLY).first()

        // 2 kg plástico = 3.0 kg CO2, 11.4 L agua
        // 1 kg papel = 0.9 kg CO2, 26.0 L agua
        // Total: 3.0 kg peso, 3.9 kg CO2, 37.4 L agua
        assertEquals(3.0, result.metrics.totalWeightKg, 0.01)
        assertEquals(3.9, result.metrics.co2SavedKg, 0.01)
        assertEquals(37.4, result.metrics.waterSavedLiters, 0.01)
        assertEquals(2, result.materialDistribution.size)
        assertTrue(result.barChartData.isNotEmpty())
    }
}