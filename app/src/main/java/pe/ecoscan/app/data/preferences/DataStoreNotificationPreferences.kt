package pe.ecoscan.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import pe.ecoscan.app.domain.preferences.NotificationPreferences
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationPreferencesDataStore by preferencesDataStore(
    name = "notification_preferences"
)

private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")

// Sonido y vibración son preferencias del dispositivo (HU09, parte 2), no del perfil: se
// guardan con Preferences DataStore, no en Room, y no se sincronizan con Firestore.
@Singleton
class DataStoreNotificationPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationPreferences {

    override val soundEnabled = context.notificationPreferencesDataStore.data
        .map { prefs -> prefs[SOUND_ENABLED_KEY] ?: true }

    override val vibrationEnabled = context.notificationPreferencesDataStore.data
        .map { prefs -> prefs[VIBRATION_ENABLED_KEY] ?: true }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        context.notificationPreferencesDataStore.edit { prefs -> prefs[SOUND_ENABLED_KEY] = enabled }
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        context.notificationPreferencesDataStore.edit { prefs -> prefs[VIBRATION_ENABLED_KEY] = enabled }
    }
}
