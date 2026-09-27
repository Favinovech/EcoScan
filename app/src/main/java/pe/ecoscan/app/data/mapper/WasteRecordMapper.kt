package pe.ecoscan.app.data.mapper

import pe.ecoscan.app.data.local.entity.WasteRecordEntity
import pe.ecoscan.app.domain.model.WasteRecord

// El dominio no conoce las entidades de Room: toda la conversión vive aquí.
fun WasteRecordEntity.toDomain(): WasteRecord = WasteRecord(
    id = id.toString(),
    category = category,
    weightKg = weightKg,
    points = points,
    recordedAt = recordedAt
)

fun WasteRecord.toEntity(): WasteRecordEntity = WasteRecordEntity(
    id = id.toLongOrNull() ?: 0L,
    category = category,
    weightKg = weightKg,
    points = points,
    recordedAt = recordedAt
)
