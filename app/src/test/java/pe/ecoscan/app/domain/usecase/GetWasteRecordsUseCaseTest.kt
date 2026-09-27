package pe.ecoscan.app.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.repository.WasteRecordRepository

class GetWasteRecordsUseCaseTest {

    @Test
    fun `invoke devuelve los registros que expone el repositorio`() = runBlocking {
        val fakeRecords = listOf(
            WasteRecord("1", WasteCategory.PLASTIC, weightKg = 2.5, points = 25, recordedAt = 0L),
            WasteRecord("2", WasteCategory.GLASS, weightKg = 5.0, points = 30, recordedAt = 1_000L)
        )
        val repository = mockk<WasteRecordRepository>()
        every { repository.getWasteRecords() } returns flowOf(fakeRecords)

        val useCase = GetWasteRecordsUseCase(repository)

        assertEquals(fakeRecords, useCase().first())
    }
}
