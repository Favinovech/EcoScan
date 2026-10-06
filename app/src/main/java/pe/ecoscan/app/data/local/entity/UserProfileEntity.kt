package pe.ecoscan.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Fila de la tabla "user_profiles": copia local del perfil para usarlo sin conexión.
// isSynced = false significa que hay cambios locales que aún no llegan a Firestore.
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val uid: String,
    val email: String,
    val displayName: String,
    val district: String,
    val photoUrl: String?,
    val notificationsEnabled: Boolean,
    val weeklyGoalKg: Int,
    @ColumnInfo(defaultValue = "0")
    val isSynced: Boolean = false
)