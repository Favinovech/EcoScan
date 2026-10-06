package pe.ecoscan.app.presentation.profile

import pe.ecoscan.app.domain.model.UserProfile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val email: String = "",
    val displayName: String = "",
    val district: String = "",
    // URL remota (Firebase Storage) de la foto guardada.
    val photoUrl: String? = null,
    // Uri local de la foto recién elegida (cámara/galería) que aún no se sube.
    val pendingPhotoUri: String? = null,
    val notificationsEnabled: Boolean = true,
    val weeklyGoalKg: Int = UserProfile.DEFAULT_WEEKLY_GOAL_KG,
    // false = hay cambios guardados solo en el dispositivo, pendientes de subir a Firebase.
    val isSynced: Boolean = true,
    val hasUnsavedChanges: Boolean = false,
    val isSignedOut: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)