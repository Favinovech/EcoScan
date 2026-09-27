package pe.ecoscan.app.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import pe.ecoscan.app.data.local.entity.WasteRecordEntity
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord

class WasteRecordMapperTest {

    @Test
    fun `entity a domain conserva todos los campos`() {
        val entity = WasteRecordEntity(
            id = 7L,
            category = WasteCategory.GLASS,
            weightKg = 5.0,
            points = 30,
            recordedAt = 123456L,
            isSynced = true
        )

        val domain = entity.toDomain()

        assertEquals("7", domain.id)
        assertEquals(WasteCategory.GLASS, domain.category)
        assertEquals(5.0, domain.weightKg, 0.0)
        assertEquals(30, domain.points)
        assertEquals(123456L, domain.recordedAt)
    }

    @Test
    fun `domain a entity conserva todos los campos`() {
        val record = WasteRecord(
            id = "7",
            category = WasteCategory.METAL,
            weightKg = 1.8,
            points = 36,
            recordedAt = 654321L
        )

        val entity = record.toEntity()

        assertEquals(7L, entity.id)
        assertEquals(WasteCategory.METAL, entity.category)
        assertEquals(1.8, entity.weightKg, 0.0)
        assertEquals(36, entity.points)
        assertEquals(654321L, entity.recordedAt)
        assertFalse(entity.isSynced)
    }

    @Test
    fun `ida y vuelta entity domain entity no pierde datos`() {
        val original = WasteRecordEntity(
            id = 3L,
            category = WasteCategory.HAZARDOUS,
            weightKg = 0.5,
            points = 50,
            recordedAt = 999L,
            isSynced = false
        )

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.category, roundTripped.category)
        assertEquals(original.weightKg, roundTripped.weightKg, 0.0)
        assertEquals(original.points, roundTripped.points)
        assertEquals(original.recordedAt, roundTripped.recordedAt)
    }
}
