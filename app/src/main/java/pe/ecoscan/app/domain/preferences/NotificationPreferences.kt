package pe.ecoscan.app.domain.preferences

import kotlinx.coroutines.flow.Flow

// Preferencias de sonido y vibración de las notificaciones (HU09, parte 2). Son preferencias
// del dispositivo (se guardan con DataStore en la capa data), no del perfil del usuario: no se
// sincronizan con Firestore ni viajan con el resto de UserProfile.
interface NotificationPreferences {
    val soundEnabled: Flow<Boolean>
    val vibrationEnabled: Flow<Boolean>

    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
}
