package pe.ecoscan.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import pe.ecoscan.app.domain.model.WasteCategory

// Fila de la tabla "waste_records". La columna category se guarda como texto mediante
// WasteCategoryConverter.
// HU06 (offline-first + sincronización con Firestore):
// - userId: dueño del registro (uid de Firebase). Vacío en registros anteriores a HU06;
//   el primer usuario que inicia sesión los adopta.
// - syncId: identificador estable (UUID) que se usa como ID del documento en Firestore.
// - isSynced: false = hay cambios locales que aún no llegan a Firestore.
// - isDeleted: eliminación pendiente de propagar a Firestore (el registro ya no se muestra).
// - updatedAt: última modificación, para resolver conflictos (gana la más reciente).
@Entity(tableName = "waste_records")
data class WasteRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val category: WasteCategory,
    val weightKg: Double,
    val points: Int,
    val recordedAt: Long,
    @ColumnInfo(defaultValue = "0")
    val isSynced: Boolean = false,
    @ColumnInfo(defaultValue = "''")
    val userId: String = "",
    @ColumnInfo(defaultValue = "''")
    val syncId: String = "",
    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val updatedAt: Long = 0L
)