package pe.ecoscan.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import pe.ecoscan.app.domain.model.WasteCategory

// Fila de la tabla "waste_records". La columna category se guarda como texto mediante
// WasteCategoryConverter. isSynced anticipa la sincronización con Firestore que se
// implementará en sprints posteriores.
@Entity(tableName = "waste_records")
data class WasteRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val category: WasteCategory,
    val weightKg: Double,
    val points: Int,
    val recordedAt: Long,
    @ColumnInfo(defaultValue = "0")
    val isSynced: Boolean = false
)
