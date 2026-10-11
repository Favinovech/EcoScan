package pe.ecoscan.app.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.repository.WasteRecordRepository

class UpdateWasteRecordUseCaseTest {

    private val repository = mockk<WasteRecordRepository>()
    private val useCase = UpdateWasteRecordUseCase(repository)
    private val record = WasteRecord("7", WasteCategory.PLASTIC, 1.0, 15, recordedAt = 1_000L)

    @Test
    fun `peso valido actualiza categoria, peso y recalcula los puntos`() = runTest {
        coEvery { repository.updateWasteRecord(any()) } returns Result.success(Unit)

        val result = useCase(record, WasteCategory.GLASS, 2.5)

        assertTrue(result.isSuccess)
        // Vidrio = 20 pts/kg x 2.5 kg = 50 pts
        coVerify {
            repository.updateWasteRecord(
                record.copy(category = WasteCategory.GLASS, weightKg = 2.5, points = 50)
            )
        }
    }

    @Test
    fun `los puntos nunca bajan de 1`() = runTest {
        coEvery { repository.updateWasteRecord(any()) } returns Result.success(Unit)

        useCase(record, WasteCategory.ORGANIC, 0.01)

        coVerify { repository.updateWasteRecord(match { it.points == 1 }) }
    }

    @Test
    fun `el peso se redondea a dos decimales`() = runTest {
        coEvery { repository.updateWasteRecord(any()) } returns Result.success(Unit)

        useCase(record, WasteCategory.PAPER, 1.23456)

        coVerify { repository.updateWasteRecord(match { it.weightKg == 1.23 }) }
    }

    @Test
    fun `peso cero devuelve error y no toca el repositorio`() = runTest {
        val result = useCase(record, WasteCategory.PLASTIC, 0.0)

        assertTrue(result.isFailure)
        assertEquals("El peso debe estar entre 0.01 y 1000 kg", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.updateWasteRecord(any()) }
    }

    @Test
    fun `peso mayor al maximo devuelve error`() = runTest {
        val result = useCase(record, WasteCategory.PLASTIC, 1000.5)

        assertTrue(result.isFailure)
    }

    @Test
    fun `peso NaN devuelve error`() = runTest {
        val result = useCase(record, WasteCategory.PLASTIC, Double.NaN)

        assertTrue(result.isFailure)
    }
}