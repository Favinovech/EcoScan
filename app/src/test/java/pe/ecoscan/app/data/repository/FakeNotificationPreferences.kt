package pe.ecoscan.app.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import pe.ecoscan.app.domain.preferences.NotificationPreferences

// Preferencias en memoria solo para pruebas unitarias (sin DataStore).
class FakeNotificationPreferences : NotificationPreferences {

    val soundEnabledFlow = MutableStateFlow(true)
    val vibrationEnabledFlow = MutableStateFlow(true)

    override val soundEnabled = soundEnabledFlow
    override val vibrationEnabled = vibrationEnabledFlow

    override suspend fun setSoundEnabled(enabled: Boolean) {
        soundEnabledFlow.value = enabled
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        vibrationEnabledFlow.value = enabled
    }
}
