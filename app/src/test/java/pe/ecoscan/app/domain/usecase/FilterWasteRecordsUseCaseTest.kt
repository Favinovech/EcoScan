package pe.ecoscan.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.model.WasteRecordFilter

class FilterWasteRecordsUseCaseTest {

    private val useCase = FilterWasteRecordsUseCase()

    private val plasticDay1 = WasteRecord("1", WasteCategory.PLASTIC, 1.0, 15, recordedAt = 1_000L)
    private val glassDay2 = WasteRecord("2", WasteCategory.GLASS, 2.0, 40, recordedAt = 2_000L)
    private val plasticDay3 = WasteRecord("3", WasteCategory.PLASTIC, 3.0, 45, recordedAt = 3_000L)
    private val records = listOf(plasticDay1, glassDay2, plasticDay3)

    @Test
    fun `sin filtros devuelve todos los registros`() {
        assertEquals(records, useCase(records, WasteRecordFilter()))
    }

    @Test
    fun `filtra por categoria`() {
        val result = useCase(records, WasteRecordFilter(category = WasteCategory.PLASTIC))

        assertEquals(listOf(plasticDay1, plasticDay3), result)
    }

    @Test
    fun `filtra por rango de fechas con extremos inclusivos`() {
        val result = useCase(records, WasteRecordFilter(fromMillis = 2_000L, toMillis = 3_000L))

        assertEquals(listOf(glassDay2, plasticDay3), result)
    }

    @Test
    fun `solo fecha desde deja pasar los registros posteriores`() {
        val result = useCase(records, WasteRecordFilter(fromMillis = 2_500L))

        assertEquals(listOf(plasticDay3), result)
    }

    @Test
    fun `combina categoria y fechas`() {
        val result = useCase(
            records,
            WasteRecordFilter(category = WasteCategory.PLASTIC, fromMillis = 2_000L)
        )

        assertEquals(listOf(plasticDay3), result)
    }

    @Test
    fun `un filtro sin resultados devuelve lista vacia`() {
        val result = useCase(records, WasteRecordFilter(category = WasteCategory.METAL))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `isActive es falso solo cuando no hay ningun filtro`() {
        assertFalse(WasteRecordFilter().isActive)
        assertTrue(WasteRecordFilter(category = WasteCategory.GLASS).isActive)
        assertTrue(WasteRecordFilter(fromMillis = 1L).isActive)
        assertTrue(WasteRecordFilter(toMillis = 1L).isActive)
    }
}