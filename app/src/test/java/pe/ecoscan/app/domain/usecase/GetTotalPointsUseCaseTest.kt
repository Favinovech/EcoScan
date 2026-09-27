package pe.ecoscan.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord

class GetTotalPointsUseCaseTest {

    private val useCase = GetTotalPointsUseCase()

    @Test
    fun `invoke suma los puntos de todos los registros`() {
        val records = listOf(
            WasteRecord("1", WasteCategory.PLASTIC, weightKg = 2.5, points = 25, recordedAt = 0L),
            WasteRecord("2", WasteCategory.GLASS, weightKg = 5.0, points = 30, recordedAt = 1_000L)
        )

        assertEquals(55, useCase(records))
    }

    @Test
    fun `invoke devuelve cero cuando la lista esta vacia`() {
        assertEquals(0, useCase(emptyList()))
    }
}
