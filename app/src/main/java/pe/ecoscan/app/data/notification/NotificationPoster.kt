package pe.ecoscan.app.data.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import pe.ecoscan.app.R
import pe.ecoscan.app.domain.notification.selectNotificationChannelId
import pe.ecoscan.app.domain.preferences.NotificationPreferences
import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

// Punto único donde EcoScan muestra notificaciones visuales (recordatorios de la parte 1 y
// mensajes de FCM de la parte 2). Centraliza aquí el interruptor maestro del perfil, el canal
// según sonido/vibración, y el chequeo del permiso POST_NOTIFICATIONS, para que ningún llamador
// tenga que repetir esa lógica.
@Singleton
class NotificationPoster @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val notificationPreferences: NotificationPreferences
) {

    suspend fun post(notificationId: Int, title: String, body: String) {
        if (title.isBlank() && body.isBlank()) return
        if (!hasNotificationPermission()) return

        val uid = authRepository.currentUser.first()?.uid ?: return
        val profile = profileRepository.observeProfile(uid).first() ?: return
        if (!profile.notificationsEnabled) return

        val channelId = selectNotificationChannelId(
            soundEnabled = notificationPreferences.soundEnabled.first(),
            vibrationEnabled = notificationPreferences.vibrationEnabled.first()
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
