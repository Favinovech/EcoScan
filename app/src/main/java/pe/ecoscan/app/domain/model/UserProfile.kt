package pe.ecoscan.app.domain.model

// Perfil editable del usuario (HU03). Se identifica por el mismo uid de Firebase Auth.
// photoUrl es la URL de descarga de Firebase Storage; isSynced indica si los datos
// locales (Room) ya están reflejados en Firestore.
data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String = "",
    val district: String = "",
    val photoUrl: String? = null,
    val notificationsEnabled: Boolean = true,
    val weeklyGoalKg: Int = DEFAULT_WEEKLY_GOAL_KG,
    val isSynced: Boolean = true
) {
    companion object {
        const val DEFAULT_WEEKLY_GOAL_KG = 5
        const val MIN_WEEKLY_GOAL_KG = 1
        const val MAX_WEEKLY_GOAL_KG = 50
        const val MAX_NAME_LENGTH = 50
        const val MAX_DISTRICT_LENGTH = 40
    }
}